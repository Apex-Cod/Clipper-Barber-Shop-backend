# Guía de Borrado Lógico (Soft Delete)

## 📋 Resumen de Implementación

Se ha implementado un sistema completo de **borrado lógico** para las entidades principales del sistema. Esto permite "eliminar" registros sin borrarlos físicamente de la base de datos, manteniendo la integridad referencial y el historial.

## 🏗️ Arquitectura

### Clase Base: `SoftDeletableEntity`

Todas las entidades que requieren borrado lógico extienden de esta clase base:

```java
@MappedSuperclass
public abstract class SoftDeletableEntity {
    private Boolean deleted = false;        // Indica si está eliminado
    private LocalDateTime deletedAt;        // Cuándo se eliminó
    private String deletedBy;               // Quién lo eliminó
    
    // Métodos de utilidad
    public void softDelete(String deletedBy);
    public void restore();
    public boolean isActive();
}
```

## 📊 Entidades con Borrado Lógico

| Entidad | Implementado | Campos de BD | Notas |
|---------|--------------|--------------|-------|
| **Usuario** | ✅ | deleted, deleted_at, deleted_by | Esencial |
| **Empresa** | ✅ | deleted, deleted_at, deleted_by | Mantiene también `estado` |
| **Servicio** | ✅ | deleted, deleted_at, deleted_by | Campo `activo` deprecado |
| **Reserva** | ✅ | deleted, deleted_at, deleted_by | Mantiene también `status` |
| **Promocion** | ✅ | deleted, deleted_at, deleted_by | Campo `activa` deprecado |
| **Disponibilidad** | ✅ | deleted, deleted_at, deleted_by | Campo `disponible` deprecado |
| **Plan** | ✅ | deleted, deleted_at, deleted_by | Nuevo |
| **Resenia** | ✅ | deleted, deleted_at, deleted_by | Opcional (auditoría) |
| **Pago** | ❌ | - | NO eliminar (auditoría) |
| **Suscripcion** | ❌ | - | NO eliminar (auditoría) |
| **LogActividad** | ❌ | - | NO eliminar (auditoría) |
| **ConfiguracionEmpresa** | ❌ | - | Se elimina con Empresa |

## 🔧 Uso Básico

### 1. Eliminar un Registro (Soft Delete)

```java
// Método 1: Usando el método de utilidad
usuario.softDelete(adminUserId);
usuarioRepository.save(usuario);

// Método 2: Manualmente
usuario.setDeleted(true);
usuario.setDeletedAt(LocalDateTime.now());
usuario.setDeletedBy(adminUserId);
usuarioRepository.save(usuario);
```

### 2. Restaurar un Registro Eliminado

```java
usuario.restore();
usuarioRepository.save(usuario);
```

### 3. Verificar si un Registro está Activo

```java
if (usuario.isActive()) {
    // El usuario no ha sido eliminado
    System.out.println("Usuario activo");
}

// O directamente
if (!usuario.getDeleted()) {
    // Usuario activo
}
```

### 4. Consultar Solo Registros Activos

```java
// En el Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
    
    // Buscar solo usuarios activos
    List<Usuario> findByDeletedFalse();
    
    // Buscar por empresa solo activos
    List<Usuario> findByEmpresaAndDeletedFalse(Empresa empresa);
    
    // Buscar por email solo si está activo
    Optional<Usuario> findByEmailAndDeletedFalse(String email);
}
```

### 5. Consultar Registros Eliminados

```java
// Buscar solo eliminados
List<Usuario> findByDeletedTrue();

// Buscar eliminados en un rango de fechas
List<Usuario> findByDeletedTrueAndDeletedAtBetween(
    LocalDateTime start, 
    LocalDateTime end
);

// Buscar eliminados por un usuario específico
List<Usuario> findByDeletedTrueAndDeletedBy(String userId);
```

## 🎯 Ejemplos de Implementación en Servicios

### Ejemplo 1: Servicio de Usuario

```java
@Service
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    
    // Eliminar usuario (soft delete)
    public void eliminarUsuario(String userId, String deletedBy) {
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (usuario.getDeleted()) {
            throw new IllegalStateException("El usuario ya está eliminado");
        }
        
        usuario.softDelete(deletedBy);
        usuarioRepository.save(usuario);
        
        // Opcional: Registrar en log de actividad
        logActividad("DELETE_USER", userId, deletedBy);
    }
    
    // Restaurar usuario
    public void restaurarUsuario(String userId) {
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        if (!usuario.getDeleted()) {
            throw new IllegalStateException("El usuario no está eliminado");
        }
        
        usuario.restore();
        usuarioRepository.save(usuario);
    }
    
    // Listar usuarios activos
    public List<Usuario> listarUsuariosActivos() {
        return usuarioRepository.findByDeletedFalse();
    }
    
    // Listar usuarios eliminados (para panel de administración)
    public List<Usuario> listarUsuariosEliminados() {
        return usuarioRepository.findByDeletedTrue();
    }
}
```

### Ejemplo 2: Servicio de Empresa

```java
@Service
@Transactional
public class EmpresaService {
    
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    
    // Eliminar empresa (soft delete en cascada)
    public void eliminarEmpresa(Long empresaId, String deletedBy) {
        Empresa empresa = empresaRepository.findById(empresaId)
            .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        
        // Marcar empresa como eliminada
        empresa.softDelete(deletedBy);
        empresa.setEstado("INACTIVA");
        
        // Eliminar en cascada todos los usuarios asociados
        List<Usuario> usuarios = usuarioRepository
            .findByEmpresaAndDeletedFalse(empresa);
        
        for (Usuario usuario : usuarios) {
            usuario.softDelete(deletedBy);
        }
        
        usuarioRepository.saveAll(usuarios);
        empresaRepository.save(empresa);
    }
}
```

### Ejemplo 3: Servicio de Reserva

```java
@Service
public class ReservaService {
    
    private final ReservaRepository reservaRepository;
    
    // Cancelar reserva (cambiar estado + soft delete)
    public void cancelarReserva(Long reservaId, String userId) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));
        
        if (reserva.getDeleted()) {
            throw new IllegalStateException("La reserva ya está eliminada");
        }
        
        // Cambiar estado primero
        reserva.setStatus("CANCELLED");
        
        // Luego soft delete
        reserva.softDelete(userId);
        
        reservaRepository.save(reserva);
    }
    
    // Listar reservas activas de un cliente
    public List<Reserva> listarReservasActivas(String clientId) {
        return reservaRepository
            .findByClientIdAndDeletedFalseOrderByReservationDateDesc(clientId);
    }
}
```

## 🔍 Filtros y Especificaciones JPA

### Usando Specifications para Consultas Complejas

```java
public class UsuarioSpecifications {
    
    // Solo activos
    public static Specification<Usuario> isActive() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }
    
    // Por rol y activos
    public static Specification<Usuario> hasRoleAndActive(String role) {
        return (root, query, cb) -> cb.and(
            cb.equal(root.get("role"), role),
            cb.equal(root.get("deleted"), false)
        );
    }
    
    // Búsqueda por nombre incluyendo activos
    public static Specification<Usuario> nameContainsAndActive(String name) {
        return (root, query, cb) -> cb.and(
            cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"),
            cb.equal(root.get("deleted"), false)
        );
    }
}

// Uso
List<Usuario> usuarios = usuarioRepository.findAll(
    UsuarioSpecifications.hasRoleAndActive("EMPLOYEE")
);
```

## 🚨 Consideraciones Importantes

### 1. Integridad Referencial

⚠️ **Cuidado con las relaciones en cascada:**

```java
// INCORRECTO: Podría eliminar físicamente registros relacionados
@OneToMany(cascade = CascadeType.REMOVE)
private List<Reserva> reservas;

// CORRECTO: No usar REMOVE, manejar soft delete manualmente
@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private List<Reserva> reservas;
```

### 2. Consultas por Defecto

**Siempre agregar filtro de `deleted = false` en consultas:**

```java
// ❌ MAL
List<Usuario> findByEmail(String email);

// ✅ BIEN
List<Usuario> findByEmailAndDeletedFalse(String email);
```

### 3. Índices en Base de Datos

Para mejor performance, crear índices:

```sql
CREATE INDEX idx_usuarios_deleted ON usuarios(deleted);
CREATE INDEX idx_usuarios_deleted_at ON usuarios(deleted_at);
CREATE INDEX idx_empresas_deleted ON empresas(deleted);
CREATE INDEX idx_servicios_deleted ON servicios(deleted);
CREATE INDEX idx_reservas_deleted ON reservas(deleted);
```

### 4. Validaciones en Login

```java
// Verificar que el usuario no esté eliminado al autenticarse
Optional<Usuario> usuario = usuarioRepository
    .findByEmailAndDeletedFalse(email);

if (usuario.isEmpty()) {
    throw new BadCredentialsException("Usuario no encontrado o inactivo");
}
```

## 📱 Endpoints REST Sugeridos

```java
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    
    // Eliminar (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> eliminar(@PathVariable String id, Principal principal) {
        usuarioService.eliminarUsuario(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Usuario eliminado", null));
    }
    
    // Restaurar
    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> restaurar(@PathVariable String id) {
        usuarioService.restaurarUsuario(id);
        return ResponseEntity.ok(ApiResponse.success("Usuario restaurado", null));
    }
    
    // Listar eliminados (papelera)
    @GetMapping("/deleted")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<?> listarEliminados() {
        List<Usuario> deleted = usuarioService.listarUsuariosEliminados();
        return ResponseEntity.ok(ApiResponse.success("Usuarios eliminados", deleted));
    }
}
```

## 🔄 Migración de Datos Existentes

### Script SQL para migrar datos

```sql
-- Agregar columnas de soft delete a tablas existentes
ALTER TABLE usuarios 
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN deleted_by VARCHAR(255);

ALTER TABLE empresas 
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN deleted_by VARCHAR(255);

ALTER TABLE servicios 
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMP,
    ADD COLUMN deleted_by VARCHAR(255);

-- Migrar datos del campo 'activo' a 'deleted' (invertido)
UPDATE servicios SET deleted = NOT COALESCE(activo, TRUE);
UPDATE promociones SET deleted = NOT COALESCE(activa, TRUE);
UPDATE disponibilidad SET deleted = NOT COALESCE(disponible, TRUE);

-- Migrar empresas con estado INACTIVA
UPDATE empresas SET deleted = TRUE WHERE estado = 'INACTIVA';

-- Crear índices
CREATE INDEX idx_usuarios_deleted ON usuarios(deleted);
CREATE INDEX idx_empresas_deleted ON empresas(deleted);
CREATE INDEX idx_servicios_deleted ON servicios(deleted);
CREATE INDEX idx_reservas_deleted ON reservas(deleted);
CREATE INDEX idx_promociones_deleted ON promociones(deleted);
CREATE INDEX idx_planes_deleted ON planes(deleted);
```

## 📈 Ventajas del Borrado Lógico

1. ✅ **Auditoría completa**: Se mantiene el historial de todos los registros
2. ✅ **Recuperación fácil**: Los datos pueden restaurarse si fue un error
3. ✅ **Integridad referencial**: No se rompen las relaciones entre tablas
4. ✅ **Análisis histórico**: Permite reportes y estadísticas completas
5. ✅ **Cumplimiento legal**: Mantiene registros para auditorías
6. ✅ **Trazabilidad**: Se sabe quién y cuándo eliminó cada registro

## ⚠️ Desventajas y Consideraciones

1. ❌ **Mayor uso de almacenamiento**: Los datos nunca se eliminan físicamente
2. ❌ **Complejidad en consultas**: Siempre filtrar por `deleted = false`
3. ❌ **Índices únicos**: Pueden causar problemas (email único si se reutiliza)
4. ❌ **Performance**: Tablas más grandes pueden ser más lentas

### Solución para índices únicos:

```java
// Opción 1: Índice único parcial (PostgreSQL)
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_email_unique_active", 
           columnList = "email", 
           unique = true) // Solo aplicar a deleted = false
})

// Opción 2: Agregar timestamp al email al eliminar
public void softDelete(String deletedBy) {
    this.deleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
    // Modificar email para permitir reutilización
    this.email = this.email + "_deleted_" + System.currentTimeMillis();
}
```

## 🎓 Mejores Prácticas

1. **Siempre usar el método `softDelete()`** en lugar de setear manualmente
2. **Validar que no esté ya eliminado** antes de eliminar
3. **Registrar en logs** todas las eliminaciones y restauraciones
4. **Implementar permisos** solo usuarios autorizados pueden eliminar
5. **Mostrar papelera** en el panel de administración
6. **Agregar filtros por defecto** en todos los repositorios
7. **Documentar** qué entidades usan soft delete y cuáles no
8. **Crear tareas programadas** para limpieza de registros muy antiguos (opcional)

## 🧹 Limpieza Periódica (Opcional)

Si se requiere eliminar físicamente registros muy antiguos:

```java
@Scheduled(cron = "0 0 2 * * ?") // Cada día a las 2 AM
public void limpiezaDeRegistrosAntiguos() {
    LocalDateTime fechaLimite = LocalDateTime.now().minusYears(5);
    
    List<Usuario> usuariosAntiguos = usuarioRepository
        .findByDeletedTrueAndDeletedAtBefore(fechaLimite);
    
    // Eliminar físicamente solo si es necesario
    usuarioRepository.deleteAll(usuariosAntiguos);
    
    log.info("Limpieza completada: {} usuarios eliminados físicamente", 
             usuariosAntiguos.size());
}
```
