# Módulo de Gestión de users

## 📋 Descripción

Módulo CRUD completo para la gestión de users del sistema Clipper Barber Shop. Incluye operaciones de lectura, actualización, cambio de contraseña, cambio de estado (activo/inactivo) y eliminación lógica (soft delete).

## 🏗️ Arquitectura Hexagonal

```
user/
├── adapters/
│   ├── in/
│   │   └── web/
│   │       └── UserManagementController.java
│   └── out/
│       └── persistence/
│           ├── SpringDataUserRepository.java
│           └── UserPersistenceAdapter.java
├── application/
│   ├── dto/
│   │   ├── UsuarioResponse.java
│   │   ├── ActualizarUsuarioRequest.java
│   │   ├── CambiarPasswordRequest.java
│   │   └── CambiarEstadoRequest.java
│   └── service/
│       └── UserManagementService.java
└── domain/
    └── port/
        ├── in/
        │   └── UserManagementUseCase.java
        └── out/
            └── UserRepositoryPort.java
```

## 🔑 Funcionalidades

### 1. Consultar users

#### Obtener Usuario por ID
```http
GET /api/users/{id}
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Usuario obtenido",
  "data": {
    "id": "uuid",
    "empresaId": 1,
    "empresaNombre": "Barbería El Clipper",
    "name": "Juan",
    "lastName": "Pérez",
    "email": "juan@example.com",
    "role": "EMPLOYEE",
    "activo": true,
    "registrationDate": "2025-10-19T10:30:00",
    "deleted": false,
    "deletedAt": null,
    "deletedBy": null
  }
}
```

#### Obtener Perfil del Usuario Autenticado
```http
GET /api/users/me
Authorization: Bearer {token}
```

#### Listar users por Empresa
```http
GET /api/users/empresa/{empresaId}
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

#### Listar Todos los users
```http
GET /api/users
Authorization: Bearer {token}
Roles: OWNER
```

#### Listar users Eliminados (Papelera)
```http
GET /api/users/deleted?empresaId={empresaId}
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

### 2. Actualizar Usuario

#### Actualizar Información
```http
PUT /api/users/{id}
Authorization: Bearer {token}
Roles: OWNER, ADMIN
Content-Type: application/json

{
  "name": "Juan Carlos",
  "lastName": "Pérez García",
  "email": "juancarlos@example.com"
}
```

**Validaciones:**
- Nombre: 2-50 caracteres, solo letras y espacios
- Apellido: 2-50 caracteres, solo letras y espacios
- Email: formato válido, máximo 100 caracteres, único

#### Actualizar Perfil Propio
```http
PUT /api/users/me
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Juan Carlos",
  "lastName": "Pérez García",
  "email": "juancarlos@example.com"
}
```

### 3. Cambiar Contraseña

#### Cambiar Contraseña de Usuario Específico
```http
PUT /api/users/{id}/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPassword": "Password123!",
  "newPassword": "NewSecure456@",
  "confirmPassword": "NewSecure456@"
}
```

**Validaciones:**
- Contraseña actual correcta
- Nueva contraseña cumple requisitos de seguridad
- Contraseñas coinciden
- Nueva contraseña diferente a la actual

#### Cambiar Propia Contraseña
```http
PUT /api/users/me/password
Authorization: Bearer {token}
Content-Type: application/json

{
  "currentPassword": "Password123!",
  "newPassword": "NewSecure456@",
  "confirmPassword": "NewSecure456@"
}
```

### 4. Gestionar Estado (Activo/Inactivo)

#### Cambiar Estado
```http
PATCH /api/users/{id}/estado
Authorization: Bearer {token}
Roles: OWNER, ADMIN
Content-Type: application/json

{
  "activo": false,
  "motivo": "Usuario suspendido por incumplimiento"
}
```

#### Activar Usuario
```http
PATCH /api/users/{id}/activar
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

#### Desactivar Usuario
```http
PATCH /api/users/{id}/desactivar
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

**Diferencia entre Desactivar y Eliminar:**
- **Desactivar**: El usuario no puede hacer login pero sigue visible en el sistema
- **Eliminar**: Soft delete, el usuario va a la papelera

### 5. Eliminar y Restaurar

#### Eliminar Usuario (Soft Delete)
```http
DELETE /api/users/{id}
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

**Efectos:**
- `deleted = true`
- `deletedAt = now()`
- `deletedBy = userId del que elimina`
- `activo = false`
- No puede hacer login
- Va a la papelera

#### Restaurar Usuario
```http
POST /api/users/{id}/restore
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

**Efectos:**
- `deleted = false`
- `deletedAt = null`
- `deletedBy = null`
- `activo = true`
- Puede hacer login nuevamente

## 🔒 Control de Acceso

| Endpoint | OWNER | ADMIN | EMPLOYEE | CLIENT |
|----------|-------|-------|----------|--------|
| GET /users/{id} | ✅ | ✅ | ❌ | ❌ |
| GET /users/me | ✅ | ✅ | ✅ | ✅ |
| GET /users/empresa/{id} | ✅ | ✅ | ❌ | ❌ |
| GET /users | ✅ | ❌ | ❌ | ❌ |
| GET /users/deleted | ✅ | ✅ | ❌ | ❌ |
| PUT /users/{id} | ✅ | ✅ | ❌ | ❌ |
| PUT /users/me | ✅ | ✅ | ✅ | ✅ |
| PUT /users/{id}/password | ✅* | ❌ | ✅* | ✅* |
| PUT /users/me/password | ✅ | ✅ | ✅ | ✅ |
| PATCH /users/{id}/estado | ✅ | ✅ | ❌ | ❌ |
| DELETE /users/{id} | ✅ | ✅ | ❌ | ❌ |
| POST /users/{id}/restore | ✅ | ✅ | ❌ | ❌ |

\* Solo puede cambiar su propia contraseña

## 🎯 Casos de Uso

### Caso 1: Empleado se registra y luego actualiza su perfil
```bash
# 1. Admin registra empleado (módulo register)
POST /api/registro/empleado

# 2. Empleado hace login
POST /api/auth/login

# 3. Empleado actualiza su perfil
PUT /api/users/me

# 4. Empleado cambia su contraseña
PUT /api/users/me/password
```

### Caso 2: Admin desactiva temporalmente un empleado
```bash
# 1. Admin desactiva el empleado
PATCH /api/users/{id}/desactivar

# 2. Empleado intenta hacer login
POST /api/auth/login
# ❌ Error: "Usuario inactivo. Contacte al administrador"

# 3. Admin reactiva el empleado
PATCH /api/users/{id}/activar

# 4. Empleado puede hacer login nuevamente
POST /api/auth/login
# ✅ Exitoso
```

### Caso 3: Eliminación y restauración de usuario
```bash
# 1. Admin elimina empleado (soft delete)
DELETE /api/users/{id}

# 2. Ver users eliminados
GET /api/users/deleted

# 3. Restaurar empleado
POST /api/users/{id}/restore

# 4. Empleado puede usar el sistema nuevamente
```

## 🔧 Validaciones

### Autenticación
- ✅ Usuario debe existir
- ✅ Usuario no debe estar eliminado (`deleted = false`)
- ✅ Usuario debe estar activo (`activo = true`)
- ✅ Contraseña correcta

### Actualización de Usuario
- ✅ Usuario no debe estar eliminado
- ✅ Email único (si se cambia)
- ✅ Datos sanitizados (trim, toLowerCase en email)
- ✅ Validaciones de formato

### Cambio de Contraseña
- ✅ Contraseña actual correcta
- ✅ Nueva contraseña cumple requisitos
- ✅ Confirmación coincide
- ✅ Nueva contraseña diferente a la actual

### Eliminación
- ✅ Usuario no debe estar ya eliminado
- ✅ Se registra quién eliminó

### Restauración
- ✅ Usuario debe estar eliminado
- ✅ Email no debe estar en uso por otro usuario activo

## 📊 Base de Datos

### Nuevos Campos en `users`
```sql
activo BOOLEAN NOT NULL DEFAULT TRUE
```

### Consultas Optimizadas
```sql
-- Solo users activos y no eliminados
SELECT * FROM users 
WHERE activo = TRUE AND deleted = FALSE;

-- users de una empresa activos
SELECT * FROM users 
WHERE empresa_id = ? AND activo = TRUE AND deleted = FALSE;

-- users eliminados de una empresa
SELECT * FROM users 
WHERE empresa_id = ? AND deleted = TRUE;
```

### Índices Recomendados
```sql
CREATE INDEX idx_users_activo ON users(activo);
CREATE INDEX idx_users_activo_deleted ON users(activo, deleted);
CREATE INDEX idx_users_empresa_activo ON users(empresa_id, activo, deleted);
```

## 🧪 Testing

### Ejemplo de Tests
```java
@Test
void debeActualizarUsuario() {
    // Given
    String userId = "user-123";
    ActualizarUsuarioRequest request = new ActualizarUsuarioRequest(
        "Juan Carlos", "Pérez García", "nuevoemail@example.com"
    );
    
    // When
    UsuarioResponse response = userManagementUseCase
        .actualizarUsuario(userId, request, "admin-123");
    
    // Then
    assertEquals("Juan Carlos", response.getName());
    assertEquals("nuevoemail@example.com", response.getEmail());
}

@Test
void noDebePermitirLoginUsuarioInactivo() {
    // Given
    Usuario usuario = crearUsuario();
    usuario.setActivo(false);
    
    // When & Then
    assertThrows(IllegalArgumentException.class, () -> {
        authService.authenticateUser(new AuthRequest(
            usuario.getEmail(), "password"
        ));
    });
}
```

## 🚀 Próximas Mejoras

- [ ] Historial de cambios de usuario
- [ ] Notificaciones por email al cambiar contraseña
- [ ] 2FA (autenticación de dos factores)
- [ ] Bloqueo temporal tras X intentos fallidos
- [ ] Logs detallados de todas las operaciones
- [ ] Exportar lista de users a CSV/Excel
- [ ] Importación masiva de users
- [ ] Roles personalizados y permisos granulares

## 📝 Notas

- **Activo vs Eliminado**: Son campos independientes
  - `activo=false`: Usuario suspendido temporalmente
  - `deleted=true`: Usuario eliminado (papelera)
- **Email único**: Solo entre users activos (deleted=false)
- **Sanitización**: Todos los datos se sanitizan antes de guardar
- **Auditoría**: Se registra quién y cuándo elimina/modifica
