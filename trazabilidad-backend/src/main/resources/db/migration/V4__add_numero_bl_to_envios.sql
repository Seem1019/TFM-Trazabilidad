-- =============================================================================
-- V4__add_numero_bl_to_envios.sql
-- Agrega columna numero_bl a la tabla envios si no existe
-- Esta migración es IDEMPOTENTE - segura para ejecutar en cualquier entorno
-- =============================================================================

-- Agregar columna numero_bl solo si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'envios' AND column_name = 'numero_bl'
    ) THEN
        ALTER TABLE envios ADD COLUMN numero_bl VARCHAR(100);
        RAISE NOTICE 'Columna numero_bl agregada a envios';
    ELSE
        RAISE NOTICE 'Columna numero_bl ya existe en envios';
    END IF;
END $$;
