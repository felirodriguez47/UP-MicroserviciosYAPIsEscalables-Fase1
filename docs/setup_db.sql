-- Sprint 1: creación de la base de datos
CREATE DATABASE IF NOT EXISTS vet_system CHARACTER SET utf8mb4;

-- Verificación (correr después de levantar la app con ddl-auto=update)
USE vet_system;
SHOW TABLES;

-- Resultado esperado:
-- +----------------------+
-- | Tables_in_vet_system |
-- +----------------------+
-- | duenos               |
-- | mascotas             |
-- | veterinarios         |
-- | turnos               |
-- +----------------------+

DESCRIBE duenos;
