-- =============================================================================
-- V3__fix_column_typo.sql
-- Corrige typo en nombre de columna: dosiso_cantidad -> dosis_cantidad
-- =============================================================================

-- Renombrar columna solo si existe con el nombre incorrecto
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'actividades_agronomicas'
        AND column_name = 'dosiso_cantidad'
    ) THEN
        ALTER TABLE actividades_agronomicas
        RENAME COLUMN dosiso_cantidad TO dosis_cantidad;
        RAISE NOTICE 'Columna renombrada: dosiso_cantidad -> dosis_cantidad';
    END IF;
END $$;
