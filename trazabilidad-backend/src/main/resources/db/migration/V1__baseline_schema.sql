-- =============================================================================
-- V1__baseline_schema.sql
-- Esquema base del sistema de trazabilidad
-- Esta migración representa el estado inicial de la base de datos
-- En producción, Flyway usará baseline-on-migrate para saltarla si las tablas existen
-- =============================================================================

-- Tabla de empresas (multiempresa)
CREATE TABLE IF NOT EXISTS empresas (
    id BIGSERIAL PRIMARY KEY,
    nit VARCHAR(20) NOT NULL UNIQUE,
    razon_social VARCHAR(200) NOT NULL,
    nombre_comercial VARCHAR(200),
    email VARCHAR(100),
    telefono VARCHAR(20),
    direccion VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    telefono VARCHAR(20),
    empresa_id BIGINT NOT NULL,
    rol VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    ultimo_acceso TIMESTAMP,
    intentos_fallidos INTEGER DEFAULT 0,
    bloqueado_hasta TIMESTAMP,
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id)
);

-- Tabla de refresh tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    device_info VARCHAR(500),
    ip_address VARCHAR(50),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES usuarios(id)
);

-- Tabla de fincas
CREATE TABLE IF NOT EXISTS fincas (
    id BIGSERIAL PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    codigo_finca VARCHAR(50) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    ubicacion VARCHAR(500),
    municipio VARCHAR(200),
    departamento VARCHAR(100),
    pais VARCHAR(50),
    area_hectareas DOUBLE PRECISION,
    propietario VARCHAR(100),
    encargado VARCHAR(100),
    telefono VARCHAR(20),
    email VARCHAR(100),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_finca_empresa FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    CONSTRAINT uk_finca_codigo_empresa UNIQUE (codigo_finca, empresa_id)
);

-- Tabla de certificaciones
CREATE TABLE IF NOT EXISTS certificaciones (
    id BIGSERIAL PRIMARY KEY,
    finca_id BIGINT NOT NULL,
    tipo_certificacion VARCHAR(100) NOT NULL,
    entidad_emisora VARCHAR(200),
    numero_certificado VARCHAR(100),
    fecha_emision DATE,
    fecha_vencimiento DATE,
    estado VARCHAR(50) DEFAULT 'VIGENTE',
    url_documento VARCHAR(500),
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_certificacion_finca FOREIGN KEY (finca_id) REFERENCES fincas(id)
);

-- Tabla de lotes
CREATE TABLE IF NOT EXISTS lotes (
    id BIGSERIAL PRIMARY KEY,
    finca_id BIGINT NOT NULL,
    codigo_lote VARCHAR(50) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    tipo_fruta VARCHAR(100) NOT NULL,
    variedad VARCHAR(100),
    area_hectareas DOUBLE PRECISION,
    fecha_siembra DATE,
    fecha_primera_cosecha_estimada DATE,
    densidad_siembra INTEGER,
    ubicacion_interna VARCHAR(200),
    estado_lote VARCHAR(50) DEFAULT 'ACTIVO',
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_lote_finca FOREIGN KEY (finca_id) REFERENCES fincas(id),
    CONSTRAINT uk_lote_codigo_finca UNIQUE (codigo_lote, finca_id)
);

-- Tabla de actividades agronómicas
CREATE TABLE IF NOT EXISTS actividades_agronomicas (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    tipo_actividad VARCHAR(100) NOT NULL,
    fecha_actividad DATE NOT NULL,
    producto_aplicado VARCHAR(200),
    dosiso_cantidad VARCHAR(100),
    unidad_medida VARCHAR(50),
    metodo_aplicacion VARCHAR(100),
    responsable VARCHAR(150),
    numero_registro_producto VARCHAR(100),
    intervalo_seguridad_dias INTEGER,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_actividad_lote FOREIGN KEY (lote_id) REFERENCES lotes(id)
);

-- Tabla de cosechas
CREATE TABLE IF NOT EXISTS cosechas (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    fecha_cosecha DATE NOT NULL,
    cantidad_cosechada DOUBLE PRECISION NOT NULL,
    unidad_medida VARCHAR(50) NOT NULL DEFAULT 'kg',
    calidad_inicial VARCHAR(50),
    estado_fruta VARCHAR(100),
    responsable_cosecha VARCHAR(150),
    numero_trabajadores INTEGER,
    hora_inicio VARCHAR(10),
    hora_fin VARCHAR(10),
    temperatura_ambiente DOUBLE PRECISION,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_cosecha_lote FOREIGN KEY (lote_id) REFERENCES lotes(id)
);

-- Tabla de recepciones en planta
CREATE TABLE IF NOT EXISTS recepciones_planta (
    id BIGSERIAL PRIMARY KEY,
    lote_id BIGINT NOT NULL,
    codigo_recepcion VARCHAR(50) NOT NULL UNIQUE,
    fecha_recepcion DATE NOT NULL,
    hora_recepcion VARCHAR(10),
    cantidad_recibida DOUBLE PRECISION NOT NULL,
    unidad_medida VARCHAR(20) NOT NULL,
    temperatura_fruta DOUBLE PRECISION,
    estado_inicial VARCHAR(50),
    responsable_recepcion VARCHAR(150),
    vehiculo_transporte VARCHAR(50),
    conductor VARCHAR(100),
    observaciones TEXT,
    estado_recepcion VARCHAR(30) NOT NULL DEFAULT 'RECIBIDA',
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_recepcion_lote FOREIGN KEY (lote_id) REFERENCES lotes(id)
);

-- Tabla de clasificaciones
CREATE TABLE IF NOT EXISTS clasificaciones (
    id BIGSERIAL PRIMARY KEY,
    recepcion_id BIGINT NOT NULL,
    codigo_clasificacion VARCHAR(50) NOT NULL UNIQUE,
    fecha_clasificacion DATE NOT NULL,
    calidad VARCHAR(50) NOT NULL,
    cantidad_clasificada DOUBLE PRECISION NOT NULL,
    unidad_medida VARCHAR(20) NOT NULL,
    calibre VARCHAR(30),
    porcentaje_merma DOUBLE PRECISION,
    cantidad_merma DOUBLE PRECISION,
    motivo_merma TEXT,
    responsable_clasificacion VARCHAR(150),
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_clasificacion_recepcion FOREIGN KEY (recepcion_id) REFERENCES recepciones_planta(id)
);

-- Tabla de envíos (se crea antes de pallets por la FK)
CREATE TABLE IF NOT EXISTS envios (
    id BIGSERIAL PRIMARY KEY,
    codigo_envio VARCHAR(50) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATE NOT NULL,
    fecha_salida_estimada DATE,
    fecha_salida_real DATE,
    exportador VARCHAR(200),
    pais_destino VARCHAR(100) NOT NULL,
    puerto_destino VARCHAR(100),
    ciudad_destino VARCHAR(100),
    tipo_transporte VARCHAR(20) NOT NULL,
    codigo_contenedor VARCHAR(50),
    tipo_contenedor VARCHAR(50),
    temperatura_contenedor DOUBLE PRECISION,
    transportista VARCHAR(200),
    numero_booking VARCHAR(100),
    numero_bl VARCHAR(100),
    estado VARCHAR(30) NOT NULL DEFAULT 'CREADO',
    peso_neto_total DOUBLE PRECISION,
    peso_bruto_total DOUBLE PRECISION,
    numero_pallets INTEGER,
    numero_cajas INTEGER,
    observaciones VARCHAR(1000),
    cliente_importador VARCHAR(200),
    incoterm VARCHAR(10),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hash_cierre VARCHAR(64),
    fecha_cierre TIMESTAMP,
    usuario_cierre_id BIGINT,
    CONSTRAINT fk_envio_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_envio_usuario_cierre FOREIGN KEY (usuario_cierre_id) REFERENCES usuarios(id)
);

-- Tabla de pallets (SIN empresa_id en V1 - se agregará en V2)
CREATE TABLE IF NOT EXISTS pallets (
    id BIGSERIAL PRIMARY KEY,
    codigo_pallet VARCHAR(50) NOT NULL UNIQUE,
    fecha_paletizado DATE NOT NULL,
    tipo_pallet VARCHAR(50),
    numero_cajas INTEGER NOT NULL,
    peso_neto_total DOUBLE PRECISION,
    peso_bruto_total DOUBLE PRECISION,
    altura_pallet DOUBLE PRECISION,
    tipo_fruta VARCHAR(100),
    calidad VARCHAR(50),
    destino VARCHAR(200),
    temperatura_almacenamiento DOUBLE PRECISION,
    responsable_paletizado VARCHAR(150),
    estado_pallet VARCHAR(30) NOT NULL DEFAULT 'ARMADO',
    observaciones TEXT,
    envio_id BIGINT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_pallet_envio FOREIGN KEY (envio_id) REFERENCES envios(id)
);

-- Tabla de etiquetas
CREATE TABLE IF NOT EXISTS etiquetas (
    id BIGSERIAL PRIMARY KEY,
    clasificacion_id BIGINT NOT NULL,
    codigo_etiqueta VARCHAR(50) NOT NULL UNIQUE,
    codigo_qr VARCHAR(36) NOT NULL UNIQUE,
    tipo_etiqueta VARCHAR(30) NOT NULL,
    cantidad_contenida DOUBLE PRECISION,
    unidad_medida VARCHAR(20),
    peso_neto DOUBLE PRECISION,
    peso_bruto DOUBLE PRECISION,
    numero_cajas INTEGER,
    estado_etiqueta VARCHAR(30) NOT NULL DEFAULT 'DISPONIBLE',
    url_qr VARCHAR(500),
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_etiqueta_clasificacion FOREIGN KEY (clasificacion_id) REFERENCES clasificaciones(id)
);

-- Tabla intermedia etiquetas-pallets
CREATE TABLE IF NOT EXISTS etiquetas_pallets (
    id BIGSERIAL PRIMARY KEY,
    etiqueta_id BIGINT NOT NULL,
    pallet_id BIGINT NOT NULL,
    posicion_en_pallet INTEGER,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_ep_etiqueta FOREIGN KEY (etiqueta_id) REFERENCES etiquetas(id),
    CONSTRAINT fk_ep_pallet FOREIGN KEY (pallet_id) REFERENCES pallets(id)
);

-- Tabla de controles de calidad
CREATE TABLE IF NOT EXISTS controles_calidad (
    id BIGSERIAL PRIMARY KEY,
    clasificacion_id BIGINT,
    pallet_id BIGINT,
    codigo_control VARCHAR(50) NOT NULL UNIQUE,
    fecha_control DATE NOT NULL,
    tipo_control VARCHAR(100) NOT NULL,
    parametro_evaluado VARCHAR(200),
    valor_medido VARCHAR(100),
    valor_esperado VARCHAR(100),
    cumple_especificacion BOOLEAN NOT NULL DEFAULT true,
    resultado VARCHAR(30) NOT NULL,
    responsable_control VARCHAR(150) NOT NULL,
    laboratorio VARCHAR(200),
    numero_certificado VARCHAR(100),
    accion_correctiva TEXT,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_control_clasificacion FOREIGN KEY (clasificacion_id) REFERENCES clasificaciones(id),
    CONSTRAINT fk_control_pallet FOREIGN KEY (pallet_id) REFERENCES pallets(id)
);

-- Tabla de documentos de exportación
CREATE TABLE IF NOT EXISTS documentos_exportacion (
    id BIGSERIAL PRIMARY KEY,
    envio_id BIGINT NOT NULL,
    tipo_documento VARCHAR(50) NOT NULL,
    numero_documento VARCHAR(100) NOT NULL,
    fecha_emision DATE NOT NULL,
    fecha_vencimiento DATE,
    entidad_emisora VARCHAR(200),
    funcionario_emisor VARCHAR(200),
    url_archivo VARCHAR(500),
    tipo_archivo VARCHAR(50),
    tamano_archivo BIGINT,
    hash_archivo VARCHAR(64),
    estado VARCHAR(20) NOT NULL DEFAULT 'GENERADO',
    descripcion VARCHAR(1000),
    valor_declarado DOUBLE PRECISION,
    moneda VARCHAR(10),
    obligatorio BOOLEAN NOT NULL DEFAULT false,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_documento_envio FOREIGN KEY (envio_id) REFERENCES envios(id)
);

-- Tabla de eventos logísticos
CREATE TABLE IF NOT EXISTS eventos_logisticos (
    id BIGSERIAL PRIMARY KEY,
    envio_id BIGINT NOT NULL,
    codigo_evento VARCHAR(50),
    tipo_evento VARCHAR(30) NOT NULL,
    fecha_evento DATE NOT NULL,
    hora_evento TIME NOT NULL,
    ubicacion VARCHAR(200) NOT NULL,
    ciudad VARCHAR(100),
    pais VARCHAR(100),
    latitud DOUBLE PRECISION,
    longitud DOUBLE PRECISION,
    responsable VARCHAR(200) NOT NULL,
    organizacion VARCHAR(200),
    temperatura_registrada DOUBLE PRECISION,
    humedad_registrada DOUBLE PRECISION,
    vehiculo VARCHAR(20),
    conductor VARCHAR(200),
    numero_precinto VARCHAR(100),
    observaciones VARCHAR(1000),
    url_evidencia VARCHAR(500),
    incidencia BOOLEAN NOT NULL DEFAULT false,
    detalle_incidencia VARCHAR(1000),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evento_envio FOREIGN KEY (envio_id) REFERENCES envios(id)
);

-- Tabla de auditoría de eventos
CREATE TABLE IF NOT EXISTS auditoria_eventos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo_entidad VARCHAR(50) NOT NULL,
    entidad_id BIGINT NOT NULL,
    codigo_entidad VARCHAR(100),
    tipo_operacion VARCHAR(20) NOT NULL,
    descripcion_operacion VARCHAR(500) NOT NULL,
    datos_anteriores TEXT,
    datos_nuevos TEXT,
    campos_modificados VARCHAR(500),
    hash_evento VARCHAR(64) NOT NULL,
    hash_anterior VARCHAR(64),
    ip_origen VARCHAR(50),
    user_agent VARCHAR(500),
    empresa_id BIGINT NOT NULL,
    empresa_nombre VARCHAR(200),
    modulo VARCHAR(30) NOT NULL,
    nivel_criticidad VARCHAR(10) NOT NULL DEFAULT 'INFO',
    en_cadena BOOLEAN NOT NULL DEFAULT false,
    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Índices para auditoría
CREATE INDEX IF NOT EXISTS idx_auditoria_entidad ON auditoria_eventos(tipo_entidad, entidad_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario ON auditoria_eventos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_fecha ON auditoria_eventos(fecha_evento);

-- Índices adicionales para performance
CREATE INDEX IF NOT EXISTS idx_usuario_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_usuario_empresa ON usuarios(empresa_id);
CREATE INDEX IF NOT EXISTS idx_finca_empresa ON fincas(empresa_id);
CREATE INDEX IF NOT EXISTS idx_lote_finca ON lotes(finca_id);
CREATE INDEX IF NOT EXISTS idx_pallet_estado ON pallets(estado_pallet);
CREATE INDEX IF NOT EXISTS idx_envio_estado ON envios(estado);
CREATE INDEX IF NOT EXISTS idx_envio_usuario ON envios(usuario_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_tokens(user_id);
