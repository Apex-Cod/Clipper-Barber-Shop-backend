# Módulo de Gestión de Usuarios

## 📋 Descripción

Módulo CRUD completo para la gestión de usuarios del sistema Clipper Barber Shop. Incluye operaciones de lectura, actualización, cambio de contraseña, cambio de estado (activo/inactivo) y eliminación lógica (soft delete).

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

### 1. Consultar Usuarios

#### Obtener Usuario por ID
```http
GET /api/usuarios/{id}
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
GET /api/usuarios/me
Authorization: Bearer {token}
```

#### Listar Usuarios por Empresa
```http
GET /api/usuarios/empresa/{empresaId}
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

#### Listar Todos los Usuarios
```http
GET /api/usuarios
Authorization: Bearer {token}
Roles: OWNER
```

#### Listar Usuarios Eliminados (Papelera)
```http
GET /api/usuarios/deleted?empresaId={empresaId}
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

### 2. Actualizar Usuario

#### Actualizar Información
```http
PUT /api/usuarios/{id}
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
PUT /api/usuarios/me
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
PUT /api/usuarios/{id}/password
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
PUT /api/usuarios/me/password
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
PATCH /api/usuarios/{id}/estado
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
PATCH /api/usuarios/{id}/activar
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

#### Desactivar Usuario
```http
PATCH /api/usuarios/{id}/desactivar
Authorization: Bearer {token}
Roles: OWNER, ADMIN
```

**Diferencia entre Desactivar y Eliminar:**
- **Desactivar**: El usuario no puede hacer login pero sigue visible en el sistema
- **Eliminar**: Soft delete, el usuario va a la papelera

### 5. Eliminar y Restaurar

#### Eliminar Usuario (Soft Delete)
```http
DELETE /api/usuarios/{id}
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
POST /api/usuarios/{id}/restore
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
| GET /usuarios/{id} | ✅ | ✅ | ❌ | ❌ |
| GET /usuarios/me | ✅ | ✅ | ✅ | ✅ |
| GET /usuarios/empresa/{id} | ✅ | ✅ | ❌ | ❌ |
| GET /usuarios | ✅ | ❌ | ❌ | ❌ |
| GET /usuarios/deleted | ✅ | ✅ | ❌ | ❌ |
| PUT /usuarios/{id} | ✅ | ✅ | ❌ | ❌ |
| PUT /usuarios/me | ✅ | ✅ | ✅ | ✅ |
| PUT /usuarios/{id}/password | ✅* | ❌ | ✅* | ✅* |
| PUT /usuarios/me/password | ✅ | ✅ | ✅ | ✅ |
| PATCH /usuarios/{id}/estado | ✅ | ✅ | ❌ | ❌ |
| DELETE /usuarios/{id} | ✅ | ✅ | ❌ | ❌ |
| POST /usuarios/{id}/restore | ✅ | ✅ | ❌ | ❌ |

\* Solo puede cambiar su propia contraseña

## 🎯 Casos de Uso

### Caso 1: Empleado se registra y luego actualiza su perfil
```bash
# 1. Admin registra empleado (módulo register)
POST /api/registro/empleado

# 2. Empleado hace login
POST /api/auth/login

# 3. Empleado actualiza su perfil
PUT /api/usuarios/me

# 4. Empleado cambia su contraseña
PUT /api/usuarios/me/password
```

### Caso 2: Admin desactiva temporalmente un empleado
```bash
# 1. Admin desactiva el empleado
PATCH /api/usuarios/{id}/desactivar

# 2. Empleado intenta hacer login
POST /api/auth/login
# ❌ Error: "Usuario inactivo. Contacte al administrador"

# 3. Admin reactiva el empleado
PATCH /api/usuarios/{id}/activar

# 4. Empleado puede hacer login nuevamente
POST /api/auth/login
# ✅ Exitoso
```

### Caso 3: Eliminación y restauración de usuario
```bash
# 1. Admin elimina empleado (soft delete)
DELETE /api/usuarios/{id}

# 2. Ver usuarios eliminados
GET /api/usuarios/deleted

# 3. Restaurar empleado
POST /api/usuarios/{id}/restore

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

### Nuevos Campos en `usuarios`
```sql
activo BOOLEAN NOT NULL DEFAULT TRUE
```

### Consultas Optimizadas
```sql
-- Solo usuarios activos y no eliminados
SELECT * FROM usuarios 
WHERE activo = TRUE AND deleted = FALSE;

-- Usuarios de una empresa activos
SELECT * FROM usuarios 
WHERE empresa_id = ? AND activo = TRUE AND deleted = FALSE;

-- Usuarios eliminados de una empresa
SELECT * FROM usuarios 
WHERE empresa_id = ? AND deleted = TRUE;
```

### Índices Recomendados
```sql
CREATE INDEX idx_usuarios_activo ON usuarios(activo);
CREATE INDEX idx_usuarios_activo_deleted ON usuarios(activo, deleted);
CREATE INDEX idx_usuarios_empresa_activo ON usuarios(empresa_id, activo, deleted);
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
- [ ] Exportar lista de usuarios a CSV/Excel
- [ ] Importación masiva de usuarios
- [ ] Roles personalizados y permisos granulares

## 📝 Notas

- **Activo vs Eliminado**: Son campos independientes
  - `activo=false`: Usuario suspendido temporalmente
  - `deleted=true`: Usuario eliminado (papelera)
- **Email único**: Solo entre usuarios activos (deleted=false)
- **Sanitización**: Todos los datos se sanitizan antes de guardar
- **Auditoría**: Se registra quién y cuándo elimina/modifica
