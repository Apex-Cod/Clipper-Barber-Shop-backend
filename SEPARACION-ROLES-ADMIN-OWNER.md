# Separación de Roles: ADMIN vs OWNER

## 📋 Resumen de Cambios

Se ha corregido la lógica de permisos en el módulo de gestión de usuarios para separar correctamente los roles **ADMIN** y **OWNER**.

## 🎯 Reglas de Negocio

### ADMIN (Super Administrador)
- ✅ Acceso **TOTAL** a todos los usuarios del sistema
- ✅ Puede listar, ver, modificar y eliminar **cualquier usuario**
- ✅ Puede gestionar usuarios de **cualquier empresa**
- ✅ Puede gestionar **clientes** (usuarios sin empresa)
- ✅ Puede gestionar **empleados** de cualquier empresa

### OWNER (Dueño de Empresa)
- ⚠️ Acceso **LIMITADO** solo a usuarios de su empresa
- ✅ Puede listar, ver, modificar y eliminar **solo empleados de su empresa**
- ❌ **NO** puede acceder a clientes (usuarios sin empresa)
- ❌ **NO** puede acceder a empleados de otras empresas
- ⚠️ Debe tener una empresa asociada

### Clientes
- Los clientes **NO tienen empresa asociada** (`empresa_id = null`)
- Solo **ADMIN** puede gestionar clientes
- Los **OWNER** nunca tienen acceso a clientes

## 🔧 Cambios Implementados

### 1. Controller (`UserManagementController.java`)

#### Endpoints Modificados:

**GET `/api/users/{id}`** - Obtener usuario
```java
// Antes: @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
// Ahora: Añadido validación por empresa en el servicio
- ADMIN: Puede ver cualquier usuario
- OWNER: Solo usuarios de su empresa
```

**GET `/api/users/empresa/{empresaId}`** - Listar usuarios por empresa
```java
// Ahora valida que OWNER solo pueda acceder a su propia empresa
- ADMIN: Cualquier empresa
- OWNER: Solo su empresa
```

**GET `/api/users`** - Listar todos los usuarios
```java
// Antes: @PreAuthorize("hasRole('OWNER')")
// Ahora: @PreAuthorize("hasRole('ADMIN')")
// Solo ADMIN tiene acceso
```

**GET `/api/users/deleted`** - Listar usuarios eliminados
```java
// ADMIN: Puede ver eliminados de todas las empresas
// OWNER: Solo eliminados de su empresa
```

**PUT `/api/users/{id}`** - Actualizar usuario
```java
// Añadida validación de permisos
- ADMIN: Puede actualizar cualquier usuario
- OWNER: Solo usuarios de su empresa
```

**PATCH `/api/users/{id}/estado`** - Cambiar estado
```java
// Añadida validación de permisos
- ADMIN: Puede cambiar estado de cualquier usuario
- OWNER: Solo usuarios de su empresa
```

**DELETE `/api/users/{id}`** - Eliminar usuario
```java
// Añadida validación de permisos
- ADMIN: Puede eliminar cualquier usuario
- OWNER: Solo usuarios de su empresa
```

**POST `/api/users/{id}/restore`** - Restaurar usuario
```java
// Añadida validación de permisos
- ADMIN: Puede restaurar cualquier usuario
- OWNER: Solo usuarios de su empresa
```

### 2. Use Case Interface (`UserManagementUseCase.java`)

Todos los métodos relevantes ahora reciben el parámetro `requestedBy` para validar permisos:

```java
UsuarioResponse obtenerUsuario(String userId, String requestedBy);
List<UsuarioResponse> listarUsuariosPorEmpresa(Long empresaId, String requestedBy);
List<UsuarioResponse> listarUsuariosEliminados(Long empresaId, String requestedBy);
UsuarioResponse restaurarUsuario(String userId, String restoredBy);
```

### 3. Service (`UserManagementService.java`)

#### Nuevos Métodos de Validación:

**`validarAccesoUsuario(String solicitanteId, Usuario usuarioObjetivo)`**
```java
// Valida que el solicitante tenga acceso al usuario objetivo
- ADMIN: Acceso total
- OWNER: Solo si el usuario objetivo:
  1. Tiene empresa asociada (es empleado)
  2. Es de la misma empresa que el OWNER
```

**`validarAccesoEmpresa(String solicitanteId, Long empresaId)`**
```java
// Valida que el solicitante tenga acceso a una empresa
- ADMIN: Acceso a todas las empresas
- OWNER: Solo a su propia empresa
```

#### Métodos Actualizados:

Todos estos métodos ahora validan permisos:
- ✅ `obtenerUsuario()` - Valida acceso al usuario
- ✅ `listarUsuariosPorEmpresa()` - Valida acceso a la empresa
- ✅ `listarUsuariosEliminados()` - Valida acceso según rol
- ✅ `actualizarUsuario()` - Valida acceso al usuario
- ✅ `cambiarEstado()` - Valida acceso al usuario
- ✅ `eliminarUsuario()` - Valida acceso al usuario
- ✅ `restaurarUsuario()` - Valida acceso al usuario

## 🔒 Validaciones de Seguridad

### Nivel 1: Spring Security (`@PreAuthorize`)
```java
@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')") // ADMIN u OWNER
```

### Nivel 2: Validación de Negocio (Servicio)
```java
validarAccesoUsuario(solicitanteId, usuario);
validarAccesoEmpresa(solicitanteId, empresaId);
```

## 📊 Casos de Uso

### Ejemplo 1: OWNER intenta ver un cliente
```
❌ OWNER no puede acceder a clientes
Razón: Los clientes no tienen empresa asociada
Error: "No tiene permisos para acceder a este usuario"
```

### Ejemplo 2: OWNER intenta ver empleado de otra empresa
```
❌ OWNER no puede acceder a empleados de otras empresas
Error: "No tiene permisos para acceder a este usuario"
```

### Ejemplo 3: OWNER lista usuarios de su empresa
```
✅ OWNER puede listar empleados de su empresa
Resultado: Lista de empleados de su empresa
```

### Ejemplo 4: ADMIN lista todos los usuarios
```
✅ ADMIN puede listar todos los usuarios
Resultado: Todos los usuarios del sistema (clientes + empleados)
```

### Ejemplo 5: OWNER intenta listar usuarios eliminados sin especificar empresa
```
✅ Se fuerza el filtro por su empresa automáticamente
Resultado: Usuarios eliminados de su empresa
```

### Ejemplo 6: ADMIN lista usuarios eliminados sin especificar empresa
```
✅ ADMIN puede ver todos los eliminados
Resultado: Todos los usuarios eliminados del sistema
```

## ⚠️ Consideraciones Importantes

1. **OWNER siempre debe tener empresa asociada**
   - Si un OWNER no tiene empresa, las operaciones fallarán
   - Mensaje: "OWNER debe tener una empresa asociada"

2. **Clientes nunca tienen empresa**
   - `empresa_id = null` para clientes
   - Solo ADMIN puede gestionarlos

3. **Empleados siempre tienen empresa**
   - `empresa_id != null` para empleados
   - OWNER puede gestionar empleados de su empresa

4. **Doble capa de seguridad**
   - Primera capa: Spring Security (`@PreAuthorize`)
   - Segunda capa: Validación de negocio en el servicio

## 🧪 Pruebas Recomendadas

### Como ADMIN:
- [ ] Listar todos los usuarios
- [ ] Ver un cliente
- [ ] Ver un empleado de cualquier empresa
- [ ] Modificar un cliente
- [ ] Modificar un empleado de cualquier empresa

### Como OWNER:
- [ ] Listar usuarios de mi empresa
- [ ] Ver un empleado de mi empresa
- [ ] Intentar ver un cliente (debe fallar)
- [ ] Intentar ver empleado de otra empresa (debe fallar)
- [ ] Modificar empleado de mi empresa
- [ ] Intentar modificar empleado de otra empresa (debe fallar)

## 📝 Notas Finales

- Los cambios son **retrocompatibles** con el resto del sistema
- Se mantiene el **soft delete** en todas las operaciones
- Las validaciones de **email único** siguen funcionando
- El endpoint `/me` funciona para **todos los usuarios**
