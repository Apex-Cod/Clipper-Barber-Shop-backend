-- =====================================================
-- Migración: Agregar campo recordatorio_enviado a reservas
-- Fecha: 2025-11-14
-- Descripción: Agrega campo booleano para controlar envío de recordatorios
-- y evitar duplicados en el sistema de notificaciones por email
-- =====================================================

-- Agregar columna recordatorio_enviado a la tabla reservas
ALTER TABLE reservas 
ADD COLUMN IF NOT EXISTS recordatorio_enviado BOOLEAN NOT NULL DEFAULT FALSE;

-- Comentario de la columna para documentación
COMMENT ON COLUMN reservas.recordatorio_enviado IS 
'Indica si ya se envió el recordatorio por email 15 minutos antes de la reserva. Evita envíos duplicados.';

-- Crear índice para mejorar el rendimiento de consultas de la tarea programada
CREATE INDEX IF NOT EXISTS idx_reservas_recordatorio_fecha 
ON reservas(recordatorio_enviado, reservation_date, status, deleted)
WHERE deleted = false AND recordatorio_enviado = false;

-- Verificar que la columna se agregó correctamente
SELECT 
    column_name, 
    data_type, 
    column_default, 
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'reservas' 
  AND column_name = 'recordatorio_enviado';

-- Mostrar estadísticas actuales
SELECT 
    COUNT(*) as total_reservas,
    COUNT(*) FILTER (WHERE recordatorio_enviado = true) as con_recordatorio,
    COUNT(*) FILTER (WHERE recordatorio_enviado = false) as sin_recordatorio
FROM reservas
WHERE deleted = false;

-- Fin de la migración
