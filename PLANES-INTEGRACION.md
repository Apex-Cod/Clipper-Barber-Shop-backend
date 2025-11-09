# Guía de Integración: Validación de Límites de Plan

## 📋 Descripción

Esta guía muestra cómo integrar el `PlanLimitService` en tus servicios existentes para validar los límites del plan antes de crear recursos.

---

## 🔧 Configuración Inicial

### 1. Inyectar el Servicio

En cualquier servicio donde necesites validar límites, inyecta `PlanLimitService`:

```java
import apex.code.clipperBarberShop.plan.application.service.PlanLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TuServicio {
    
    private final PlanLimitService planLimitService;
    // ... otros servicios
}
```

---

## 💡 Ejemplos de Integración

### Ejemplo 1: Validar Usuarios

**Archivo**: `user/application/service/UsuarioOwnerService.java`

```java
package apex.code.clipperBarberShop.user.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Usuario;
import apex.code.clipperBarberShop.plan.application.service.PlanLimitService;
import apex.code.clipperBarberShop.plan.domain.exception.PlanLimitExceededException;
import apex.code.clipperBarberShop.register.domain.port.out.EmpresaRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UsuarioOwnerService {
    
    private final PlanLimitService planLimitService;
    private final EmpresaRepositoryPort empresaRepository;
    // ... otros repositorios

    public Usuario crearEmpleado(Long empresaId, CrearEmpleadoRequest request) {
        log.info("Creando empleado para empresa ID: {}", empresaId);
        
        // 1. Obtener la empresa
        Empresa empresa = empresaRepository.findByIdAndDeletedFalse(empresaId)
                .orElseThrow(() -> new EmpresaNotFoundException(
                    "Empresa no encontrada con ID: " + empresaId
                ));
        
        // 2. 🔒 VALIDAR LÍMITE DEL PLAN
        int cantidadActualUsuarios = empresa.getUsuarios().size();
        
        try {
            planLimitService.validarLimiteUsuarios(empresa, cantidadActualUsuarios);
        } catch (PlanLimitExceededException e) {
            log.warn("Límite de usuarios excedido para empresa {}: {}", 
                     empresaId, e.getMessage());
            throw e; // Re-lanzar para que el controlador lo maneje
        }
        
        // 3. Continuar con la creación del usuario
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .empresa(empresa)
                .rol(Rol.EMPLOYEE)
                .build();
        
        return usuarioRepository.save(nuevoUsuario);
    }
}
```

---

### Ejemplo 2: Validar Reservas

**Archivo**: `reserva/application/service/ReservaClientService.java`

```java
package apex.code.clipperBarberShop.reserva.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Reserva;
import apex.code.clipperBarberShop.plan.application.service.PlanLimitService;
import apex.code.clipperBarberShop.reserva.domain.port.out.ReservaRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservaClientService {
    
    private final PlanLimitService planLimitService;
    private final ReservaRepositoryPort reservaRepository;
    private final EmpresaRepositoryPort empresaRepository;

    public Reserva crearReserva(Long empresaId, CrearReservaRequest request) {
        log.info("Creando reserva para empresa ID: {}", empresaId);
        
        // 1. Obtener la empresa
        Empresa empresa = empresaRepository.findByIdAndDeletedFalse(empresaId)
                .orElseThrow(() -> new EmpresaNotFoundException(
                    "Empresa no encontrada con ID: " + empresaId
                ));
        
        // 2. Contar reservas del mes actual
        YearMonth mesActual = YearMonth.now();
        LocalDate inicioMes = mesActual.atDay(1);
        LocalDate finMes = mesActual.atEndOfMonth();
        
        int reservasEsteMes = reservaRepository.countByEmpresaIdAndFechaBetween(
            empresaId, 
            inicioMes.atStartOfDay(), 
            finMes.atTime(23, 59, 59)
        );
        
        // 3. 🔒 VALIDAR LÍMITE DEL PLAN
        planLimitService.validarLimiteReservas(empresa, reservasEsteMes);
        
        // 4. Continuar con la creación de la reserva
        Reserva nuevaReserva = Reserva.builder()
                .empresa(empresa)
                .fecha(request.getFecha())
                .hora(request.getHora())
                // ... otros campos
                .build();
        
        return reservaRepository.save(nuevaReserva);
    }
}
```

**Nota**: Necesitarás agregar este método al repositorio de reservas:

```java
// En ReservaRepositoryPort.java
int countByEmpresaIdAndFechaBetween(Long empresaId, LocalDateTime inicio, LocalDateTime fin);

// En SpringDataReservaRepository.java
int countByEmpresaIdAndFechaHoraBetween(Long empresaId, LocalDateTime inicio, LocalDateTime fin);
```

---

### Ejemplo 3: Validar Servicios

**Archivo**: `servicio/application/service/ServicioOwnerService.java`

```java
package apex.code.clipperBarberShop.servicio.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Servicio;
import apex.code.clipperBarberShop.plan.application.service.PlanLimitService;
import apex.code.clipperBarberShop.servicio.domain.port.out.ServicioRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ServicioOwnerService {
    
    private final PlanLimitService planLimitService;
    private final ServicioRepositoryPort servicioRepository;
    private final EmpresaRepositoryPort empresaRepository;

    public Servicio crearServicio(Long empresaId, ServicioRequest request) {
        log.info("Creando servicio para empresa ID: {}", empresaId);
        
        // 1. Obtener la empresa
        Empresa empresa = empresaRepository.findByIdAndDeletedFalse(empresaId)
                .orElseThrow(() -> new EmpresaNotFoundException(
                    "Empresa no encontrada con ID: " + empresaId
                ));
        
        // 2. Contar servicios actuales (no eliminados)
        int serviciosActuales = servicioRepository.countByEmpresaIdAndDeletedFalse(empresaId);
        
        // 3. 🔒 VALIDAR LÍMITE DEL PLAN
        planLimitService.validarLimiteServicios(empresa, serviciosActuales);
        
        // 4. Continuar con la creación del servicio
        Servicio nuevoServicio = Servicio.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .duracion(request.getDuracion())
                .empresa(empresa)
                .build();
        
        return servicioRepository.save(nuevoServicio);
    }
}
```

---

### Ejemplo 4: Validar Promociones

**Archivo**: `promocion/application/service/PromocionOwnerService.java`

```java
package apex.code.clipperBarberShop.promocion.application.service;

import apex.code.clipperBarberShop.Entities.Empresa;
import apex.code.clipperBarberShop.Entities.Promocion;
import apex.code.clipperBarberShop.plan.application.service.PlanLimitService;
import apex.code.clipperBarberShop.promocion.domain.port.out.PromocionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromocionOwnerService {
    
    private final PlanLimitService planLimitService;
    private final PromocionRepositoryPort promocionRepository;
    private final EmpresaRepositoryPort empresaRepository;

    public Promocion crearPromocion(Long empresaId, PromocionRequest request) {
        log.info("Creando promoción para empresa ID: {}", empresaId);
        
        // 1. Obtener la empresa
        Empresa empresa = empresaRepository.findByIdAndDeletedFalse(empresaId)
                .orElseThrow(() -> new EmpresaNotFoundException(
                    "Empresa no encontrada con ID: " + empresaId
                ));
        
        // 2. Contar promociones activas (no eliminadas y activas)
        int promocionesActivas = promocionRepository.countByEmpresaIdAndActivaTrueAndDeletedFalse(empresaId);
        
        // 3. 🔒 VALIDAR LÍMITE DEL PLAN
        planLimitService.validarLimitePromociones(empresa, promocionesActivas);
        
        // 4. Continuar con la creación de la promoción
        Promocion nuevaPromocion = Promocion.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .descuento(request.getDescuento())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .empresa(empresa)
                .activa(true)
                .build();
        
        return promocionRepository.save(nuevaPromocion);
    }
}
```

---

## 🎨 Manejo de Excepciones en Controladores

### Opción 1: Try-Catch en el Controlador

```java
@PostMapping("/usuarios")
public ResponseEntity<?> crearUsuario(@RequestBody CrearUsuarioRequest request) {
    try {
        Usuario usuario = usuarioService.crearEmpleado(empresaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
        
    } catch (PlanLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(Map.of(
                    "error", "Límite del plan excedido",
                    "mensaje", e.getMessage(),
                    "recurso", e.getRecurso(),
                    "actual", e.getLimiteActual(),
                    "permitido", e.getLimitePermitido(),
                    "sugerencia", "Actualiza tu plan para continuar"
                ));
    }
}
```

### Opción 2: Global Exception Handler (Recomendado)

Crea un `@RestControllerAdvice` para manejar las excepciones globalmente:

```java
package apex.code.clipperBarberShop.shared.exception;

import apex.code.clipperBarberShop.plan.domain.exception.PlanLimitExceededException;
import apex.code.clipperBarberShop.plan.domain.exception.PlanNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handlePlanLimitExceeded(
            PlanLimitExceededException ex) {
        
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.PAYMENT_REQUIRED.value());
        error.put("error", "Límite del Plan Excedido");
        error.put("mensaje", ex.getMessage());
        error.put("recurso", ex.getRecurso());
        error.put("limiteActual", ex.getLimiteActual());
        error.put("limitePermitido", ex.getLimitePermitido());
        error.put("sugerencia", "Actualiza tu plan para acceder a más recursos");
        
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(error);
    }

    @ExceptionHandler(PlanNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePlanNotFound(
            PlanNotFoundException ex) {
        
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.NOT_FOUND.value());
        error.put("error", "Plan No Encontrado");
        error.put("mensaje", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
```

---

## 📊 Respuestas de Error

### Cuando se excede el límite

```json
{
  "timestamp": "2025-11-09T10:30:00",
  "status": 402,
  "error": "Límite del Plan Excedido",
  "mensaje": "Límite del plan excedido para usuarios. Actual: 2, Permitido: 2",
  "recurso": "usuarios",
  "limiteActual": 2,
  "limitePermitido": 2,
  "sugerencia": "Actualiza tu plan para acceder a más recursos"
}
```

---

## 🔍 Obtener Información de Límites

Puedes crear un endpoint para que los usuarios vean sus límites actuales:

```java
@RestController
@RequestMapping("/api/owner/empresa")
@RequiredArgsConstructor
public class EmpresaInfoController {
    
    private final PlanLimitService planLimitService;
    private final EmpresaRepositoryPort empresaRepository;

    @GetMapping("/limites")
    public ResponseEntity<LimitesResponse> obtenerLimitesEmpresa(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Obtener empresa del usuario autenticado
        Long empresaId = obtenerEmpresaIdDelUsuario(userDetails);
        
        Empresa empresa = empresaRepository.findByIdAndDeletedFalse(empresaId)
                .orElseThrow(() -> new EmpresaNotFoundException(
                    "Empresa no encontrada"
                ));
        
        // Obtener información de límites
        var limitesInfo = planLimitService.obtenerLimitesInfo(empresa);
        
        // Contar recursos actuales
        int usuariosActuales = empresa.getUsuarios().size();
        int serviciosActuales = servicioRepository.countByEmpresaIdAndDeletedFalse(empresaId);
        // ... contar otros recursos
        
        return ResponseEntity.ok(LimitesResponse.builder()
                .planNombre(limitesInfo.getNombrePlan())
                .planTipo(limitesInfo.getTipoPlan())
                .usuarios(new RecursoInfo(
                    usuariosActuales,
                    limitesInfo.getLimiteUsuarios(),
                    limitesInfo.isUsuariosIlimitados()
                ))
                .servicios(new RecursoInfo(
                    serviciosActuales,
                    limitesInfo.getLimiteServicios(),
                    limitesInfo.isServiciosIlimitados()
                ))
                // ... otros recursos
                .build());
    }
}

@Data
@Builder
class LimitesResponse {
    private String planNombre;
    private TipoPlan planTipo;
    private RecursoInfo usuarios;
    private RecursoInfo reservas;
    private RecursoInfo servicios;
    private RecursoInfo promociones;
}

@Data
@AllArgsConstructor
class RecursoInfo {
    private int actual;
    private Integer limite;  // null = ilimitado
    private boolean ilimitado;
}
```

**Respuesta:**
```json
{
  "planNombre": "Plan Básico",
  "planTipo": "BASICO",
  "usuarios": {
    "actual": 1,
    "limite": 2,
    "ilimitado": false
  },
  "reservas": {
    "actual": 30,
    "limite": 50,
    "ilimitado": false
  },
  "servicios": {
    "actual": 5,
    "limite": 10,
    "ilimitado": false
  },
  "promociones": {
    "actual": 0,
    "limite": 2,
    "ilimitado": false
  }
}
```

---

## ✅ Checklist de Integración

Antes de hacer commit, asegúrate de:

- [ ] Inyectar `PlanLimitService` en el servicio
- [ ] Contar los recursos actuales correctamente
- [ ] Llamar al método de validación apropiado
- [ ] Manejar `PlanLimitExceededException`
- [ ] Agregar logs informativos
- [ ] Probar con diferentes planes (GRATUITO, BASICO, PREMIUM)
- [ ] Verificar comportamiento con límites ilimitados (null)
- [ ] Documentar en comentarios el límite validado

---

## 📝 Notas Importantes

1. **Transacciones**: Las validaciones deben estar dentro de la transacción para evitar race conditions
2. **Performance**: Las validaciones son muy rápidas (solo consultas de conteo)
3. **Orden**: Valida ANTES de crear el recurso, no después
4. **Mensajes**: Los mensajes de error son claros y ayudan al usuario a entender qué hacer
5. **Límites Ilimitados**: Si un límite es `null`, la validación pasa automáticamente

---

**Fecha de Actualización**: 2025-11-09  
**Versión**: 1.0.0
