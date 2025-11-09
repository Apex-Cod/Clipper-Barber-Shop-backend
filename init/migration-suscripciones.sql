-- ===================================================================
-- MIGRACIÓN: Sistema de Suscripciones
-- Integra planes con pagos para validar suscripciones activas
-- ===================================================================

-- 1. Actualizar tabla suscripciones (ya existe pero hay que mejorarla)
ALTER TABLE suscripciones 
ADD COLUMN IF NOT EXISTS monto_pagado DECIMAL(10,2),
ADD COLUMN IF NOT EXISTS fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN IF NOT EXISTS fecha_activacion TIMESTAMP,
ADD COLUMN IF NOT EXISTS fecha_cancelacion TIMESTAMP,
ADD COLUMN IF NOT EXISTS auto_renovar BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS notas VARCHAR(500),
ADD COLUMN IF NOT EXISTS deleted BOOLEAN DEFAULT FALSE;

-- 2. Modificar columna estado para usar ENUM
ALTER TABLE suscripciones 
ALTER COLUMN estado TYPE VARCHAR(20),
ALTER COLUMN estado SET DEFAULT 'PENDIENTE_PAGO';

-- 3. Añadir índices para mejorar rendimiento
CREATE INDEX IF NOT EXISTS idx_suscripciones_empresa_estado 
ON suscripciones(empresa_id, estado) WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_suscripciones_fecha_fin 
ON suscripciones(fecha_fin) WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_suscripciones_estado 
ON suscripciones(estado) WHERE deleted = FALSE;

-- 4. Actualizar tabla pagos para agregar relación con suscripción (si no existe)
ALTER TABLE pagos 
ADD COLUMN IF NOT EXISTS suscripcion_id BIGINT,
ADD CONSTRAINT IF NOT EXISTS fk_pagos_suscripcion 
    FOREIGN KEY (suscripcion_id) REFERENCES suscripciones(id);

-- 5. Añadir comentarios para documentación
COMMENT ON TABLE suscripciones IS 'Almacena las suscripciones de empresas a planes. Una empresa puede tener múltiples suscripciones en su historial, pero solo una ACTIVA a la vez.';
COMMENT ON COLUMN suscripciones.estado IS 'Estados: ACTIVA, EXPIRADA, CANCELADA, PENDIENTE_PAGO';
COMMENT ON COLUMN suscripciones.monto_pagado IS 'Monto total pagado por la suscripción';
COMMENT ON COLUMN suscripciones.fecha_activacion IS 'Fecha en que se activó la suscripción tras el pago';
COMMENT ON COLUMN suscripciones.auto_renovar IS 'Si es TRUE, la suscripción se renueva automáticamente al vencer';
COMMENT ON COLUMN suscripciones.deleted IS 'Borrado lógico';

-- 6. Actualizar suscripciones existentes con valores por defecto
UPDATE suscripciones 
SET fecha_creacion = CURRENT_TIMESTAMP 
WHERE fecha_creacion IS NULL;

UPDATE suscripciones 
SET estado = 'ACTIVA' 
WHERE estado IS NULL OR estado = '';

UPDATE suscripciones 
SET deleted = FALSE 
WHERE deleted IS NULL;

-- 7. Vista para consultar suscripciones activas con información completa
CREATE OR REPLACE VIEW v_suscripciones_activas AS
SELECT 
    s.id AS suscripcion_id,
    s.empresa_id,
    e.nombre AS empresa_nombre,
    s.plan_id,
    p.nombre AS plan_nombre,
    p.tipo AS plan_tipo,
    s.fecha_inicio,
    s.fecha_fin,
    s.estado,
    s.monto_pagado,
    s.auto_renovar,
    CURRENT_DATE - s.fecha_fin AS dias_restantes,
    CASE 
        WHEN s.fecha_fin < CURRENT_DATE THEN TRUE 
        ELSE FALSE 
    END AS esta_expirada
FROM suscripciones s
INNER JOIN empresas e ON s.empresa_id = e.id
INNER JOIN planes p ON s.plan_id = p.id
WHERE s.deleted = FALSE 
  AND s.estado = 'ACTIVA';

-- 8. Función para marcar suscripciones expiradas automáticamente
CREATE OR REPLACE FUNCTION marcar_suscripciones_expiradas()
RETURNS INTEGER AS $$
DECLARE
    total_expiradas INTEGER;
BEGIN
    -- Marcar como EXPIRADA las suscripciones activas cuya fecha_fin ya pasó
    UPDATE suscripciones
    SET estado = 'EXPIRADA'
    WHERE estado = 'ACTIVA'
      AND fecha_fin < CURRENT_DATE
      AND deleted = FALSE;
    
    GET DIAGNOSTICS total_expiradas = ROW_COUNT;
    
    RETURN total_expiradas;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION marcar_suscripciones_expiradas() IS 'Marca como EXPIRADA las suscripciones activas que ya pasaron su fecha de fin. Retorna el número de suscripciones actualizadas. Debe ejecutarse diariamente mediante un job programado.';

-- 9. Verificar integridad de datos
DO $$
BEGIN
    -- Verificar que no hay empresas con múltiples suscripciones ACTIVAS
    IF EXISTS (
        SELECT empresa_id 
        FROM suscripciones 
        WHERE estado = 'ACTIVA' AND deleted = FALSE
        GROUP BY empresa_id 
        HAVING COUNT(*) > 1
    ) THEN
        RAISE NOTICE 'ADVERTENCIA: Existen empresas con múltiples suscripciones activas. Por favor, revisar manualmente.';
    END IF;
END $$;

-- 10. Grants (ajustar según roles de tu base de datos)
-- GRANT SELECT, INSERT, UPDATE ON suscripciones TO app_user;
-- GRANT SELECT ON v_suscripciones_activas TO app_user;
-- GRANT EXECUTE ON FUNCTION marcar_suscripciones_expiradas() TO app_user;

-- ===================================================================
-- FIN DE MIGRACIÓN
-- ===================================================================

-- Para ejecutar la función de expiración manual:
-- SELECT marcar_suscripciones_expiradas();

-- Para ver suscripciones activas:
-- SELECT * FROM v_suscripciones_activas;

-- Para ver suscripciones próximas a vencer (7 días):
-- SELECT * FROM v_suscripciones_activas WHERE dias_restantes <= 7;
