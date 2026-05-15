INSERT INTO cargo (id, nombre, salario_base) VALUES
(1, 'Gerente general', 4000000),
(2, 'Gerente de operaciones', 2750000),
(3, 'Jefe de mantenimiento', 3000000),
(4, 'Jefe de recursos humanos', 2900000),
(5, 'Auxiliar de mantenimiento', 2100000),
(6, 'Director de TI', 3400000),
(7, 'Auxiliar administrativo', 2500000);

INSERT INTO empleado (nombre, apellido, telefono, direccion, fecha_nacimiento, documento, email, fecha_ingreso, pin, id_cargo) VALUES
('Cristian', 'Marrugo Barrios', '3203930195', 'La boquilla carrera 9 # 53-29', '2005-04-02', '1043296213', 'cristian.maba2005@outlook.com', CURDATE(), '0204', 1);

INSERT INTO usuario (username, password, role) VALUES
('admin', 'admin123', 'ROLE_ADMIN');