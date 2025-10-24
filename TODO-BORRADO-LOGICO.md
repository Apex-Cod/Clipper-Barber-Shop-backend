# 📋 Checklist de Implementación - Borrado Lógico

## ✅ Completado

- [x] Crear clase base `SoftDeletableEntity`
- [x] Actualizar entidad `Usuario`
- [x] Actualizar entidad `Empresa`
- [x] Actualizar entidad `Servicio`
- [x] Actualizar entidad `Reserva`
- [x] Actualizar entidad `Promocion`
- [x] Actualizar entidad `Disponibilidad`
- [x] Actualizar entidad `Plan`
- [x] Actualizar entidad `Resenia`
- [x] Crear script SQL de migración
- [x] Crear documentación completa
- [x] Cambiar `@Builder` a `@SuperBuilder` en entidades

## 🔄 En Progreso / Pendiente

### 1. Actualizar Repositorios (CRÍTICO)

#### Usuario
- [ ] `UsuarioRepositoryPort.findByEmail()` → `findByEmailAndDeletedFalse()`
- [ ] Agregar `findByDeletedFalse()`
- [ ] Agregar `findByEmpresaAndDeletedFalse(Empresa empresa)`
- [ ] Agregar `findByDeletedTrue()` (para papelera)

#### Empresa
- [ ] `EmpresaRepositoryPort.findAll()` → considerar filtro por deleted
- [ ] Agregar `findByDeletedFalse()`
- [ ] Agregar `findByDeletedTrue()`

#### Otros Repositorios
- [ ] Revisar todos los `find*` en repositorios
- [ ] Agregar filtro `AndDeletedFalse` donde corresponda

### 2. Actualizar Servicios

#### RegistroServiceImpl
- [ ] Verificar que `findByEmail()` use `findByEmailAndDeletedFalse()`
- [ ] Agregar validación: no registrar con email de usuario eliminado

#### AuthService
- [ ] **CRÍTICO**: Actualizar autenticación para filtrar usuarios eliminados
- [ ] Cambiar a `findByEmailAndDeletedFalse()` en login

#### Nuevos Métodos en Servicios
- [ ] `UsuarioService.eliminarUsuario(id, deletedBy)`
- [ ] `UsuarioService.restaurarUsuario(id)`
- [ ] `UsuarioService.listarUsuariosEliminados()`
- [ ] `EmpresaService.eliminarEmpresa(id, deletedBy)` (con cascada a usuarios)
- [ ] `ServicioService.eliminarServicio(id, deletedBy)`
- [ ] `ReservaService.cancelarReserva(id, deletedBy)`

### 3. Actualizar Controladores

#### Nuevos Endpoints Sugeridos
```java
// UsuarioController
- [ ] DELETE /api/usuarios/{id} - Soft delete
- [ ] POST /api/usuarios/{id}/restore - Restaurar
- [ ] GET /api/usuarios/deleted - Listar eliminados

// EmpresaController  
- [ ] DELETE /api/empresas/{id}
- [ ] POST /api/empresas/{id}/restore
- [ ] GET /api/empresas/deleted

// ServicioController
- [ ] DELETE /api/servicios/{id}
- [ ] POST /api/servicios/{id}/restore

// ReservaController
- [ ] DELETE /api/reservas/{id}
- [ ] POST /api/reservas/{id}/restore
```

### 4. Migración de Base de Datos

- [ ] **Ejecutar script**: `init/migration-soft-delete.sql`
- [ ] Verificar que las columnas se crearon correctamente
- [ ] Verificar índices creados
- [ ] Verificar migración de datos antiguos (activo, activa, disponible)
- [ ] Probar rollback del script (en ambiente de desarrollo)

### 5. Validaciones y Seguridad

- [ ] Validar permisos: Solo OWNER puede eliminar/restaurar
- [ ] Agregar validación: No eliminar usuario ya eliminado
- [ ] Agregar validación: No restaurar usuario activo
- [ ] Logging de todas las eliminaciones y restauraciones

### 6. Testing

#### Tests Unitarios
- [ ] Test: `softDelete()` marca campos correctamente
- [ ] Test: `restore()` limpia campos correctamente
- [ ] Test: `isActive()` retorna valor correcto
- [ ] Test: Repositorio filtra por deleted=false

#### Tests de Integración
- [ ] Test: Eliminar usuario y verificar que no pueda hacer login
- [ ] Test: Restaurar usuario y verificar que pueda hacer login
- [ ] Test: Eliminar empresa elimina usuarios en cascada
- [ ] Test: No se puede registrar con email de usuario eliminado

### 7. Solución de Email Único

Elegir una de las dos opciones:

#### Opción A: Modificar Email al Eliminar
- [ ] Sobrescribir `softDelete()` en Usuario
- [ ] Agregar timestamp al email
- [ ] Actualizar tests

#### Opción B: Índice Único Parcial
- [ ] Eliminar constraint único actual del email
- [ ] Crear índice único parcial (solo deleted=false)
- [ ] Actualizar documentación

### 8. Documentación Frontend

- [ ] Documentar endpoints de eliminación
- [ ] Documentar endpoints de restauración
- [ ] Documentar estructura de respuestas
- [ ] Crear mockups de "papelera" en UI

### 9. Features Opcionales

- [ ] Panel de administración: Vista de papelera
- [ ] Filtro de fecha: Ver eliminados en último mes
- [ ] Búsqueda en papelera
- [ ] Eliminación masiva (restaurar varios a la vez)
- [ ] Tarea programada: Limpieza de registros antiguos (>5 años)
- [ ] Notificación al eliminar: ¿Está seguro?
- [ ] Historial de cambios: Quién eliminó/restauró y cuándo

### 10. Optimizaciones

- [ ] Analizar performance con tablas grandes
- [ ] Considerar particionamiento por `deleted`
- [ ] Índices compuestos según consultas frecuentes
- [ ] Cache de queries frecuentes

## 🎯 Prioridad Alta (Hacer Primero)

1. **Actualizar AuthService** - No permitir login de usuarios eliminados
2. **Actualizar repositorios** - Filtrar por deleted=false
3. **Ejecutar migración SQL** - Agregar columnas a BD
4. **Solucionar email único** - Elegir e implementar solución

## ⚠️ Notas Importantes

### Email Único - Decisión Requerida

**Problema**: Un email eliminado no puede reutilizarse

**Opción 1**: Modificar email al eliminar
```java
this.email = this.email + "_deleted_" + timestamp
```
- ✅ Pros: Simple, compatible con cualquier BD
- ❌ Contras: Email original se pierde visualmente

**Opción 2**: Índice único parcial (PostgreSQL)
```sql
CREATE UNIQUE INDEX ON usuarios(email) WHERE deleted = FALSE;
```
- ✅ Pros: Email original se mantiene
- ❌ Contras: Solo PostgreSQL 9.5+

**Decisión**: [ ] Opción 1  [ ] Opción 2

### Cascada en Eliminación de Empresa

Cuando se elimina una empresa:
- [ ] Eliminar usuarios asociados
- [ ] Eliminar servicios asociados
- [ ] Eliminar reservas asociadas
- [ ] ¿Eliminar promociones asociadas?
- [ ] ¿Eliminar disponibilidad asociada?

**Decisión requerida**: ¿Qué entidades se deben eliminar en cascada?

## 📊 Progreso

- Entidades actualizadas: 8/8 (100%)
- Repositorios actualizados: 0/? (Pendiente)
- Servicios actualizados: 0/? (Pendiente)
- Controladores actualizados: 0/? (Pendiente)
- Tests creados: 0/? (Pendiente)
- Migración BD: Pendiente

## 🚀 Siguiente Sesión de Desarrollo

1. Decidir sobre email único (Opción 1 o 2)
2. Actualizar `AuthService` y repositorios
3. Ejecutar migración SQL
4. Crear tests básicos
5. Probar flujo completo: eliminar → login falla → restaurar → login OK

---

**Última actualización**: Implementación de borrado lógico completada
**Próximo paso**: Actualizar repositorios y servicios
