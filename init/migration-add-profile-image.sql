-- Migración: Agregar campo de imagen de perfil a usuarios
-- Fecha: 2025-11-14
-- Descripción: Agrega columna profile_image_url para almacenar la URL de la imagen de perfil del usuario en Supabase

-- Agregar columna para URL de imagen de perfil (opcional)
ALTER TABLE usuarios 
ADD COLUMN IF NOT EXISTS profile_image_url VARCHAR(500);

-- Comentario de la columna
COMMENT ON COLUMN usuarios.profile_image_url IS 'URL de la imagen de perfil del usuario almacenada en Supabase Storage (bucket: Clipper-User). Campo opcional.';

-- Verificar la migración
SELECT 
    column_name, 
    data_type, 
    character_maximum_length, 
    is_nullable
FROM information_schema.columns
WHERE table_name = 'usuarios' 
  AND column_name = 'profile_image_url';
