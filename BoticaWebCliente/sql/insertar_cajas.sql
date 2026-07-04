-- =====================================================
-- INSERTAR CAJAS POR DEFECTO
-- Ejecutar en MySQL si no existen cajas
-- =====================================================

-- Verificar estructura de la tabla cajas
-- Si no existe la tabla, crearla:

CREATE TABLE IF NOT EXISTS cajas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(200),
    activo TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar cajas por defecto si no existen
INSERT INTO cajas (nombre, descripcion, activo) 
SELECT 'Caja Principal', 'Mostrador principal', 1
WHERE NOT EXISTS (SELECT 1 FROM cajas WHERE nombre = 'Caja Principal');

INSERT INTO cajas (nombre, descripcion, activo) 
SELECT 'Caja 2', 'Mostrador secundario', 1
WHERE NOT EXISTS (SELECT 1 FROM cajas WHERE nombre = 'Caja 2');

-- Verificar las cajas insertadas
SELECT * FROM cajas;
