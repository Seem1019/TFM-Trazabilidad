-- =============================================================================
-- Script de reparación para base de datos local
-- Ejecutar manualmente en PostgreSQL si Flyway no aplicó las migraciones
-- =============================================================================

-- 1. Verificar si la columna empresa_id ya existe en pallets
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'pallets'
        AND column_name = 'empresa_id'
    ) THEN
        -- Agregar columna empresa_id
        ALTER TABLE pallets ADD COLUMN empresa_id BIGINT;
        RAISE NOTICE 'Columna empresa_id agregada a pallets';

        -- Actualizar registros existentes con la primera empresa activa
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
        RAISE NOTICE 'Registros existentes actualizados con empresa_id';

        -- Hacer la columna NOT NULL
        ALTER TABLE pallets ALTER COLUMN empresa_id SET NOT NULL;
        RAISE NOTICE 'Columna empresa_id marcada como NOT NULL';

        -- Agregar foreign key si no existe
        IF NOT EXISTS (
            SELECT 1 FROM pg_constraint WHERE conname = 'fk_pallet_empresa'
        ) THEN
            ALTER TABLE pallets
            ADD CONSTRAINT fk_pallet_empresa
            FOREIGN KEY (empresa_id) REFERENCES empresas(id);
            RAISE NOTICE 'Foreign key fk_pallet_empresa agregada';
        END IF;

        -- Crear índices si no existen
        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_pallet_empresa') THEN
            CREATE INDEX idx_pallet_empresa ON pallets(empresa_id);
            RAISE NOTICE 'Índice idx_pallet_empresa creado';
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_pallet_empresa_activo') THEN
            CREATE INDEX idx_pallet_empresa_activo ON pallets(empresa_id, activo);
            RAISE NOTICE 'Índice idx_pallet_empresa_activo creado';
        END IF;

        IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_pallet_empresa_estado') THEN
            CREATE INDEX idx_pallet_empresa_estado ON pallets(empresa_id, estado_pallet);
            RAISE NOTICE 'Índice idx_pallet_empresa_estado creado';
        END IF;

    ELSE
        RAISE NOTICE 'La columna empresa_id ya existe en pallets. No se requieren cambios.';
    END IF;
END $$;

-- 2. Verificar/crear tabla flyway_schema_history si no existe
-- y marcar V2 como aplicada
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'flyway_schema_history') THEN
        -- Verificar si V2 está registrada
        IF NOT EXISTS (SELECT 1 FROM flyway_schema_history WHERE version = '2') THEN
            INSERT INTO flyway_schema_history (
                installed_rank, version, description, type, script,
                checksum, installed_by, installed_on, execution_time, success
            )
            VALUES (
                (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history),
                '2', 'add empresa id to pallets', 'SQL',
                'V2__add_empresa_id_to_pallets.sql',
                NULL, current_user, now(), 0, true
            );
            RAISE NOTICE 'Migración V2 registrada en flyway_schema_history';
        ELSE
            RAISE NOTICE 'Migración V2 ya está registrada en flyway_schema_history';
        END IF;
    ELSE
        RAISE NOTICE 'Tabla flyway_schema_history no existe. Flyway la creará al iniciar.';
    END IF;
END $$;

-- 3. Verificar el resultado
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'pallets' AND column_name = 'empresa_id';

SELECT * FROM flyway_schema_history ORDER BY installed_rank;
