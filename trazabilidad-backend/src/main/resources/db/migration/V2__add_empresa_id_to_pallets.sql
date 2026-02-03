-- =============================================================================
-- V2__add_empresa_id_to_pallets.sql
-- Agrega aislamiento multitenant a la tabla pallets
-- Esta migración es IDEMPOTENTE - puede ejecutarse en BD nuevas o existentes
-- =============================================================================

-- 1. Agregar columna empresa_id solo si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'pallets' AND column_name = 'empresa_id'
    ) THEN
        ALTER TABLE pallets ADD COLUMN empresa_id BIGINT;
        RAISE NOTICE 'Columna empresa_id agregada a pallets';
    END IF;
END $$;

-- 2. Actualizar registros existentes que tengan empresa_id NULL
UPDATE pallets
SET empresa_id = (
    SELECT COALESCE(
        (SELECT u.empresa_id
         FROM envios e
         JOIN usuarios u ON e.usuario_id = u.id
         WHERE e.id = pallets.envio_id),
        (SELECT MIN(id) FROM empresas WHERE activo = true)
    )
)
WHERE empresa_id IS NULL;

-- 3. Hacer la columna NOT NULL (solo si no lo es ya)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'pallets'
        AND column_name = 'empresa_id'
        AND is_nullable = 'YES'
    ) THEN
        ALTER TABLE pallets ALTER COLUMN empresa_id SET NOT NULL;
        RAISE NOTICE 'Columna empresa_id marcada como NOT NULL';
    END IF;
END $$;

-- 4. Agregar la foreign key constraint si no existe
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_pallet_empresa'
    ) THEN
        ALTER TABLE pallets ADD CONSTRAINT fk_pallet_empresa
        FOREIGN KEY (empresa_id) REFERENCES empresas(id);
        RAISE NOTICE 'FK fk_pallet_empresa agregada';
    END IF;
END $$;

-- 5. Crear índices si no existen
CREATE INDEX IF NOT EXISTS idx_pallet_empresa ON pallets(empresa_id);
CREATE INDEX IF NOT EXISTS idx_pallet_empresa_activo ON pallets(empresa_id, activo);
CREATE INDEX IF NOT EXISTS idx_pallet_empresa_estado ON pallets(empresa_id, estado_pallet);
