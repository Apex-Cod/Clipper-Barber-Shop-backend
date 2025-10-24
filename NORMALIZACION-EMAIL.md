# Normalización de Correos Electrónicos

## Estándar RFC 5321/5322

Según los estándares RFC 5321/5322, técnicamente el **local-part** (la parte antes del `@`) de un correo electrónico podría ser case-sensitive. Sin embargo, en la práctica:

- **Ningún proveedor importante** (Gmail, Outlook, Yahoo, etc.) trata el local-part como case-sensitive
- **La mejor práctica** es normalizar siempre los correos electrónicos a **minúsculas**
- Esto evita problemas de autenticación y duplicación de usuarios

## Implementación

### 1. Utilidad Centralizada

Se creó el método `StringUtils.sanitizeEmail()` para normalizar correos:

```java
public static String sanitizeEmail(String email) {
    return email != null ? email.trim().toLowerCase() : null;
}
```

### 2. Módulo de Registro

**Archivo:** `RegistroServiceImpl.java`

✅ **Ya implementado correctamente:**
- `registrarEmpresaConAdmin()` - normaliza tanto `empresaEmail` como `adminEmail`
- `registrarEmpleado()` - normaliza el email
- `registrarCliente()` - normaliza el email
- `registrarEmpleadoByAdmin()` - normaliza el email

```java
String email = request.getEmail().trim().toLowerCase();
```

### 3. Módulo de Autenticación

**Archivo:** `AuthService.java`

✅ **Corregido:**
```java
@Override
public AuthResponse authenticateUser(AuthRequest request) {
    // Normalizar email a minúsculas según RFC 5321/5322 (best practice)
    String normalizedEmail = request.getEmail().trim().toLowerCase();
    
    Usuario user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    // ...
}
```

### 4. Spring Security UserDetailsService

**Archivo:** `DatabaseUserDetailsService.java`

✅ **Corregido:**
```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Normalizar email a minúsculas según RFC 5321/5322 (best practice)
    String normalizedEmail = username.trim().toLowerCase();
    
    Usuario u = usuarioRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    // ...
}
```

### 5. Módulo de Gestión de Usuarios

**Archivo:** `UserManagementService.java`

✅ **Ya implementado correctamente:**
```java
String email = StringUtils.sanitizeEmail(request.getEmail());
```

## Flujo Completo

### Registro de Usuario
1. Usuario ingresa: `JuAn@ExAmPlE.cOm`
2. Sistema normaliza a: `juan@example.com`
3. Se guarda en BD: `juan@example.com`

### Login
1. Usuario ingresa: `JUAN@EXAMPLE.COM`
2. Sistema normaliza a: `juan@example.com`
3. Sistema busca y encuentra el usuario correctamente ✅

### Actualización de Datos
1. Usuario actualiza email a: `Juan.Perez@Gmail.COM`
2. Sistema normaliza a: `juan.perez@gmail.com`
3. Se guarda en BD: `juan.perez@gmail.com`

## Ventajas

1. ✅ **Case-insensitive authentication** - Los usuarios pueden iniciar sesión con cualquier combinación de mayúsculas/minúsculas
2. ✅ **Previene duplicados** - No se pueden crear dos usuarios con `juan@example.com` y `Juan@Example.com`
3. ✅ **Estándar de la industria** - Sigue las mejores prácticas de todos los proveedores principales
4. ✅ **Experiencia de usuario mejorada** - No hay confusión por mayúsculas/minúsculas

## Testing

Se recomienda probar los siguientes casos:

```bash
# Caso 1: Registro y login con diferentes mayúsculas
POST /api/register/cliente
{
  "email": "Test@Example.COM",
  "password": "password123"
}

POST /api/auth/login
{
  "email": "test@example.com",  // Debe funcionar ✅
  "password": "password123"
}

# Caso 2: Evitar duplicados
POST /api/register/cliente
{
  "email": "juan@test.com",
  "password": "pass1"
}

POST /api/register/cliente
{
  "email": "Juan@Test.COM",  // Debe fallar: email ya registrado ✅
  "password": "pass2"
}
```

## Archivos Modificados

- ✅ `/src/main/java/apex/code/clipperBarberShop/auth/application/service/AuthService.java`
- ✅ `/src/main/java/apex/code/clipperBarberShop/security/DatabaseUserDetailsService.java`

## Archivos que ya estaban correctos

- `/src/main/java/apex/code/clipperBarberShop/register/application/service/impl/RegistroServiceImpl.java`
- `/src/main/java/apex/code/clipperBarberShop/user/application/service/UserManagementService.java`
- `/src/main/java/apex/code/clipperBarberShop/shared/util/StringUtils.java`
