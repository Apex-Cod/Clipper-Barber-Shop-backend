-- ============================================================================
-- Script de Migración: Implementación de Borrado Lógico (Soft Delete)
-- ============================================================================
-- Este script agrega las columnas necesarias para soportar borrado lógico
-- en las entidades del sistema Clipper Barber Shop
-- 
-- IMPORTANTE: Ejecutar en orden y verificar cada paso
-- ============================================================================

-- ============================================================================
-- PASO 1: Agregar columnas de soft delete a las tablas
-- ============================================================================

-- Tabla: usuarios
ALTER TABLE usuarios 
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- Tabla: empresas
ALTER TABLE empresas 
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- Tabla: servicios
ALTER TABLE servicios 
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- Tabla: reservas
ALTER TABLE reservas 
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- Tabla: promociones
ALTER TABLE promociones 
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- Tabla: disponibilidad
ALTER TABLE disponibilidad 
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- Tabla: planes
ALTER TABLE planes 
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- Tabla: resenias
ALTER TABLE resenias 
    ADD COLUMN IF NOT EXISTS deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_by VARCHAR(255);

-- ============================================================================
-- PASO 2: Migrar datos existentes
-- ============================================================================

-- Migrar servicios: deleted = inverso de activo
UPDATE servicios 
SET deleted = CASE 
    WHEN activo IS NULL OR activo = TRUE THEN FALSE 
    ELSE TRUE 
END
WHERE deleted = FALSE; -- Solo actualizar registros no migrados

-- Migrar promociones: deleted = inverso de activa
UPDATE promociones 
SET deleted = CASE 
    WHEN activa IS NULL OR activa = TRUE THEN FALSE 
    ELSE TRUE 
END
WHERE deleted = FALSE;

-- Migrar disponibilidad: deleted = inverso de disponible
UPDATE disponibilidad 
SET deleted = CASE 
    WHEN disponible IS NULL OR disponible = TRUE THEN FALSE 
    ELSE TRUE 
END
WHERE deleted = FALSE;

-- Migrar empresas: marcar como deleted si estado = 'INACTIVA'
UPDATE empresas 
SET deleted = TRUE,
    deleted_at = created_at  -- Usar created_at como aproximación
WHERE estado = 'INACTIVA' AND deleted = FALSE;

-- ============================================================================
-- PASO 3: Crear índices para mejorar el rendimiento
-- ============================================================================

-- Índices en la columna 'deleted' para filtros frecuentes
CREATE INDEX IF NOT EXISTS idx_usuarios_deleted ON usuarios(deleted);
CREATE INDEX IF NOT EXISTS idx_usuarios_deleted_at ON usuarios(deleted_at) WHERE deleted = TRUE;

CREATE INDEX IF NOT EXISTS idx_empresas_deleted ON empresas(deleted);
CREATE INDEX IF NOT EXISTS idx_empresas_deleted_at ON empresas(deleted_at) WHERE deleted = TRUE;

CREATE INDEX IF NOT EXISTS idx_servicios_deleted ON servicios(deleted);
CREATE INDEX IF NOT EXISTS idx_servicios_deleted_at ON servicios(deleted_at) WHERE deleted = TRUE;

CREATE INDEX IF NOT EXISTS idx_reservas_deleted ON reservas(deleted);
CREATE INDEX IF NOT EXISTS idx_reservas_deleted_at ON reservas(deleted_at) WHERE deleted = TRUE;

CREATE INDEX IF NOT EXISTS idx_promociones_deleted ON promociones(deleted);
CREATE INDEX IF NOT EXISTS idx_promociones_deleted_at ON promociones(deleted_at) WHERE deleted = TRUE;

CREATE INDEX IF NOT EXISTS idx_disponibilidad_deleted ON disponibilidad(deleted);

CREATE INDEX IF NOT EXISTS idx_planes_deleted ON planes(deleted);

CREATE INDEX IF NOT EXISTS idx_resenias_deleted ON resenias(deleted);

-- Índices compuestos para consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_usuarios_empresa_active 
    ON usuarios(empresa_id, deleted) 
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_usuarios_email_active 
    ON usuarios(email) 
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_reservas_client_active 
    ON reservas(client_id, deleted) 
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_reservas_employee_active 
    ON reservas(employee_id, deleted) 
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_servicios_empresa_active 
    ON servicios(empresa_id, deleted) 
    WHERE deleted = FALSE;

-- ============================================================================
-- PASO 4: Comentarios en columnas (documentación en BD)
-- ============================================================================

COMMENT ON COLUMN usuarios.deleted IS 'Indica si el usuario ha sido eliminado lógicamente';
COMMENT ON COLUMN usuarios.deleted_at IS 'Fecha y hora de eliminación lógica';
COMMENT ON COLUMN usuarios.deleted_by IS 'ID del usuario que realizó la eliminación';

COMMENT ON COLUMN empresas.deleted IS 'Indica si la empresa ha sido eliminada lógicamente';
COMMENT ON COLUMN empresas.deleted_at IS 'Fecha y hora de eliminación lógica';
COMMENT ON COLUMN empresas.deleted_by IS 'ID del usuario que realizó la eliminación';

COMMENT ON COLUMN servicios.deleted IS 'Indica si el servicio ha sido eliminado lógicamente';
COMMENT ON COLUMN servicios.deleted_at IS 'Fecha y hora de eliminación lógica';
COMMENT ON COLUMN servicios.deleted_by IS 'ID del usuario que realizó la eliminación';

COMMENT ON COLUMN reservas.deleted IS 'Indica si la reserva ha sido eliminada lógicamente';
COMMENT ON COLUMN reservas.deleted_at IS 'Fecha y hora de eliminación lógica';
COMMENT ON COLUMN reservas.deleted_by IS 'ID del usuario que realizó la eliminación';

-- ============================================================================
-- PASO 5: Verificación de la migración
-- ============================================================================

-- Verificar que las columnas existen
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM 
    information_schema.columns
WHERE 
    table_name IN ('usuarios', 'empresas', 'servicios', 'reservas', 'promociones', 'disponibilidad', 'planes', 'resenias')
    AND column_name IN ('deleted', 'deleted_at', 'deleted_by')
ORDER BY 
    table_name, ordinal_position;

-- Contar registros eliminados vs activos por tabla
SELECT 'usuarios' as tabla, 
       COUNT(*) FILTER (WHERE deleted = FALSE) as activos,
       COUNT(*) FILTER (WHERE deleted = TRUE) as eliminados,
       COUNT(*) as total
FROM usuarios
UNION ALL
SELECT 'empresas', 
       COUNT(*) FILTER (WHERE deleted = FALSE),
       COUNT(*) FILTER (WHERE deleted = TRUE),
       COUNT(*)
FROM empresas
UNION ALL
SELECT 'servicios', 
       COUNT(*) FILTER (WHERE deleted = FALSE),
       COUNT(*) FILTER (WHERE deleted = TRUE),
       COUNT(*)
FROM servicios
UNION ALL
SELECT 'reservas', 
       COUNT(*) FILTER (WHERE deleted = FALSE),
       COUNT(*) FILTER (WHERE deleted = TRUE),
       COUNT(*)
FROM reservas
UNION ALL
SELECT 'promociones', 
       COUNT(*) FILTER (WHERE deleted = FALSE),
       COUNT(*) FILTER (WHERE deleted = TRUE),
       COUNT(*)
FROM promociones;

-- Verificar índices creados
SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM 
    pg_indexes
WHERE 
    tablename IN ('usuarios', 'empresas', 'servicios', 'reservas', 'promociones', 'disponibilidad', 'planes', 'resenias')
    AND indexname LIKE '%deleted%'
ORDER BY 
    tablename, indexname;

-- ============================================================================
-- PASO 6: (OPCIONAL) Deprecar columnas antiguas
-- ============================================================================
-- Estas columnas ahora son reemplazadas por el campo 'deleted'
-- Se recomienda mantenerlas por un tiempo de transición antes de eliminarlas

-- COMENTAR estas columnas como deprecadas
COMMENT ON COLUMN servicios.activo IS 'DEPRECATED: Usar campo deleted de la clase base SoftDeletableEntity';
COMMENT ON COLUMN promociones.activa IS 'DEPRECATED: Usar campo deleted de la clase base SoftDeletableEntity';
COMMENT ON COLUMN disponibilidad.disponible IS 'DEPRECATED: Usar campo deleted de la clase base SoftDeletableEntity';

-- ============================================================================
-- ROLLBACK (en caso de necesitar revertir)
-- ============================================================================
-- ADVERTENCIA: Solo usar si necesitas revertir la migración completamente
-- Esto eliminará las columnas y los índices creados

/*
-- Eliminar índices
DROP INDEX IF EXISTS idx_usuarios_deleted;
DROP INDEX IF EXISTS idx_usuarios_deleted_at;
DROP INDEX IF EXISTS idx_empresas_deleted;
DROP INDEX IF EXISTS idx_empresas_deleted_at;
DROP INDEX IF EXISTS idx_servicios_deleted;
DROP INDEX IF EXISTS idx_servicios_deleted_at;
DROP INDEX IF EXISTS idx_reservas_deleted;
DROP INDEX IF EXISTS idx_reservas_deleted_at;
DROP INDEX IF EXISTS idx_promociones_deleted;
DROP INDEX IF EXISTS idx_promociones_deleted_at;
DROP INDEX IF EXISTS idx_disponibilidad_deleted;
DROP INDEX IF EXISTS idx_planes_deleted;
DROP INDEX IF EXISTS idx_resenias_deleted;
DROP INDEX IF EXISTS idx_usuarios_empresa_active;
DROP INDEX IF EXISTS idx_usuarios_email_active;
DROP INDEX IF EXISTS idx_reservas_client_active;
DROP INDEX IF EXISTS idx_reservas_employee_active;
DROP INDEX IF EXISTS idx_servicios_empresa_active;

-- Eliminar columnas
ALTER TABLE usuarios DROP COLUMN IF EXISTS deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
ALTER TABLE empresas DROP COLUMN IF EXISTS deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
ALTER TABLE servicios DROP COLUMN IF EXISTS deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
ALTER TABLE reservas DROP COLUMN IF EXISTS deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
ALTER TABLE promociones DROP COLUMN IF EXISTS deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
ALTER TABLE disponibilidad DROP COLUMN IF EXISTS deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
ALTER TABLE planes DROP COLUMN IF EXISTS deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
ALTER TABLE resenias DROP COLUMN IF EXISTS deleted, DROP COLUMN IF EXISTS deleted_at, DROP COLUMN IF EXISTS deleted_by;
*/

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================

-- Mensaje de finalización
DO $$
BEGIN
    RAISE NOTICE '============================================================================';
    RAISE NOTICE 'Migración de Borrado Lógico completada exitosamente';
    RAISE NOTICE '============================================================================';
    RAISE NOTICE 'Columnas agregadas: deleted, deleted_at, deleted_by';
    RAISE NOTICE 'Índices creados para mejorar el rendimiento';
    RAISE NOTICE 'Datos migrados desde campos antiguos (activo, activa, disponible)';
    RAISE NOTICE '';
    RAISE NOTICE 'PRÓXIMOS PASOS:';
    RAISE NOTICE '1. Verificar que la aplicación funcione correctamente';
    RAISE NOTICE '2. Actualizar todos los repositorios para filtrar por deleted=false';
    RAISE NOTICE '3. Implementar endpoints de restauración';
    RAISE NOTICE '4. Considerar eliminar columnas deprecadas en el futuro';
    RAISE NOTICE '============================================================================';
END $$;
