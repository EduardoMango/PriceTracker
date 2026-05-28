-- =============================================================================
-- 1. INSERCIÓN DE PERMISOS (Enum Permits)
-- =============================================================================
INSERT INTO permit_entity (permit) VALUES
                                       ('ALTA_PRODUCTO'),
                                       ('CREAR_CUENTA'),
                                       ('ACTUALIZAR_CUENTA'),
                                       ('ELIMINAR_CUENTA'),
                                       ('VER_USUARIOS'),
                                       ('CREAR_USUARIO'),
                                       ('ACTUALIZAR_USUARIO'),
                                       ('ELIMINAR_USUARIO');

-- =============================================================================
-- 2. INSERCIÓN DE ROLES (Enum Roles)
-- =============================================================================
INSERT INTO role_entity (role) VALUES
                                   ('ROLE_USER'),
                                   ('ROLE_ADMIN');

-- =============================================================================
-- 3. ASOCIACIÓN DE ROLES Y PERMISOS (Tabla intermedia role_permits)
-- =============================================================================
-- ROLE_USER (id: 1) -> CREAR_CUENTA, ACTUALIZAR_CUENTA
INSERT INTO role_permits (role_id, permit_id) VALUES
                                                  (1, 2), -- CREAR_CUENTA
                                                  (1, 3); -- ACTUALIZAR_CUENTA

-- ROLE_ADMIN (id: 2) -> Todos los permisos
INSERT INTO role_permits (role_id, permit_id) VALUES
                                                  (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8);

-- =============================================================================
-- 4. INSERCIÓN DE USUARIOS DE PRUEBA (UserEntity)
-- Generamos UUIDs válidos y correos simulados
-- =============================================================================
INSERT INTO user_entity (id, external_id, email_address) VALUES
                                                             (1, 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'admin.perez@example.com'),
                                                             (2, 'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'juan.lopez@example.com'),
                                                             (3, 'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f', 'maria.gomez@example.com'),
                                                             (4, 'd4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a', 'carlos.rodriguez@example.com'),
                                                             (5, 'e5f6a7b8-c9d0-1e2f-3a4b-5c6d7e8f9a0b', 'ana.martinez@example.com'),
                                                             (6, 'f6a7b8c9-d0e1-2f3a-4b5c-6d7e8f9a0b1c', 'lucas.fernandez@example.com'),
                                                             (7, 'a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d', 'sofia.alvarez@example.com'),
                                                             (8, 'b8c9d0e1-f2a3-4b5c-6d7e-8f9a0b1c2d3e', 'diego.romero@example.com'),
                                                             (9, 'c9d0e1f2-a3b4-5c6d-7e8f-9a0b1c2d3e4f', 'elena.ruiz@example.com'),
                                                             (10, 'd0e1f2a3-b4c5-6d7e-8f9a-0b1c2d3e4f5a', 'martin.silva@example.com');

-- =============================================================================
-- 5. INSERCIÓN DE CREDENCIALES (CredentialsEntity)
-- Contraseña para TODOS: password123 (hasheada con BCrypt)
-- =============================================================================
INSERT INTO credentials_entity (id, username, password, enabled, usuario_id) VALUES
                                                                                 (1, 'admin', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', true, 1),
                                                                                 (2, 'juan99', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', true, 2),
                                                                                 (3, 'maria_g', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', true, 3),
                                                                                 (4, 'crodriguez', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', true, 4),
                                                                                 (5, 'anam', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', true, 5),
                                                                                 (6, 'lucas_f', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', true, 6),
                                                                                 (7, 'sofia_alvarez', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', true, 7),
                                                                                 (8, 'dromero', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', true, 8),
                                                                                 (9, 'elena_ruiz', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', true, 9),
                                                                                 (10, 'msilva', '$2a$10$e0MYzXy6F4B7wL9s9S9O/.M7gN6n5yXGZJ7p2yvA5g.HjZ3f4q6GG', false, 10); -- Cuenta deshabilitada para probar el flujo de error

-- =============================================================================
-- 6. ASOCIACIÓN DE CREDENCIALES Y ROLES (Tabla intermedia credentials_roles)
-- =============================================================================
INSERT INTO credentials_roles (credential_id, role_id) VALUES
                                                           (1, 2), -- admin -> ROLE_ADMIN
                                                           (2, 1), -- juan99 -> ROLE_USER
                                                           (3, 1), -- maria_g -> ROLE_USER
                                                           (4, 1), -- crodriguez -> ROLE_USER
                                                           (5, 1), -- anam -> ROLE_USER
                                                           (6, 1), -- lucas_f -> ROLE_USER
                                                           (7, 1), -- sofia_alvarez -> ROLE_USER
                                                           (8, 1), -- dromero -> ROLE_USER
                                                           (9, 1), -- elena_ruiz -> ROLE_USER
                                                           (10, 1); -- msilva -> ROLE_USER