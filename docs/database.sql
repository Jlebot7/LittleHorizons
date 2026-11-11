-- Anexo: Script de Base de Datos

-- A continuación se presenta el script completo DDL de MySQL para crear todas las tablas necesarias. 
-- Este script debe ejecutarse en una base de datos llamada `little_horizons_erp`.
-- Si encuentra detalles que modificar o agregar, puede editar el script siempre y cuando pueda justificar sus cambios.


-- ============================================
-- Little Horizons ERP - Esquema de Base de Datos
-- Sistema de Gestión Educativa
-- Versión: 2.0 (Corregida y Mejorada)
-- Motor: MySQL 8.0 o superior (REQUERIDO)
-- ============================================

DROP DATABASE IF EXISTS erp_little_horizons;
CREATE DATABASE erp_little_horizons CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_little_horizons;

-- ============================================
-- 1. MÓDULO DE ESTUDIANTES Y ACUDIENTES
-- ============================================

-- Tabla de grados académicos
CREATE TABLE grados (
    id_grado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_grado VARCHAR(50) NOT NULL UNIQUE COMMENT 'Ej: Preescolar, Primero, Segundo, ... Once',
    nivel_numerico INT NOT NULL UNIQUE COMMENT 'Nivel numérico para ordenamiento (0 para preescolar, 1-11 para grados)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Catálogo de grados académicos del colegio';

-- Tabla de estudiantes
CREATE TABLE estudiantes (
    id_estudiante INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('TI', 'CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'TI',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    fecha_nacimiento DATE NOT NULL,
    genero ENUM('M', 'F', 'Otro') NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(20),
    correo_electronico VARCHAR(100),
    tipo_sangre VARCHAR(5),
    alergias TEXT,
    condiciones_medicas TEXT,
    fecha_ingreso DATE NOT NULL,
    estado ENUM('Activo', 'Graduado', 'Retirado', 'Suspendido') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido, primer_nombre)
) ENGINE=InnoDB COMMENT='Información completa de estudiantes';

-- Tabla de acudientes/tutores legales
CREATE TABLE acudientes (
    id_acudiente INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    parentesco VARCHAR(50) NOT NULL COMMENT 'Padre, Madre, Abuelo, Tío, Tutor Legal, etc.',
    telefono_principal VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),
    correo_electronico VARCHAR(100),
    direccion VARCHAR(255),
    ocupacion VARCHAR(100),
    es_responsable_financiero BOOLEAN DEFAULT TRUE COMMENT 'Indica si es responsable de pagos',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Acudientes y tutores legales';

-- Relación estudiantes-acudientes (muchos a muchos)
CREATE TABLE estudiante_acudiente (
    id_estudiante INT NOT NULL,
    id_acudiente INT NOT NULL,
    es_principal BOOLEAN DEFAULT FALSE COMMENT 'Acudiente principal para comunicaciones',
    es_contacto_emergencia BOOLEAN DEFAULT TRUE COMMENT 'Disponible para emergencias',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_estudiante, id_acudiente),
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_acudiente (id_acudiente)
) ENGINE=InnoDB COMMENT='Relación entre estudiantes y sus acudientes';

-- Tabla de matrículas (historial de grados por año)
CREATE TABLE matriculas (
    id_matricula INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_grado INT NOT NULL,
    año_academico INT NOT NULL COMMENT 'Año académico (ej: 2024)',
    estado_matricula ENUM('Activa', 'Completada', 'Retirada') DEFAULT 'Activa',
    fecha_matricula DATE NOT NULL,
    fecha_retiro DATE,
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    UNIQUE KEY matricula_unica (id_estudiante, año_academico),
    INDEX idx_año_grado (año_academico, id_grado),
    INDEX idx_estado (estado_matricula)
) ENGINE=InnoDB COMMENT='Historial de matrículas de estudiantes por año y grado';

-- ============================================
-- 2. MÓDULO ACADÉMICO
-- ============================================

-- Tabla de materias/asignaturas
CREATE TABLE materias (
    id_materia INT AUTO_INCREMENT PRIMARY KEY,
    codigo_materia VARCHAR(20) NOT NULL UNIQUE COMMENT 'Código único de la materia (ej: MAT-5, ESP-3)',
    nombre_materia VARCHAR(100) NOT NULL,
    descripcion TEXT,
    id_grado INT NOT NULL COMMENT 'Grado al que pertenece la materia',
    horas_semanales INT DEFAULT 2 COMMENT 'Horas semanales asignadas',
    esta_activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_grado) REFERENCES grados(id_grado),
    INDEX idx_grado (id_grado),
    INDEX idx_activa (esta_activa),
    INDEX idx_codigo (codigo_materia)
) ENGINE=InnoDB COMMENT='Catálogo de materias por grado';

-- Tabla de profesores
CREATE TABLE profesores (
    id_profesor INT AUTO_INCREMENT PRIMARY KEY,
    tipo_documento ENUM('CC', 'CE', 'Pasaporte') NOT NULL DEFAULT 'CC',
    numero_documento VARCHAR(20) NOT NULL UNIQUE,
    primer_nombre VARCHAR(100) NOT NULL,
    segundo_nombre VARCHAR(100),
    primer_apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    correo_electronico VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    especializacion VARCHAR(100) COMMENT 'Área de especialización (Matemáticas, Español, etc.)',
    fecha_contratacion DATE NOT NULL,
    estado ENUM('Activo', 'Inactivo', 'Licencia') DEFAULT 'Activo',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_documento (tipo_documento, numero_documento),
    INDEX idx_estado (estado),
    INDEX idx_nombre (primer_apellido, segundo_apellido)
) ENGINE=InnoDB COMMENT='Información de profesores';

-- Asignación de materias a profesores
CREATE TABLE asignaciones_profesor (
    id_asignacion INT AUTO_INCREMENT PRIMARY KEY,
    id_profesor INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    seccion VARCHAR(10) COMMENT 'Sección o grupo (A, B, C)',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY asignacion_unica (id_profesor, id_materia, año_academico, seccion),
    INDEX idx_profesor_año (id_profesor, año_academico),
    INDEX idx_materia_año (id_materia, año_academico)
) ENGINE=InnoDB COMMENT='Asignación de profesores a materias por año académico';

-- Inscripción de estudiantes en materias
CREATE TABLE inscripciones_materia (
    id_inscripcion INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV') NOT NULL COMMENT 'Período académico',
    seccion VARCHAR(10) COMMENT 'Sección o grupo asignado',
    fecha_inscripcion DATE NOT NULL,
    estado ENUM('Activa', 'Retirada', 'Completada') DEFAULT 'Activa',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    UNIQUE KEY inscripcion_unica (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico)
) ENGINE=InnoDB COMMENT='Inscripción de estudiantes en materias específicas';

-- Tabla de registros académicos (calificaciones/notas)
CREATE TABLE registros_academicos (
    id_registro INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    id_profesor INT NOT NULL,
    año_academico INT NOT NULL,
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Final') NOT NULL,
    valor_nota DECIMAL(2,1) NOT NULL COMMENT 'Nota de 0.0 a 5.0 según estándar colombiano',
    observaciones TEXT,
    fecha_registro DATE NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor),
    CHECK (valor_nota >= 0.0 AND valor_nota <= 5.0),
    UNIQUE KEY registro_unico (id_estudiante, id_materia, año_academico, periodo_academico),
    INDEX idx_estudiante_año (id_estudiante, año_academico),
    INDEX idx_materia_periodo (id_materia, periodo_academico),
    INDEX idx_valor_nota (valor_nota)
) ENGINE=InnoDB COMMENT='Registro de calificaciones de estudiantes';

-- Tabla de asistencia
CREATE TABLE asistencias (
    id_asistencia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_materia INT NOT NULL,
    fecha_asistencia DATE NOT NULL,
    estado_asistencia ENUM('Presente', 'Ausente', 'Tardanza', 'Excusa Médica') NOT NULL,
    observaciones TEXT,
    registrado_por INT COMMENT 'ID del profesor que registró la asistencia',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materias(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (registrado_por) REFERENCES profesores(id_profesor),
    UNIQUE KEY asistencia_unica (id_estudiante, id_materia, fecha_asistencia),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_asistencia),
    INDEX idx_materia_fecha (id_materia, fecha_asistencia),
    INDEX idx_estado (estado_asistencia)
) ENGINE=InnoDB COMMENT='Registro de asistencia diaria de estudiantes';

-- ============================================
-- 3. MÓDULO DE ACTIVIDADES EXTRACURRICULARES
-- ============================================

-- Tabla de clubes
CREATE TABLE clubes (
    id_club INT AUTO_INCREMENT PRIMARY KEY,
    nombre_club VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    id_profesor_supervisor INT COMMENT 'Profesor encargado del club',
    capacidad_maxima INT DEFAULT 30 COMMENT 'Cupo máximo de estudiantes',
    horario_reunion VARCHAR(100) COMMENT 'Días y horario de reunión (ej: Martes 3-5 PM)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_profesor_supervisor) REFERENCES profesores(id_profesor),
    INDEX idx_activo (esta_activo),
    INDEX idx_nombre (nombre_club)
) ENGINE=InnoDB COMMENT='Catálogo de clubes extracurriculares';

-- Membresías en clubes
CREATE TABLE membresias_club (
    id_membresia INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_club INT NOT NULL,
    fecha_ingreso DATE NOT NULL,
    estado_membresia ENUM('Activa', 'Retirada') DEFAULT 'Activa',
    fecha_retiro DATE,
    motivo_retiro TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_club) REFERENCES clubes(id_club) ON DELETE CASCADE,
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_club_estado (id_club, estado_membresia)
) ENGINE=InnoDB COMMENT='Membresías de estudiantes en clubes';

-- Tabla de eventos
CREATE TABLE eventos (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    nombre_evento VARCHAR(150) NOT NULL,
    descripcion TEXT,
    tipo_evento ENUM('Académico', 'Cultural', 'Deportivo', 'Social', 'Científico') NOT NULL,
    fecha_evento DATE NOT NULL,
    lugar VARCHAR(150),
    tiene_competencia BOOLEAN DEFAULT FALSE COMMENT 'Indica si el evento tiene competencia con posiciones',
    id_organizador INT COMMENT 'Profesor organizador del evento',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_organizador) REFERENCES profesores(id_profesor),
    INDEX idx_fecha (fecha_evento),
    INDEX idx_tipo (tipo_evento),
    INDEX idx_activo (esta_activo)
) ENGINE=InnoDB COMMENT='Catálogo de eventos del colegio';

-- Participantes en eventos
CREATE TABLE participantes_evento (
    id_participacion INT AUTO_INCREMENT PRIMARY KEY,
    id_evento INT NOT NULL,
    id_estudiante INT NOT NULL,
    rol_participacion ENUM('Participante', 'Competidor', 'Organizador', 'Asistente') DEFAULT 'Participante',
    posicion_obtenida VARCHAR(20) COMMENT 'Posición obtenida si hay competencia (1er Lugar, 2do Lugar, 3er Lugar, Participante)',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    UNIQUE KEY participacion_unica (id_evento, id_estudiante),
    INDEX idx_evento (id_evento),
    INDEX idx_estudiante (id_estudiante)
) ENGINE=InnoDB COMMENT='Registro de participación de estudiantes en eventos';

-- ============================================
-- 4. MÓDULO FINANCIERO
-- ============================================

-- Conceptos de pago (catálogo)
CREATE TABLE conceptos_pago (
    id_concepto INT AUTO_INCREMENT PRIMARY KEY,
    codigo_concepto VARCHAR(20) NOT NULL UNIQUE,
    nombre_concepto VARCHAR(100) NOT NULL,
    descripcion TEXT,
    monto_base DECIMAL(10,2) NOT NULL COMMENT 'Valor base del concepto en pesos colombianos',
    es_recurrente BOOLEAN DEFAULT FALSE COMMENT 'Indica si es un pago recurrente (ej: pensión mensual)',
    esta_activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_activo (esta_activo),
    INDEX idx_codigo (codigo_concepto)
) ENGINE=InnoDB COMMENT='Catálogo de conceptos de pago del colegio';

-- Tabla de pagos
CREATE TABLE pagos (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante INT NOT NULL,
    id_acudiente INT COMMENT 'Acudiente que realizó el pago',
    id_concepto INT NOT NULL,
    monto_pagado DECIMAL(10,2) NOT NULL,
    fecha_pago DATE NOT NULL,
    metodo_pago ENUM('Efectivo', 'Transferencia', 'Tarjeta Débito', 'Tarjeta Crédito', 'Cheque') NOT NULL,
    numero_recibo VARCHAR(50) UNIQUE,
    año_academico INT COMMENT 'Año al que aplica el pago',
    periodo_academico ENUM('I', 'II', 'III', 'IV', 'Anual') COMMENT 'Período al que aplica el pago',
    notas TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    FOREIGN KEY (id_acudiente) REFERENCES acudientes(id_acudiente),
    FOREIGN KEY (id_concepto) REFERENCES conceptos_pago(id_concepto),
    INDEX idx_estudiante_fecha (id_estudiante, fecha_pago),
    INDEX idx_concepto (id_concepto),
    INDEX idx_fecha (fecha_pago),
    INDEX idx_recibo (numero_recibo)
) ENGINE=InnoDB COMMENT='Registro de todos los pagos realizados';

-- Tabla de facturas electrónicas
CREATE TABLE facturas (
    id_factura INT AUTO_INCREMENT PRIMARY KEY,
    numero_factura VARCHAR(50) NOT NULL UNIQUE COMMENT 'Número consecutivo de factura',
    id_pago INT NOT NULL COMMENT 'Pago asociado a esta factura',
    fecha_emision DATE NOT NULL,
    cufe VARCHAR(100) COMMENT 'Código Único de Factura Electrónica (simulado)',
    nombre_archivo_pdf VARCHAR(255) COMMENT 'Nombre del archivo PDF generado',
    ruta_archivo VARCHAR(500) COMMENT 'Ruta completa donde se guardó el PDF',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_pago) REFERENCES pagos(id_pago) ON DELETE CASCADE,
    INDEX idx_numero_factura (numero_factura),
    INDEX idx_fecha_emision (fecha_emision),
    INDEX idx_pago (id_pago)
) ENGINE=InnoDB COMMENT='Registro de facturas electrónicas generadas';

-- ============================================
-- 5. MÓDULO DE GESTIÓN DE RECURSOS
-- ============================================

-- Categorías de recursos
CREATE TABLE categorias_recurso (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='Categorías de recursos (Libro, Equipo de Laboratorio, etc.)';

-- Tabla de recursos del colegio
CREATE TABLE recursos (
    id_recurso INT AUTO_INCREMENT PRIMARY KEY,
    codigo_recurso VARCHAR(50) NOT NULL UNIQUE COMMENT 'Código de inventario único',
    nombre_recurso VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL,
    descripcion TEXT,
    fecha_adquisicion DATE,
    ubicacion_fisica VARCHAR(100) COMMENT 'Ubicación física (Biblioteca, Laboratorio 1, etc.)',
    estado_recurso ENUM('Disponible', 'Prestado', 'En Mantenimiento', 'Dado de Baja') DEFAULT 'Disponible',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_categoria) REFERENCES categorias_recurso(id_categoria),
    INDEX idx_codigo (codigo_recurso),
    INDEX idx_estado (estado_recurso),
    INDEX idx_categoria (id_categoria),
    INDEX idx_nombre (nombre_recurso)
) ENGINE=InnoDB COMMENT='Inventario de recursos prestables del colegio';

-- Tabla de préstamos de recursos a ESTUDIANTES
CREATE TABLE prestamos_estudiante (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_estudiante INT NOT NULL COMMENT 'Estudiante que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_estudiante (id_estudiante),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a estudiantes';

-- Tabla de préstamos de recursos a PROFESORES
CREATE TABLE prestamos_profesor (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_recurso INT NOT NULL,
    id_profesor INT NOT NULL COMMENT 'Profesor que toma prestado el recurso',
    fecha_prestamo DATE NOT NULL,
    fecha_devolucion_esperada DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado_prestamo ENUM('Activo', 'Devuelto', 'Vencido') DEFAULT 'Activo',
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id_recurso) REFERENCES recursos(id_recurso) ON DELETE CASCADE,
    FOREIGN KEY (id_profesor) REFERENCES profesores(id_profesor) ON DELETE CASCADE,
    INDEX idx_recurso_estado (id_recurso, estado_prestamo),
    INDEX idx_profesor (id_profesor),
    INDEX idx_estado (estado_prestamo),
    INDEX idx_fecha_devolucion (fecha_devolucion_esperada)
) ENGINE=InnoDB COMMENT='Registro de préstamos de recursos a profesores';



-- ========================================================
-- SECCIÓN 2: DATA MANIPULATION LANGUAGE (DML) - DATOS DE PRUEBA
-- Mínimo 30 registros coherentes por cada tabla principal
-- ========================================================

-- 1. Catálogo de Grados
INSERT INTO grados (id_grado, nombre_grado, nivel_numerico) VALUES
(1, 'Preescolar', 0),
(2, 'Primero', 1),
(3, 'Segundo', 2),
(4, 'Tercero', 3),
(5, 'Cuarto', 4),
(6, 'Quinto', 5),
(7, 'Sexto', 6),
(8, 'Séptimo', 7),
(9, 'Octavo', 8),
(10, 'Noveno', 9),
(11, 'Décimo', 10),
(12, 'Once', 11);

-- 2. Catálogo de Categorías de Recurso
INSERT INTO categorias_recurso (id_categoria, nombre_categoria, descripcion) VALUES
(1, 'Libro', 'Libros de literatura, textos escolares y enciclopedias de biblioteca'),
(2, 'Equipo de Laboratorio', 'Microscopios, tubos de ensayo, balanzas y mecheros Bunsen'),
(3, 'Equipo Deportivo', 'Balones reglamentarios, colchonetas, raquetas y cronómetros'),
(4, 'Dispositivo Tecnológico', 'Tablets educativas, portátiles, proyectores y calculadoras científicas'),
(5, 'Material Didáctico', 'Mapas geográficos, modelos anatómicos y juegos de robótica educativa');

-- 3. Catálogo de Conceptos de Pago
INSERT INTO conceptos_pago (id_concepto, codigo_concepto, nombre_concepto, descripcion, monto_base, es_recurrente) VALUES
(1, 'MAT', 'Matrícula Anual', 'Derecho de matrícula para el año lectivo', 650000.00, FALSE),
(2, 'PENSION', 'Pensión Mensual', 'Cuota de pensión académica ordinaria', 420000.00, TRUE),
(3, 'UNIFORME', 'Uniforme Escolar', 'Dotación de uniforme de diario y educación física', 280000.00, FALSE),
(4, 'TRANSPORTE', 'Transporte Escolar', 'Servicio mensual de ruta escolar ida y vuelta', 190000.00, TRUE),
(5, 'ALIMENTACION', 'Alimentación Escolar', 'Almuerzo balanceado y refrigerios diarios mensual', 160000.00, TRUE),
(6, 'ACTIVIDAD', 'Salida Pedagógica', 'Transporte y entradas a museos o centros científicos', 75000.00, FALSE),
(7, 'CERTIFICADO', 'Certificado Académico', 'Expedición de certificados y constancias de estudio', 20000.00, FALSE);

-- 4. Tabla de Acudientes (35 registros)
INSERT INTO acudientes (id_acudiente, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, correo_electronico, direccion, ocupacion, es_responsable_financiero) VALUES
(1, 'CC', '1018456201', 'Carlos', 'Alberto', 'Ramirez', 'Gomez', 'Padre', '3104567890', '3001234567', 'carlos.ramirez@gmail.com', 'Calle 127 # 45-12, Bogota', 'Ingeniero Civil', TRUE),
(2, 'CC', '1020456202', 'Martha', 'Cecilia', 'Rodriguez', 'Perez', 'Madre', '3156789012', NULL, 'martha.rodriguez@hotmail.com', 'Carrera 15 # 85-30, Bogota', 'Contadora Publica', TRUE),
(3, 'CC', '1032456203', 'Jorge', 'Enrique', 'Gonzalez', 'Castro', 'Padre', '3182345678', '3119876543', 'jorge.gonzalez@yahoo.com', 'Avenida Suba # 116-20, Bogota', 'Abogado', TRUE),
(4, 'CC', '1045456204', 'Claudia', 'Patricia', 'Lopez', 'Mendoza', 'Madre', '3123456789', NULL, 'claudia.lopez@outlook.com', 'Calle 72 # 11-45, Bogota', 'Medica Pediatra', TRUE),
(5, 'CC', '1056456205', 'Andres', 'Felipe', 'Hernandez', 'Silva', 'Padre', '3167890123', '3201234567', 'andres.hernandez@gmail.com', 'Carrera 7 # 140-50, Bogota', 'Arquitecto', TRUE),
(6, 'CC', '1067456206', 'Gloria', 'Esperanza', 'Martinez', 'Rios', 'Madre', '3178901234', NULL, 'gloria.martinez@hotmail.com', 'Calle 100 # 19-60, Bogota', 'Docente Universitaria', TRUE),
(7, 'CC', '1078456207', 'Gustavo', 'Adolfo', 'Garcia', 'Vargas', 'Padre', '3112345678', '3134567890', 'gustavo.garcia@gmail.com', 'Carrera 58 # 134-22, Bogota', 'Economista', TRUE),
(8, 'CC', '1089456208', 'Paola', 'Andrea', 'Sanchez', 'Navarro', 'Madre', '3145678901', NULL, 'paola.sanchez@gmail.com', 'Calle 142 # 18-35, Bogota', 'Disenadora Grafica', TRUE),
(9, 'CC', '1090456209', 'Fernando', 'Jose', 'Diaz', 'Ortiz', 'Padre', '3190123456', '3157890123', 'fernando.diaz@gmail.com', 'Carrera 9 # 106-15, Bogota', 'Empresario', TRUE),
(10, 'CC', '1101456210', 'Sandra', 'Milena', 'Torres', 'Morales', 'Madre', '3128901234', NULL, 'sandra.torres@hotmail.com', 'Calle 80 # 68-40, Bogota', 'Administradora', TRUE),
(11, 'CC', '1112456211', 'Luis', 'Eduardo', 'Rojas', 'Herrera', 'Padre', '3139012345', '3184567890', 'luis.rojas@gmail.com', 'Carrera 11 # 93-20, Bogota', 'Consultor Financiero', TRUE),
(12, 'CC', '1123456212', 'Adriana', 'Maria', 'Vargas', 'Jimenez', 'Madre', '3150123456', NULL, 'adriana.vargas@outlook.com', 'Calle 134 # 9-45, Bogota', 'Odontologa', TRUE),
(13, 'CC', '1134456213', 'Mauricio', 'Alejandro', 'Castro', 'Duarte', 'Padre', '3161234567', '3109876543', 'mauricio.castro@gmail.com', 'Carrera 19 # 122-10, Bogota', 'Ingeniero de Sistemas', TRUE),
(14, 'CC', '1145456214', 'Diana', 'Carolina', 'Mendoza', 'Romero', 'Madre', '3172345678', NULL, 'diana.mendoza@gmail.com', 'Calle 152 # 13-25, Bogota', 'Psicologa', TRUE),
(15, 'CC', '1156456215', 'Ricardo', 'Antonio', 'Morales', 'Cruz', 'Padre', '3183456789', '3126789012', 'ricardo.morales@yahoo.com', 'Carrera 45 # 104-50, Bogota', 'Comerciante', TRUE),
(16, 'CC', '1167456216', 'Olga', 'Lucia', 'Ortiz', 'Gutierrez', 'Madre', '3194567890', NULL, 'olga.ortiz@hotmail.com', 'Calle 116 # 15-80, Bogota', 'Nutricionista', TRUE),
(17, 'CC', '1178456217', 'Camilo', 'Ernesto', 'Gutierrez', 'Pardo', 'Padre', '3105678901', '3147890123', 'camilo.gutierrez@gmail.com', 'Carrera 7A # 124-30, Bogota', 'Gerente Comercial', TRUE),
(18, 'CC', '1189456218', 'Monica', 'Viviana', 'Alvarez', 'Castillo', 'Madre', '3116789012', NULL, 'monica.alvarez@gmail.com', 'Calle 140 # 11-15, Bogota', 'Fisioterapeuta', TRUE),
(19, 'CC', '1190456219', 'Oscar', 'Javier', 'Reyes', 'Salazar', 'Padre', '3127890123', '3163456789', 'oscar.reyes@hotmail.com', 'Carrera 21 # 86-45, Bogota', 'Abogado Laboralista', TRUE),
(20, 'CC', '1201456220', 'Liliana', 'Maria', 'Herrera', 'Montoya', 'Madre', '3138901234', NULL, 'liliana.herrera@gmail.com', 'Calle 95 # 13-35, Bogota', 'Bacteriologa', TRUE),
(21, 'CC', '1212456221', 'Hector', 'Fabio', 'Jimenez', 'Rincon', 'Padre', '3149012345', '3171234567', 'hector.jimenez@yahoo.com', 'Carrera 13 # 118-25, Bogota', 'Contador', TRUE),
(22, 'CC', '1223456222', 'Beatriz', 'Elena', 'Castillo', 'Guerrero', 'Madre', '3151234567', NULL, 'beatriz.castillo@outlook.com', 'Calle 122 # 18-40, Bogota', 'Docente', TRUE),
(23, 'CC', '1234456223', 'Alvaro', 'Jose', 'Perez', 'Valencia', 'Padre', '3162345678', '3108901234', 'alvaro.perez@gmail.com', 'Carrera 50 # 128-15, Bogota', 'Veterinario', TRUE),
(24, 'CC', '1245456224', 'Natalia', 'Andrea', 'Silva', 'Medina', 'Madre', '3173456789', NULL, 'natalia.silva@hotmail.com', 'Calle 106 # 23-50, Bogota', 'Periodista', TRUE),
(25, 'CC', '1256456225', 'Victor', 'Manuel', 'Romero', 'Pena', 'Padre', '3184567890', '3117890123', 'victor.romero@gmail.com', 'Carrera 15 # 102-18, Bogota', 'Ingeniero Quimico', TRUE),
(26, 'CC', '1267456226', 'Luz', 'Marina', 'Suarez', 'Cardenas', 'Madre', '3195678901', NULL, 'luz.suarez@gmail.com', 'Calle 138 # 52-20, Bogota', 'Enfermera Jefe', TRUE),
(27, 'CC', '1278456227', 'German', 'Alonso', 'Cardenas', 'Bermudez', 'Padre', '3106789012', '3124567890', 'german.cardenas@outlook.com', 'Carrera 9 # 147-30, Bogota', 'Piloto Comercial', TRUE),
(28, 'CC', '1289456228', 'Angela', 'Patricia', 'Pena', 'Lozano', 'Madre', '3117890123', NULL, 'angela.pena@gmail.com', 'Calle 170 # 8-60, Bogota', 'Arquitecta', TRUE),
(29, 'CC', '1290456229', 'Jairo', 'Alberto', 'Lozano', 'Pineda', 'Tutor Legal', '3128901234', '3156789012', 'jairo.lozano@yahoo.com', 'Carrera 7 # 155-40, Bogota', 'Pensionado', TRUE),
(30, 'CC', '1301456230', 'Esperanza', 'del Carmen', 'Bermudez', 'Caicedo', 'Abuela', '3139012345', NULL, 'esperanza.bermudez@hotmail.com', 'Calle 118 # 14-22, Bogota', 'Comerciante', TRUE),
(31, 'CC', '1312456231', 'Diego', 'Armando', 'Montoya', 'Vega', 'Padre', '3140123456', '3182345678', 'diego.montoya@gmail.com', 'Carrera 11 # 100-10, Bogota', 'Economista', TRUE),
(32, 'CC', '1323456232', 'Yolanda', 'Ines', 'Salazar', 'Rios', 'Madre', '3152345678', NULL, 'yolanda.salazar@gmail.com', 'Calle 85 # 11-55, Bogota', 'Publicista', TRUE),
(33, 'CC', '1334456233', 'Cesar', 'Augusto', 'Medina', 'Duque', 'Padre', '3163456789', '3101234567', 'cesar.medina@outlook.com', 'Carrera 17 # 134-80, Bogota', 'Ingeniero Mecanico', TRUE),
(34, 'CC', '1345456234', 'Patricia', 'Elena', 'Vega', 'Sarmiento', 'Madre', '3174567890', NULL, 'patricia.vega@gmail.com', 'Calle 147 # 19-35, Bogota', 'Microbiologa', TRUE),
(35, 'CC', '1356456235', 'Ramiro', 'Jose', 'Duque', 'Bernal', 'Tio', '3185678901', '3149012345', 'ramiro.duque@hotmail.com', 'Carrera 58 # 127-45, Bogota', 'Abogado', TRUE);

-- 5. Tabla de Estudiantes (35 registros)
INSERT INTO estudiantes (id_estudiante, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) VALUES
(1, 'TI', '1098456001', 'Santiago', 'David', 'Ramirez', 'Rodriguez', '2010-03-15', 'M', 'Calle 127 # 45-12, Bogota', '3104567890', 'santiago.ramirez@lh.edu.co', 'O+', 'Polvo', 'Ninguna', '2020-01-20', 'Activo'),
(2, 'TI', '1098456002', 'Valentina', 'Sofia', 'Ramirez', 'Rodriguez', '2012-07-22', 'F', 'Calle 127 # 45-12, Bogota', '3104567890', 'valentina.ramirez@lh.edu.co', 'O+', 'Ninguna', 'Ninguna', '2021-01-18', 'Activo'),
(3, 'TI', '1098456003', 'Mateo', 'Alejandro', 'Gonzalez', 'Lopez', '2009-11-05', 'M', 'Avenida Suba # 116-20, Bogota', '3182345678', 'mateo.gonzalez@lh.edu.co', 'A+', 'Penicilina', 'Asma leve', '2019-01-22', 'Activo'),
(4, 'TI', '1098456004', 'Mariana', 'Lucia', 'Hernandez', 'Martinez', '2011-04-18', 'F', 'Carrera 7 # 140-50, Bogota', '3167890123', 'mariana.hernandez@lh.edu.co', 'B+', 'Mani', 'Ninguna', '2020-01-20', 'Activo'),
(5, 'TI', '1098456005', 'Samuel', 'David', 'Garcia', 'Sanchez', '2008-09-30', 'M', 'Carrera 58 # 134-22, Bogota', '3112345678', 'samuel.garcia@lh.edu.co', 'O+', 'Ninguna', 'Ninguna', '2018-01-25', 'Activo'),
(6, 'TI', '1098456006', 'Isabella', 'Marie', 'Diaz', 'Torres', '2013-02-14', 'F', 'Carrera 9 # 106-15, Bogota', '3190123456', 'isabella.diaz@lh.edu.co', 'AB+', 'Mariscos', 'Ninguna', '2022-01-17', 'Activo'),
(7, 'TI', '1098456007', 'Nicolas', 'Felipe', 'Rojas', 'Vargas', '2007-06-10', 'M', 'Carrera 11 # 93-20, Bogota', '3139012345', 'nicolas.rojas@lh.edu.co', 'A-', 'Ninguna', 'Miopia', '2017-01-23', 'Activo'),
(8, 'TI', '1098456008', 'Camila', 'Andrea', 'Castro', 'Mendoza', '2014-08-25', 'F', 'Carrera 19 # 122-10, Bogota', '3161234567', 'camila.castro@lh.edu.co', 'O-', 'Ninguna', 'Ninguna', '2022-01-17', 'Activo'),
(9, 'TI', '1098456009', 'Sebastian', 'Andres', 'Morales', 'Ortiz', '2009-01-12', 'M', 'Carrera 45 # 104-50, Bogota', '3183456789', 'sebastian.morales@lh.edu.co', 'O+', 'Ibuprofeno', 'Ninguna', '2019-01-22', 'Activo'),
(10, 'TI', '1098456010', 'Gabriela', 'Sofia', 'Gutierrez', 'Alvarez', '2010-12-03', 'F', 'Carrera 7A # 124-30, Bogota', '3105678901', 'gabriela.gutierrez@lh.edu.co', 'B+', 'Polen', 'Rinitis', '2020-01-20', 'Activo'),
(11, 'TI', '1098456011', 'Lucas', 'Emilio', 'Reyes', 'Herrera', '2015-05-19', 'M', 'Carrera 21 # 86-45, Bogota', '3127890123', 'lucas.reyes@lh.edu.co', 'A+', 'Ninguna', 'Ninguna', '2023-01-19', 'Activo'),
(12, 'TI', '1098456012', 'Sara', 'Valentina', 'Jimenez', 'Castillo', '2008-03-28', 'F', 'Carrera 13 # 118-25, Bogota', '3149012345', 'sara.jimenez@lh.edu.co', 'O+', 'Lactosa', 'Ninguna', '2018-01-25', 'Activo'),
(13, 'TI', '1098456013', 'Daniel', 'Felipe', 'Perez', 'Silva', '2011-10-11', 'M', 'Carrera 50 # 128-15, Bogota', '3162345678', 'daniel.perez@lh.edu.co', 'O+', 'Ninguna', 'Ninguna', '2021-01-18', 'Activo'),
(14, 'TI', '1098456014', 'Luciana', 'Andrea', 'Romero', 'Suarez', '2012-04-05', 'F', 'Carrera 15 # 102-18, Bogota', '3184567890', 'luciana.romero@lh.edu.co', 'A+', 'Gluten', 'Celiaquia', '2021-01-18', 'Activo'),
(15, 'TI', '1098456015', 'Martin', 'Eduardo', 'Cardenas', 'Pena', '2007-08-16', 'M', 'Carrera 9 # 147-30, Bogota', '3106789012', 'martin.cardenas@lh.edu.co', 'B+', 'Ninguna', 'Ninguna', '2017-01-23', 'Activo'),
(16, 'TI', '1098456016', 'Salome', 'del Mar', 'Lozano', 'Bermudez', '2013-09-09', 'F', 'Carrera 7 # 155-40, Bogota', '3128901234', 'salome.lozano@lh.edu.co', 'O+', 'Acaros', 'Ninguna', '2022-01-17', 'Activo'),
(17, 'TI', '1098456017', 'Alejandro', 'Jose', 'Montoya', 'Salazar', '2009-06-24', 'M', 'Carrera 11 # 100-10, Bogota', '3140123456', 'alejandro.montoya@lh.edu.co', 'AB-', 'Ninguna', 'Ninguna', '2019-01-22', 'Activo'),
(18, 'TI', '1098456018', 'Emma', 'Victoria', 'Medina', 'Vega', '2014-11-30', 'F', 'Carrera 17 # 134-80, Bogota', '3163456789', 'emma.medina@lh.edu.co', 'O+', 'Ninguna', 'Ninguna', '2023-01-19', 'Activo'),
(19, 'TI', '1098456019', 'Joaquin', 'Alonso', 'Duque', 'Bernal', '2010-02-17', 'M', 'Carrera 58 # 127-45, Bogota', '3185678901', 'joaquin.duque@lh.edu.co', 'A+', 'Ninguna', 'Ninguna', '2020-01-20', 'Activo'),
(20, 'TI', '1098456020', 'Antonella', 'Maria', 'Ramirez', 'Rodriguez', '2016-08-12', 'F', 'Calle 127 # 45-12, Bogota', '3104567890', 'antonella.ramirez@lh.edu.co', 'O+', 'Ninguna', 'Ninguna', '2023-01-19', 'Activo'),
(21, 'TI', '1098456021', 'Matias', 'Ignacio', 'Gonzalez', 'Lopez', '2015-12-01', 'M', 'Avenida Suba # 116-20, Bogota', '3182345678', 'matias.gonzalez@lh.edu.co', 'A+', 'Ninguna', 'Ninguna', '2022-01-17', 'Activo'),
(22, 'TI', '1098456022', 'Juliana', 'Marcela', 'Hernandez', 'Martinez', '2007-01-19', 'F', 'Carrera 7 # 140-50, Bogota', '3167890123', 'juliana.hernandez@lh.edu.co', 'B+', 'Ninguna', 'Astigmatismo', '2017-01-23', 'Activo'),
(23, 'TI', '1098456023', 'Emiliano', 'Tomas', 'Garcia', 'Sanchez', '2012-10-08', 'M', 'Carrera 58 # 134-22, Bogota', '3112345678', 'emiliano.garcia@lh.edu.co', 'O+', 'Pasto', 'Ninguna', '2021-01-18', 'Activo'),
(24, 'TI', '1098456024', 'Valery', 'Juliana', 'Diaz', 'Torres', '2008-05-14', 'F', 'Carrera 9 # 106-15, Bogota', '3190123456', 'valery.diaz@lh.edu.co', 'AB+', 'Ninguna', 'Ninguna', '2018-01-25', 'Activo'),
(25, 'TI', '1098456025', 'Jeromino', 'David', 'Rojas', 'Vargas', '2011-09-27', 'M', 'Carrera 11 # 93-20, Bogota', '3139012345', 'jeronimo.rojas@lh.edu.co', 'A-', 'Picadura Abeja', 'Ninguna', '2021-01-18', 'Activo'),
(26, 'TI', '1098456026', 'Catalina', 'Ines', 'Castro', 'Mendoza', '2016-03-03', 'F', 'Carrera 19 # 122-10, Bogota', '3161234567', 'catalina.castro@lh.edu.co', 'O-', 'Ninguna', 'Ninguna', '2023-01-19', 'Activo'),
(27, 'TI', '1098456027', 'Tomas', 'Felipe', 'Morales', 'Ortiz', '2013-07-15', 'M', 'Carrera 45 # 104-50, Bogota', '3183456789', 'tomas.morales@lh.edu.co', 'O+', 'Ninguna', 'Ninguna', '2022-01-17', 'Activo'),
(28, 'TI', '1098456028', 'Mia', 'Celeste', 'Gutierrez', 'Alvarez', '2014-04-20', 'F', 'Carrera 7A # 124-30, Bogota', '3105678901', 'mia.gutierrez@lh.edu.co', 'B+', 'Ninguna', 'Ninguna', '2022-01-17', 'Activo'),
(29, 'TI', '1098456029', 'Simon', 'Pedro', 'Reyes', 'Herrera', '2009-08-31', 'M', 'Carrera 21 # 86-45, Bogota', '3127890123', 'simon.reyes@lh.edu.co', 'A+', 'Ninguna', 'Ninguna', '2019-01-22', 'Activo'),
(30, 'TI', '1098456030', 'Alicia', 'Beatriz', 'Jimenez', 'Castillo', '2015-01-25', 'F', 'Carrera 13 # 118-25, Bogota', '3149012345', 'alicia.jimenez@lh.edu.co', 'O+', 'Ninguna', 'Ninguna', '2023-01-19', 'Activo'),
(31, 'TI', '1098456031', 'Benjamin', 'Gael', 'Perez', 'Silva', '2008-12-07', 'M', 'Carrera 50 # 128-15, Bogota', '3162345678', 'benjamin.perez@lh.edu.co', 'O+', 'Ninguna', 'Ninguna', '2018-01-25', 'Activo'),
(32, 'TI', '1098456032', 'Guadalupe', 'Sol', 'Romero', 'Suarez', '2016-11-14', 'F', 'Carrera 15 # 102-18, Bogota', '3184567890', 'guadalupe.romero@lh.edu.co', 'A+', 'Ninguna', 'Ninguna', '2023-01-19', 'Activo'),
(33, 'TI', '1098456033', 'Cristobal', 'Leon', 'Cardenas', 'Pena', '2012-02-18', 'M', 'Carrera 9 # 147-30, Bogota', '3106789012', 'cristobal.cardenas@lh.edu.co', 'B+', 'Polvo', 'Ninguna', '2021-01-18', 'Activo'),
(34, 'TI', '1098456034', 'Elena', 'Sofia', 'Lozano', 'Bermudez', '2010-09-04', 'F', 'Carrera 7 # 155-40, Bogota', '3128901234', 'elena.lozano@lh.edu.co', 'O+', 'Ninguna', 'Ninguna', '2020-01-20', 'Activo'),
(35, 'TI', '1098456035', 'Agustin', 'Valentin', 'Montoya', 'Salazar', '2014-06-12', 'M', 'Carrera 11 # 100-10, Bogota', '3140123456', 'agustin.montoya@lh.edu.co', 'AB-', 'Ninguna', 'Ninguna', '2022-01-17', 'Activo');

-- 6. Relación Estudiante - Acudiente
INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) VALUES
(1, 1, TRUE, TRUE),
(2, 2, TRUE, TRUE),
(3, 3, TRUE, TRUE),
(4, 4, TRUE, TRUE),
(5, 5, TRUE, TRUE),
(6, 6, TRUE, TRUE),
(7, 7, TRUE, TRUE),
(8, 8, TRUE, TRUE),
(9, 9, TRUE, TRUE),
(10, 10, TRUE, TRUE),
(11, 11, TRUE, TRUE),
(12, 12, TRUE, TRUE),
(13, 13, TRUE, TRUE),
(14, 14, TRUE, TRUE),
(15, 15, TRUE, TRUE),
(16, 16, TRUE, TRUE),
(17, 17, TRUE, TRUE),
(18, 18, TRUE, TRUE),
(19, 19, TRUE, TRUE),
(20, 20, TRUE, TRUE),
(21, 21, TRUE, TRUE),
(22, 22, TRUE, TRUE),
(23, 23, TRUE, TRUE),
(24, 24, TRUE, TRUE),
(25, 25, TRUE, TRUE),
(26, 26, TRUE, TRUE),
(27, 27, TRUE, TRUE),
(28, 28, TRUE, TRUE),
(29, 29, TRUE, TRUE),
(30, 30, TRUE, TRUE),
(31, 31, TRUE, TRUE),
(32, 32, TRUE, TRUE),
(33, 33, TRUE, TRUE),
(34, 34, TRUE, TRUE),
(35, 35, TRUE, TRUE);

-- 7. Tabla de Matrículas (Año Académico 2024)
INSERT INTO matriculas (id_matricula, id_estudiante, id_grado, año_academico, estado_matricula, fecha_matricula) VALUES
(1, 1, 9, 2024, 'Activa', '2024-01-15'),
(2, 2, 7, 2024, 'Activa', '2024-01-15'),
(3, 3, 10, 2024, 'Activa', '2024-01-15'),
(4, 4, 8, 2024, 'Activa', '2024-01-15'),
(5, 5, 11, 2024, 'Activa', '2024-01-15'),
(6, 6, 6, 2024, 'Activa', '2024-01-15'),
(7, 7, 12, 2024, 'Activa', '2024-01-15'),
(8, 8, 5, 2024, 'Activa', '2024-01-15'),
(9, 9, 10, 2024, 'Activa', '2024-01-15'),
(10, 10, 9, 2024, 'Activa', '2024-01-15'),
(11, 11, 4, 2024, 'Activa', '2024-01-15'),
(12, 12, 11, 2024, 'Activa', '2024-01-15'),
(13, 13, 8, 2024, 'Activa', '2024-01-15'),
(14, 14, 7, 2024, 'Activa', '2024-01-15'),
(15, 15, 12, 2024, 'Activa', '2024-01-15'),
(16, 16, 6, 2024, 'Activa', '2024-01-15'),
(17, 17, 10, 2024, 'Activa', '2024-01-15'),
(18, 18, 5, 2024, 'Activa', '2024-01-15'),
(19, 19, 9, 2024, 'Activa', '2024-01-15'),
(20, 20, 3, 2024, 'Activa', '2024-01-15'),
(21, 21, 4, 2024, 'Activa', '2024-01-15'),
(22, 22, 12, 2024, 'Activa', '2024-01-15'),
(23, 23, 7, 2024, 'Activa', '2024-01-15'),
(24, 24, 11, 2024, 'Activa', '2024-01-15'),
(25, 25, 8, 2024, 'Activa', '2024-01-15'),
(26, 26, 3, 2024, 'Activa', '2024-01-15'),
(27, 27, 6, 2024, 'Activa', '2024-01-15'),
(28, 28, 5, 2024, 'Activa', '2024-01-15'),
(29, 29, 10, 2024, 'Activa', '2024-01-15'),
(30, 30, 4, 2024, 'Activa', '2024-01-15'),
(31, 31, 11, 2024, 'Activa', '2024-01-15'),
(32, 32, 2, 2024, 'Activa', '2024-01-15'),
(33, 33, 7, 2024, 'Activa', '2024-01-15'),
(34, 34, 9, 2024, 'Activa', '2024-01-15'),
(35, 35, 5, 2024, 'Activa', '2024-01-15');

-- 8. Tabla de Profesores (32 registros)
INSERT INTO profesores (id_profesor, tipo_documento, numero_documento, primer_nombre, segundo_nombre, primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) VALUES
(1, 'CC', '79456101', 'Alvaro', 'Enrique', 'Monroy', 'Beltran', 'alvaro.monroy@littlehorizons.edu.co', '3102345678', 'Matemáticas y Cálculo', '2015-01-10', 'Activo'),
(2, 'CC', '79456102', 'Martha', 'Cecilia', 'Parra', 'Guzman', 'martha.parra@littlehorizons.edu.co', '3113456789', 'Lengua Castellana y Literatura', '2016-02-15', 'Activo'),
(3, 'CC', '79456103', 'Fabio', 'Nelson', 'Cifuentes', 'Soto', 'fabio.cifuentes@littlehorizons.edu.co', '3124567890', 'Física y Termodinámica', '2014-07-20', 'Activo'),
(4, 'CC', '79456104', 'Claudia', 'Patricia', 'Benavides', 'Mora', 'claudia.benavides@littlehorizons.edu.co', '3135678901', 'Química General y Orgánica', '2017-01-12', 'Activo'),
(5, 'CC', '79456105', 'Jorge', 'Eliecer', 'Amaya', 'Villamil', 'jorge.amaya@littlehorizons.edu.co', '3146789012', 'Ciencias Naturales y Biología', '2018-08-01', 'Activo'),
(6, 'CC', '79456106', 'Sonia', 'Esperanza', 'Buitrago', 'Cardona', 'sonia.buitrago@littlehorizons.edu.co', '3157890123', 'Ciencias Sociales e Historia', '2015-03-01', 'Activo'),
(7, 'CC', '79456107', 'Hector', 'Julio', 'Castaneda', 'Pinzon', 'hector.castaneda@littlehorizons.edu.co', '3168901234', 'Filosofía y Ética', '2016-09-15', 'Activo'),
(8, 'CC', '79456108', 'Myriam', 'Lucia', 'Garzon', 'Pardo', 'myriam.garzon@littlehorizons.edu.co', '3179012345', 'Inglés Académico C1', '2013-01-15', 'Activo'),
(9, 'CC', '79456109', 'Cesar', 'Augusto', 'Rincon', 'Chaparro', 'cesar.rincon@littlehorizons.edu.co', '3180123456', 'Educación Física y Deportes', '2019-01-20', 'Activo'),
(10, 'CC', '79456110', 'Dora', 'Ines', 'Valbuena', 'Triana', 'dora.valbuena@littlehorizons.edu.co', '3191234567', 'Educación Artística y Plástica', '2018-02-01', 'Activo'),
(11, 'CC', '79456111', 'Hernando', 'Jose', 'Nieto', 'Baquero', 'hernando.nieto@littlehorizons.edu.co', '3102345688', 'Tecnología e Informática', '2017-06-15', 'Activo'),
(12, 'CC', '79456112', 'Luz', 'Stella', 'Avila', 'Moreno', 'luz.avila@littlehorizons.edu.co', '3113456799', 'Educación Musical', '2016-01-18', 'Activo'),
(13, 'CC', '79456113', 'Guillermo', 'Leon', 'Barrera', 'Forero', 'guillermo.barrera@littlehorizons.edu.co', '3124567800', 'Geografía y Ciencias Políticas', '2015-08-10', 'Activo'),
(14, 'CC', '79456114', 'Esperanza', 'Maria', 'Corredor', 'Bustos', 'esperanza.corredor@littlehorizons.edu.co', '3135678911', 'Pedagogía Infantil', '2012-01-20', 'Activo'),
(15, 'CC', '79456115', 'Rodrigo', 'Alfonso', 'Duque', 'Cadena', 'rodrigo.duque@littlehorizons.edu.co', '3146789022', 'Estadística y Trigonometría', '2018-01-15', 'Activo'),
(16, 'CC', '79456116', 'Gloria', 'Ines', 'Echeverri', 'Zapata', 'gloria.echeverri@littlehorizons.edu.co', '3157890133', 'Lengua Francesa B2', '2019-07-01', 'Activo'),
(17, 'CC', '79456117', 'Mauricio', 'Andres', 'Franco', 'Ochoa', 'mauricio.franco@littlehorizons.edu.co', '3168901244', 'Robótica y Programación', '2020-01-15', 'Activo'),
(18, 'CC', '79456118', 'Sandra', 'Patricia', 'Gomez', 'Lleras', 'sandra.gomez@littlehorizons.edu.co', '3179012355', 'Ciencias Ambientales y Ecología', '2017-04-10', 'Activo'),
(19, 'CC', '79456119', 'Oscar', 'Javier', 'Hurtado', 'Mejia', 'oscar.hurtado@littlehorizons.edu.co', '3180123466', 'Educación Religiosa y Valores', '2016-10-01', 'Activo'),
(20, 'CC', '79456120', 'Adriana', 'Lucia', 'Ibanez', 'Naranjo', 'adriana.ibanez@littlehorizons.edu.co', '3191234577', 'Danza y Expresión Corporal', '2021-01-20', 'Activo'),
(21, 'CC', '79456121', 'Jaime', 'Alberto', 'Jaramillo', 'Osorio', 'jaime.jaramillo@littlehorizons.edu.co', '3103456789', 'Teatro y Artes Escénicas', '2018-09-01', 'Activo'),
(22, 'CC', '79456122', 'Beatriz', 'Eugenia', 'Londono', 'Palacio', 'beatriz.londono@littlehorizons.edu.co', '3114567890', 'Comprensión Lectora y Escritura', '2015-05-15', 'Activo'),
(23, 'CC', '79456123', 'Carlos', 'Julio', 'Maldonado', 'Quintero', 'carlos.maldonado@littlehorizons.edu.co', '3125678901', 'Ajedrez y Lógica Matemática', '2019-02-15', 'Activo'),
(24, 'CC', '79456124', 'Maria', 'Victoria', 'Norena', 'Restrepo', 'maria.norena@littlehorizons.edu.co', '3136789012', 'Inglés Primaria A2', '2017-02-01', 'Activo'),
(25, 'CC', '79456125', 'Pedro', 'Antonio', 'Ospina', 'Sierra', 'pedro.ospina@littlehorizons.edu.co', '3147890123', 'Fútbol y Acondicionamiento', '2020-08-01', 'Activo'),
(26, 'CC', '79456126', 'Consuelo', 'del Pilar', 'Pardo', 'Tafur', 'consuelo.pardo@littlehorizons.edu.co', '3158901234', 'Biología Celular', '2014-01-15', 'Activo'),
(27, 'CC', '79456127', 'Albeiro', 'de Jesus', 'Quintero', 'Uribe', 'albeiro.quintero@littlehorizons.edu.co', '3169012345', 'Matemáticas Básicas Primaria', '2013-08-01', 'Activo'),
(28, 'CC', '79456128', 'Nelly', 'Amanda', 'Rios', 'Velandia', 'nelly.rios@littlehorizons.edu.co', '3170123456', 'Ciencias Sociales Primaria', '2016-03-20', 'Activo'),
(29, 'CC', '79456129', 'Gonzalo', 'Adolfo', 'Salgado', 'Wills', 'gonzalo.salgado@littlehorizons.edu.co', '3181234567', 'Química Analítica', '2018-07-15', 'Activo'),
(30, 'CC', '79456130', 'Clara', 'Ines', 'Toro', 'Yepes', 'clara.toro@littlehorizons.edu.co', '3192345678', 'Psicorientación Escolar', '2015-11-01', 'Activo'),
(31, 'CC', '79456131', 'Bernardo', 'Jose', 'Uribe', 'Zuluaga', 'bernardo.uribe@littlehorizons.edu.co', '3104567891', 'Física Mecánica', '2019-03-01', 'Activo'),
(32, 'CC', '79456132', 'Olga', 'Marina', 'Villegas', 'Arango', 'olga.villegas@littlehorizons.edu.co', '3115678902', 'Economía y Gestión Empresarial', '2020-02-15', 'Activo');

-- 9. Tabla de Materias (36 registros)
INSERT INTO materias (id_materia, codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) VALUES
(1, 'DIM-COGN-0', 'Dimensión Cognitiva', 'Desarrollo de habilidades del pensamiento inicial', 1, 4, TRUE),
(2, 'DIM-COMU-0', 'Dimensión Comunicativa', 'Expresión oral, cuentos y motricidad fonética', 1, 4, TRUE),
(3, 'DIM-CORP-0', 'Dimensión Corporal', 'Desarrollo motor grueso y fino y lúdica', 1, 3, TRUE),
(4, 'MAT-1', 'Matemáticas 1', 'Números cardinales, sumas sencillas y figuras geométricas', 2, 5, TRUE),
(5, 'ESP-1', 'Español y Lectoescritura 1', 'Fonética, iniciación a la lectura y escritura', 2, 5, TRUE),
(6, 'ING-1', 'Inglés Infantil 1', 'Vocabulario básico, canciones y saludos en inglés', 2, 3, TRUE),
(7, 'MAT-2', 'Matemáticas 2', 'Operaciones básicas, decenas y resolución de problemas', 3, 5, TRUE),
(8, 'ESP-2', 'Español 2', 'Comprensión lectora, fábulas y reglas ortográficas iniciales', 3, 5, TRUE),
(9, 'SOC-2', 'Ciencias Sociales 2', 'El barrio, la comunidad escolar y símbolos patrios', 3, 3, TRUE),
(10, 'MAT-3', 'Matemáticas 3', 'Tablas de multiplicar, división y medidas', 4, 5, TRUE),
(11, 'NAT-3', 'Ciencias Naturales 3', 'Ecosistemas, reino animal y plantas', 4, 4, TRUE),
(12, 'ING-3', 'Inglés 3', 'Estructuras gramaticales básicas y conversación cotidiana', 4, 3, TRUE),
(13, 'MAT-4', 'Matemáticas 4', 'Fracciones, números decimales y geometría básica', 5, 5, TRUE),
(14, 'ESP-4', 'Lengua Castellana 4', 'Producción de textos narrativos e informativos', 5, 4, TRUE),
(15, 'SOC-4', 'Ciencias Sociales 4', 'Geografía e historia de Colombia precolombina', 5, 3, TRUE),
(16, 'MAT-5', 'Matemáticas 5', 'Potenciación, regla de tres y geometría euclidiana', 6, 5, TRUE),
(17, 'NAT-5', 'Ciencias Naturales 5', 'El cuerpo humano, sistemas orgánicos y nutrición', 6, 4, TRUE),
(18, 'ING-5', 'Inglés 5', 'Nivel A2.2 del MCER, redacción de párrafos y listening', 6, 4, TRUE),
(19, 'MAT-6', 'Matemáticas 6', 'Teoría de números, enteros y proporcionalidad', 7, 5, TRUE),
(20, 'ESP-6', 'Lengua Castellana 6', 'Género lírico, dramático y análisis literario', 7, 4, TRUE),
(21, 'SOC-6', 'Ciencias Sociales 6', 'Civilizaciones fluviales antiguas y geografía universal', 7, 3, TRUE),
(22, 'MAT-7', 'Matemáticas 7', 'Números racionales, proporcionalidad y álgebra introductoria', 8, 5, TRUE),
(23, 'BIO-7', 'Biología 7', 'Biología celular, genética mendeliana y taxonomía', 8, 4, TRUE),
(24, 'ING-7', 'Inglés 7', 'Tiempos verbales compuestos y proyectos orales', 8, 4, TRUE),
(25, 'ALG-8', 'Álgebra 8', 'Polinomios, productos notables y factorización', 9, 5, TRUE),
(26, 'ESP-8', 'Lengua Castellana 8', 'Literatura de la Colonia, ensayo argumentativo', 9, 4, TRUE),
(27, 'SOC-8', 'Historia y Geografía 8', 'Revolución Industrial y descolonización americana', 9, 3, TRUE),
(28, 'ALG-9', 'Álgebra y Geometría 9', 'Sistemas de ecuaciones lineales y funciones cuadráticas', 10, 5, TRUE),
(29, 'QUI-9', 'Química 9', 'Estructura atómica, tabla periódica y enlaces químicos', 10, 4, TRUE),
(30, 'ING-9', 'Inglés 9', 'Nivel B1: debates, presentaciones formales y ensayos', 10, 4, TRUE),
(31, 'TRI-10', 'Trigonometría y Analítica 10', 'Funciones trigonométricas, identidades y cónicas', 11, 4, TRUE),
(32, 'FIS-10', 'Física Mecánica 10', 'Cinemática, leyes de Newton, trabajo y energía', 11, 4, TRUE),
(33, 'QUI-10', 'Química Inorgánica 10', 'Estequiometría, gases ideales y soluciones', 11, 4, TRUE),
(34, 'CAL-11', 'Cálculo Diferencial e Integral 11', 'Límites, derivadas, integrales y aplicaciones', 12, 4, TRUE),
(35, 'FIS-11', 'Física Electromagnética 11', 'Electricidad, circuitos, ondas y óptica', 12, 4, TRUE),
(36, 'FIL-11', 'Filosofía Contemporánea 11', 'Epistemología, ética ciudadana y bioética', 12, 3, TRUE);

-- 10. Asignaciones de Profesores a Materias
INSERT INTO asignaciones_profesor (id_asignacion, id_profesor, id_materia, año_academico, seccion) VALUES
(1, 1, 1, 2024, 'A'),
(2, 2, 2, 2024, 'A'),
(3, 3, 3, 2024, 'A'),
(4, 4, 4, 2024, 'A'),
(5, 5, 5, 2024, 'A'),
(6, 6, 6, 2024, 'A'),
(7, 7, 7, 2024, 'A'),
(8, 8, 8, 2024, 'A'),
(9, 9, 9, 2024, 'A'),
(10, 10, 10, 2024, 'A'),
(11, 11, 11, 2024, 'A'),
(12, 12, 12, 2024, 'A'),
(13, 13, 13, 2024, 'A'),
(14, 14, 14, 2024, 'A'),
(15, 15, 15, 2024, 'A'),
(16, 16, 16, 2024, 'A'),
(17, 17, 17, 2024, 'A'),
(18, 18, 18, 2024, 'A'),
(19, 19, 19, 2024, 'A'),
(20, 20, 20, 2024, 'A'),
(21, 21, 21, 2024, 'A'),
(22, 22, 22, 2024, 'A'),
(23, 23, 23, 2024, 'A'),
(24, 24, 24, 2024, 'A'),
(25, 25, 25, 2024, 'A'),
(26, 26, 26, 2024, 'A'),
(27, 27, 27, 2024, 'A'),
(28, 28, 28, 2024, 'A'),
(29, 29, 29, 2024, 'A'),
(30, 30, 30, 2024, 'A'),
(31, 31, 31, 2024, 'A'),
(32, 32, 32, 2024, 'A'),
(33, 1, 33, 2024, 'A'),
(34, 2, 34, 2024, 'A'),
(35, 3, 35, 2024, 'A'),
(36, 4, 36, 2024, 'A');

-- 11. Inscripciones de Materia
INSERT INTO inscripciones_materia (id_inscripcion, id_estudiante, id_materia, año_academico, periodo_academico, seccion, fecha_inscripcion, estado) VALUES
(1, 1, 25, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(2, 1, 26, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(3, 2, 19, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(4, 2, 20, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(5, 3, 28, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(6, 3, 29, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(7, 4, 22, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(8, 4, 23, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(9, 5, 31, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(10, 5, 32, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(11, 6, 16, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(12, 6, 17, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(13, 7, 34, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(14, 7, 35, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(15, 8, 13, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(16, 8, 14, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(17, 9, 28, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(18, 9, 29, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(19, 10, 25, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(20, 10, 26, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(21, 11, 10, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(22, 11, 11, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(23, 12, 31, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(24, 12, 32, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(25, 13, 22, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(26, 13, 23, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(27, 14, 19, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(28, 14, 20, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(29, 15, 34, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(30, 15, 35, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(31, 16, 16, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(32, 16, 17, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(33, 17, 28, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(34, 17, 29, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(35, 18, 13, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(36, 18, 14, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(37, 19, 25, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(38, 19, 26, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(39, 20, 7, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(40, 20, 8, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(41, 21, 10, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(42, 21, 11, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(43, 22, 34, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(44, 22, 35, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(45, 23, 19, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(46, 23, 20, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(47, 24, 31, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(48, 24, 32, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(49, 25, 22, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(50, 25, 23, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(51, 26, 7, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(52, 26, 8, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(53, 27, 16, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(54, 27, 17, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(55, 28, 13, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(56, 28, 14, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(57, 29, 28, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(58, 29, 29, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(59, 30, 10, 2024, 'I', 'A', '2024-01-22', 'Activa'),
(60, 30, 11, 2024, 'I', 'A', '2024-01-22', 'Activa');

-- 12. Tabla de Registros Académicos (45 notas)
INSERT INTO registros_academicos (id_registro, id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, observaciones, fecha_registro) VALUES
(1, 2, 19, 19, 2024, 'I', 4.3, 'Evaluación de competencias primer periodo', '2024-03-25'),
(2, 3, 28, 28, 2024, 'I', 3.1, 'Evaluación de competencias primer periodo', '2024-03-25'),
(3, 4, 22, 22, 2024, 'I', 3.6, 'Evaluación de competencias primer periodo', '2024-03-25'),
(4, 5, 31, 31, 2024, 'I', 3.4, 'Evaluación de competencias primer periodo', '2024-03-25'),
(5, 6, 16, 16, 2024, 'I', 4.5, 'Evaluación de competencias primer periodo', '2024-03-25'),
(6, 7, 34, 2, 2024, 'I', 4.4, 'Evaluación de competencias primer periodo', '2024-03-25'),
(7, 8, 13, 13, 2024, 'I', 4.8, 'Evaluación de competencias primer periodo', '2024-03-25'),
(8, 9, 28, 28, 2024, 'I', 3.2, 'Evaluación de competencias primer periodo', '2024-03-25'),
(9, 10, 25, 25, 2024, 'I', 3.8, 'Evaluación de competencias primer periodo', '2024-03-25'),
(10, 11, 10, 10, 2024, 'I', 3.1, 'Evaluación de competencias primer periodo', '2024-03-25'),
(11, 12, 31, 31, 2024, 'I', 3.4, 'Evaluación de competencias primer periodo', '2024-03-25'),
(12, 13, 22, 22, 2024, 'I', 4.0, 'Evaluación de competencias primer periodo', '2024-03-25'),
(13, 14, 19, 19, 2024, 'I', 3.1, 'Evaluación de competencias primer periodo', '2024-03-25'),
(14, 15, 34, 2, 2024, 'I', 3.4, 'Evaluación de competencias primer periodo', '2024-03-25'),
(15, 16, 16, 16, 2024, 'I', 4.3, 'Evaluación de competencias primer periodo', '2024-03-25'),
(16, 17, 28, 28, 2024, 'I', 4.1, 'Evaluación de competencias primer periodo', '2024-03-25'),
(17, 18, 13, 13, 2024, 'I', 3.4, 'Evaluación de competencias primer periodo', '2024-03-25'),
(18, 19, 25, 25, 2024, 'I', 4.2, 'Evaluación de competencias primer periodo', '2024-03-25'),
(19, 20, 7, 7, 2024, 'I', 4.6, 'Evaluación de competencias primer periodo', '2024-03-25'),
(20, 21, 10, 10, 2024, 'I', 3.0, 'Evaluación de competencias primer periodo', '2024-03-25'),
(21, 22, 34, 2, 2024, 'I', 4.6, 'Evaluación de competencias primer periodo', '2024-03-25'),
(22, 23, 19, 19, 2024, 'I', 4.4, 'Evaluación de competencias primer periodo', '2024-03-25'),
(23, 24, 31, 31, 2024, 'I', 3.7, 'Evaluación de competencias primer periodo', '2024-03-25'),
(24, 25, 22, 22, 2024, 'I', 3.3, 'Evaluación de competencias primer periodo', '2024-03-25'),
(25, 26, 7, 7, 2024, 'I', 4.9, 'Evaluación de competencias primer periodo', '2024-03-25'),
(26, 27, 16, 16, 2024, 'I', 3.7, 'Evaluación de competencias primer periodo', '2024-03-25'),
(27, 28, 13, 13, 2024, 'I', 3.2, 'Evaluación de competencias primer periodo', '2024-03-25'),
(28, 29, 28, 28, 2024, 'I', 3.2, 'Evaluación de competencias primer periodo', '2024-03-25'),
(29, 30, 10, 10, 2024, 'I', 4.7, 'Evaluación de competencias primer periodo', '2024-03-25'),
(30, 31, 31, 31, 2024, 'I', 4.2, 'Evaluación de competencias primer periodo', '2024-03-25'),
(31, 32, 4, 4, 2024, 'I', 4.6, 'Evaluación de competencias primer periodo', '2024-03-25'),
(32, 33, 19, 19, 2024, 'I', 4.5, 'Evaluación de competencias primer periodo', '2024-03-25'),
(33, 34, 25, 25, 2024, 'I', 4.1, 'Evaluación de competencias primer periodo', '2024-03-25'),
(34, 35, 13, 13, 2024, 'I', 4.9, 'Evaluación de competencias primer periodo', '2024-03-25'),
(35, 1, 25, 25, 2024, 'I', 3.8, 'Evaluación de competencias primer periodo', '2024-03-25'),
(36, 2, 19, 19, 2024, 'I', 4.1, 'Evaluación de competencias primer periodo', '2024-03-25'),
(37, 3, 28, 28, 2024, 'I', 4.7, 'Evaluación de competencias primer periodo', '2024-03-25'),
(38, 4, 22, 22, 2024, 'I', 4.2, 'Evaluación de competencias primer periodo', '2024-03-25'),
(39, 5, 31, 31, 2024, 'I', 4.7, 'Evaluación de competencias primer periodo', '2024-03-25'),
(40, 6, 16, 16, 2024, 'I', 4.2, 'Evaluación de competencias primer periodo', '2024-03-25'),
(41, 7, 34, 2, 2024, 'I', 4.4, 'Evaluación de competencias primer periodo', '2024-03-25'),
(42, 8, 13, 13, 2024, 'I', 3.1, 'Evaluación de competencias primer periodo', '2024-03-25'),
(43, 9, 28, 28, 2024, 'I', 3.5, 'Evaluación de competencias primer periodo', '2024-03-25'),
(44, 10, 25, 25, 2024, 'I', 3.6, 'Evaluación de competencias primer periodo', '2024-03-25'),
(45, 11, 10, 10, 2024, 'I', 3.2, 'Evaluación de competencias primer periodo', '2024-03-25');

-- 13. Tabla de Asistencias
INSERT INTO asistencias (id_asistencia, id_estudiante, id_materia, fecha_asistencia, estado_asistencia, observaciones, registrado_por) VALUES
(1, 2, 19, '2024-03-11', 'Presente', 'Registro de control matutino', 19),
(2, 3, 28, '2024-03-12', 'Presente', 'Registro de control matutino', 28),
(3, 4, 22, '2024-03-13', 'Tardanza', 'Registro de control matutino', 22),
(4, 5, 31, '2024-03-14', 'Ausente', 'Registro de control matutino', 31),
(5, 6, 16, '2024-03-15', 'Excusa Médica', 'Registro de control matutino', 16),
(6, 7, 34, '2024-03-16', 'Presente', 'Registro de control matutino', 2),
(7, 8, 13, '2024-03-17', 'Presente', 'Registro de control matutino', 13),
(8, 9, 28, '2024-03-18', 'Presente', 'Registro de control matutino', 28),
(9, 10, 25, '2024-03-10', 'Tardanza', 'Registro de control matutino', 25),
(10, 11, 10, '2024-03-11', 'Ausente', 'Registro de control matutino', 10),
(11, 12, 31, '2024-03-12', 'Excusa Médica', 'Registro de control matutino', 31),
(12, 13, 22, '2024-03-13', 'Presente', 'Registro de control matutino', 22),
(13, 14, 19, '2024-03-14', 'Presente', 'Registro de control matutino', 19),
(14, 15, 34, '2024-03-15', 'Presente', 'Registro de control matutino', 2),
(15, 16, 16, '2024-03-16', 'Tardanza', 'Registro de control matutino', 16),
(16, 17, 28, '2024-03-17', 'Ausente', 'Registro de control matutino', 28),
(17, 18, 13, '2024-03-18', 'Excusa Médica', 'Registro de control matutino', 13),
(18, 19, 25, '2024-03-10', 'Presente', 'Registro de control matutino', 25),
(19, 20, 7, '2024-03-11', 'Presente', 'Registro de control matutino', 7),
(20, 21, 10, '2024-03-12', 'Presente', 'Registro de control matutino', 10),
(21, 22, 34, '2024-03-13', 'Tardanza', 'Registro de control matutino', 2),
(22, 23, 19, '2024-03-14', 'Ausente', 'Registro de control matutino', 19),
(23, 24, 31, '2024-03-15', 'Excusa Médica', 'Registro de control matutino', 31),
(24, 25, 22, '2024-03-16', 'Presente', 'Registro de control matutino', 22),
(25, 26, 7, '2024-03-17', 'Presente', 'Registro de control matutino', 7),
(26, 27, 16, '2024-03-18', 'Presente', 'Registro de control matutino', 16),
(27, 28, 13, '2024-03-10', 'Tardanza', 'Registro de control matutino', 13),
(28, 29, 28, '2024-03-11', 'Ausente', 'Registro de control matutino', 28),
(29, 30, 10, '2024-03-12', 'Excusa Médica', 'Registro de control matutino', 10),
(30, 31, 31, '2024-03-13', 'Presente', 'Registro de control matutino', 31),
(31, 32, 4, '2024-03-14', 'Presente', 'Registro de control matutino', 4),
(32, 33, 19, '2024-03-15', 'Presente', 'Registro de control matutino', 19),
(33, 34, 25, '2024-03-16', 'Tardanza', 'Registro de control matutino', 25),
(34, 35, 13, '2024-03-17', 'Ausente', 'Registro de control matutino', 13),
(35, 1, 25, '2024-03-18', 'Excusa Médica', 'Registro de control matutino', 25),
(36, 2, 19, '2024-03-10', 'Presente', 'Registro de control matutino', 19),
(37, 3, 28, '2024-03-11', 'Presente', 'Registro de control matutino', 28),
(38, 4, 22, '2024-03-12', 'Presente', 'Registro de control matutino', 22),
(39, 5, 31, '2024-03-13', 'Tardanza', 'Registro de control matutino', 31),
(40, 6, 16, '2024-03-14', 'Ausente', 'Registro de control matutino', 16);

-- 14. Tabla de Clubes (30 registros)
INSERT INTO clubes (id_club, nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, esta_activo) VALUES
(1, 'Club de Robótica y Automatización', 'Diseño, ensamblaje y programación de robots con Arduino y LEGO Mindstorms', 17, 25, 'Martes 3:00 - 5:00 PM', TRUE),
(2, 'Club de Astronomía y Cosmología', 'Observación telescópica nocturna, astrofísica divulgativa y navegación estelar', 3, 20, 'Jueves 4:00 - 6:00 PM', TRUE),
(3, 'Club de Ajedrez Little Horizons', 'Estrategia, táctica competitiva, aperturas y preparación para torneos distritales', 23, 30, 'Lunes y Miércoles 3:30 - 5:00 PM', TRUE),
(4, 'Club de Artes Plásticas y Óleo', 'Pintura al óleo, acuarela, escultura en arcilla y técnicas mixtas', 10, 20, 'Viernes 3:00 - 5:00 PM', TRUE),
(5, 'Orquesta y Ensamble Musical', 'Interpretación de cuerdas, vientos, percusión y armonía musical', 12, 35, 'Lunes a Jueves 4:00 - 5:30 PM', TRUE),
(6, 'Club de Danza Folclórica y Moderna', 'Expresión rítmica, bailes tradicionales colombianos y danza contemporánea', 20, 25, 'Martes y Jueves 3:30 - 5:00 PM', TRUE),
(7, 'Compañía de Teatro y Dramaturgia', 'Actuación, improvisación teatral, montaje escénico y vocalización', 21, 20, 'Miércoles 3:00 - 5:30 PM', TRUE),
(8, 'Club de Debate y Modelo ONU', 'Retórica, oratoria persuasiva, argumentación jurídica y relaciones diplomáticas', 7, 25, 'Jueves 3:30 - 5:30 PM', TRUE),
(9, 'Club de Prensa y Periodismo Escolar', 'Redacción del periódico escolar, entrevistas, fotografía y podcast educativo', 2, 20, 'Lunes 3:00 - 4:30 PM', TRUE),
(10, 'Club de Ciencias y Bioquímica', 'Experimentos de laboratorio, microscopía, cristalografía y biotecnología', 4, 18, 'Martes 3:00 - 5:00 PM', TRUE),
(11, 'Club de Ecología y Huerto Orgánico', 'Siembra urbana, compostaje, conservación de polinizadores y reciclaje', 18, 30, 'Miércoles 3:00 - 4:30 PM', TRUE),
(12, 'Equipo Formativo de Fútbol Masculino', 'Entrenamiento técnico-táctico, acondicionamiento físico y liga intercolegiada', 25, 30, 'Lunes y Viernes 3:30 - 5:30 PM', TRUE),
(13, 'Equipo Formativo de Fútbol Femenino', 'Técnica individual, juego colectivo y preparación competitiva', 25, 30, 'Martes y Jueves 3:30 - 5:30 PM', TRUE),
(14, 'Club de Baloncesto y Enceste', 'Fundamentos de pase, drible, tiro suspendido y jugadas de pizarra', 9, 25, 'Miércoles y Viernes 3:30 - 5:00 PM', TRUE),
(15, 'Club de Voleibol Femenino y Mixto', 'Servicio, voleo, remate y bloqueo en cancha reglamentaria', 9, 25, 'Lunes y Jueves 3:30 - 5:00 PM', TRUE),
(16, 'Club de Natación y Estilos', 'Perfeccionamiento de crol, espalda, pecho, mariposa y resistencia acuática', 9, 20, 'Martes y Jueves 2:30 - 4:30 PM', TRUE),
(17, 'Club de Francés y Cultura Francófona', 'Conversación práctica, gastronomía, cine y literatura francesa', 16, 20, 'Miércoles 3:00 - 4:30 PM', TRUE),
(18, 'Club de Programación y Videojuegos', 'Lógica de desarrollo en Python, diseño de juegos 2D y algoritmos', 11, 25, 'Viernes 3:00 - 5:00 PM', TRUE),
(19, 'Club de Emprendimiento e Innovación', 'Plan de negocios junior, finanzas personales y presentación de startups escolares', 32, 20, 'Lunes 3:30 - 5:00 PM', TRUE),
(20, 'Club de Fotografía Digital y Edición', 'Composición, iluminación, manejo manual de cámaras y revelado digital', 10, 15, 'Martes 3:30 - 5:00 PM', TRUE),
(21, 'Club de Gimnasia Rítmica y Artística', 'Flexibilidad, coordinación motriz con cintas, aros y suelo', 20, 20, 'Lunes y Miércoles 3:00 - 4:30 PM', TRUE),
(22, 'Club de Yoga Infantil y Mindfulness', 'Posturas de concentración, respiración consciente y bienestar emocional', 30, 25, 'Viernes 2:30 - 3:30 PM', TRUE),
(23, 'Club de Cine y Producción Audiovisual', 'Guionismo, rodaje de cortometrajes escolares y apreciación cinematográfica', 21, 20, 'Jueves 3:00 - 5:00 PM', TRUE),
(24, 'Club de Cocina Saludable y Repostería', 'Higiene de alimentos, recetas nutritivas, repostería creativa y cocina fría', 14, 18, 'Miércoles 3:00 - 5:00 PM', TRUE),
(25, 'Club de Filosofía y Café Literario', 'Tertulias literarias, análisis de dilemas morales y lectura de clásicos', 7, 20, 'Viernes 3:30 - 5:00 PM', TRUE),
(26, 'Club de Porrismo y Animación Deportiva', 'Acrobacias seguras, coreografía gimnástica y espíritu deportivo', 20, 25, 'Martes y Jueves 3:30 - 5:00 PM', TRUE),
(27, 'Club de Atletismo y Pruebas de Pista', 'Velocidad, fondo, relevos 4x100 y salto largo', 25, 25, 'Miércoles y Viernes 3:30 - 5:00 PM', TRUE),
(28, 'Club de Patinaje en Línea', 'Técnica de curva, frenado, equilibrio y velocidad en pista de patinaje', 9, 20, 'Lunes y Miércoles 3:00 - 4:30 PM', TRUE),
(29, 'Club de Origami y Geometría en Papel', 'Plegado tradicional japonés, modular y aplicaciones matemáticas espaciales', 1, 20, 'Jueves 3:00 - 4:30 PM', TRUE),
(30, 'Club de Primeros Auxilios y Brigada', 'Técnicas de RCP básico, inmovilización, botiquín escolar y prevención de riesgos', 5, 25, 'Viernes 3:00 - 5:00 PM', TRUE);

-- 15. Tabla de Membresías en Clubes
INSERT INTO membresias_club (id_membresia, id_estudiante, id_club, fecha_ingreso, estado_membresia) VALUES
(1, 1, 1, '2024-02-05', 'Activa'),
(2, 2, 2, '2024-02-05', 'Activa'),
(3, 3, 3, '2024-02-05', 'Activa'),
(4, 4, 4, '2024-02-05', 'Activa'),
(5, 5, 5, '2024-02-05', 'Activa'),
(6, 6, 6, '2024-02-05', 'Activa'),
(7, 7, 7, '2024-02-05', 'Activa'),
(8, 8, 8, '2024-02-05', 'Activa'),
(9, 9, 9, '2024-02-05', 'Activa'),
(10, 10, 10, '2024-02-05', 'Activa'),
(11, 11, 11, '2024-02-05', 'Activa'),
(12, 12, 12, '2024-02-05', 'Activa'),
(13, 13, 13, '2024-02-05', 'Activa'),
(14, 14, 14, '2024-02-05', 'Activa'),
(15, 15, 15, '2024-02-05', 'Activa'),
(16, 16, 16, '2024-02-05', 'Activa'),
(17, 17, 17, '2024-02-05', 'Activa'),
(18, 18, 18, '2024-02-05', 'Activa'),
(19, 19, 19, '2024-02-05', 'Activa'),
(20, 20, 20, '2024-02-05', 'Activa'),
(21, 21, 21, '2024-02-05', 'Activa'),
(22, 22, 22, '2024-02-05', 'Activa'),
(23, 23, 23, '2024-02-05', 'Activa'),
(24, 24, 24, '2024-02-05', 'Activa'),
(25, 25, 25, '2024-02-05', 'Activa'),
(26, 26, 26, '2024-02-05', 'Activa'),
(27, 27, 27, '2024-02-05', 'Activa'),
(28, 28, 28, '2024-02-05', 'Activa'),
(29, 29, 29, '2024-02-05', 'Activa'),
(30, 30, 30, '2024-02-05', 'Activa'),
(31, 31, 1, '2024-02-05', 'Activa'),
(32, 32, 2, '2024-02-05', 'Activa'),
(33, 33, 3, '2024-02-05', 'Activa'),
(34, 34, 4, '2024-02-05', 'Activa'),
(35, 35, 5, '2024-02-05', 'Activa');

-- 16. Tabla de Eventos
INSERT INTO eventos (id_evento, nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, tiene_competencia, id_organizador, esta_activo) VALUES
(1, 'Feria Científica y Tecnológica 2024', 'Exposición anual de proyectos de ciencia, química y robótica', 'Científico', '2024-05-15', 'Polideportivo Central', TRUE, 4, TRUE),
(2, 'Festival Intercolegiado de las Artes y el Teatro', 'Muestra de artes escénicas, música y pintura', 'Cultural', '2024-06-20', 'Auditorio Principal', FALSE, 10, TRUE),
(3, 'Olimpiadas Deportivas Little Horizons', 'Competencias intercursos de fútbol, atletismo y voleibol', 'Deportivo', '2024-08-10', 'Campus Deportivo', TRUE, 9, TRUE),
(4, 'Modelo de Naciones Unidas (LH-MUN)', 'Simulación diplomática de comités internacionales', 'Académico', '2024-09-05', 'Aulas Múltiples', TRUE, 8, TRUE),
(5, 'Celebración del Día de la Familia', 'Encuentro lúdico, bazar y presentaciones artísticas', 'Social', '2024-10-12', 'Canchas y Zonas Verdes', FALSE, 14, TRUE),
(6, 'Torneo Escolar de Ajedrez Rápido', 'Competencia suiza a 5 rondas para primaria y bachillerato', 'Deportivo', '2024-04-18', 'Biblioteca Central', TRUE, 23, TRUE),
(7, 'Jornada del Idioma y la Literatura', 'Concurso de cuento corto, declamación y feria del libro', 'Cultural', '2024-04-23', 'Auditorio Principal', TRUE, 2, TRUE),
(8, 'Feria de Emprendimiento y Creatividad Junior', 'Muestra comercial de microempresas y proyectos escolares', 'Académico', '2024-11-01', 'Plaza Central', TRUE, 32, TRUE),
(9, 'Festival de Música y Concierto de Cuerdas', 'Recital de gala semestral de la orquesta del colegio', 'Cultural', '2024-06-08', 'Teatro Bellas Artes', FALSE, 12, TRUE),
(10, 'Campaña Ambiental y Sembratón Escolar', 'Jornada de arborización y educación ambiental', 'Social', '2024-04-22', 'Reserva Ecológica Escolar', FALSE, 18, TRUE),
(11, 'Spelling Bee and Public Speaking Contest', 'Concurso de deletreo en inglés y debate en lengua extranjera', 'Académico', '2024-05-28', 'Auditorio Principal', TRUE, 8, TRUE),
(12, 'Copa de Natación y Relevos Little Horizons', 'Festival acuático de estilos libre, pecho y espalda', 'Deportivo', '2024-09-22', 'Complejo Acuático', TRUE, 9, TRUE),
(13, 'Noche de las Estrellas y Astrocamping', 'Observación astronómica guiada con telescopios y campamento', 'Científico', '2024-08-25', 'Campo Abierto', FALSE, 3, TRUE),
(14, 'Muestra Folclórica y Danzas de Colombia', 'Exhibición de danzas de las cinco regiones del país', 'Cultural', '2024-07-19', 'Polideportivo Central', FALSE, 20, TRUE),
(15, 'Gala de Premiación a la Excelencia Académica', 'Entrega de menciones de honor, diplomas y reconocimientos', 'Social', '2024-11-28', 'Auditorio Principal', FALSE, 1, TRUE);

-- 17. Participantes en Eventos
INSERT INTO participantes_evento (id_participacion, id_evento, id_estudiante, rol_participacion, posicion_obtenida, observaciones) VALUES
(1, 1, 1, 'Competidor', '2do Lugar', 'Excelente desempeño y espíritu deportivo'),
(2, 2, 2, 'Competidor', '3er Lugar', 'Excelente desempeño y espíritu deportivo'),
(3, 3, 3, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(4, 4, 4, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(5, 5, 5, 'Competidor', '1er Lugar', 'Excelente desempeño y espíritu deportivo'),
(6, 6, 6, 'Competidor', '2do Lugar', 'Excelente desempeño y espíritu deportivo'),
(7, 7, 7, 'Competidor', '3er Lugar', 'Excelente desempeño y espíritu deportivo'),
(8, 8, 8, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(9, 9, 9, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(10, 10, 10, 'Competidor', '1er Lugar', 'Excelente desempeño y espíritu deportivo'),
(11, 11, 11, 'Competidor', '2do Lugar', 'Excelente desempeño y espíritu deportivo'),
(12, 12, 12, 'Competidor', '3er Lugar', 'Excelente desempeño y espíritu deportivo'),
(13, 13, 13, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(14, 14, 14, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(15, 15, 15, 'Competidor', '1er Lugar', 'Excelente desempeño y espíritu deportivo'),
(16, 1, 16, 'Competidor', '2do Lugar', 'Excelente desempeño y espíritu deportivo'),
(17, 2, 17, 'Competidor', '3er Lugar', 'Excelente desempeño y espíritu deportivo'),
(18, 3, 18, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(19, 4, 19, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(20, 5, 20, 'Competidor', '1er Lugar', 'Excelente desempeño y espíritu deportivo'),
(21, 6, 21, 'Competidor', '2do Lugar', 'Excelente desempeño y espíritu deportivo'),
(22, 7, 22, 'Competidor', '3er Lugar', 'Excelente desempeño y espíritu deportivo'),
(23, 8, 23, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(24, 9, 24, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(25, 10, 25, 'Competidor', '1er Lugar', 'Excelente desempeño y espíritu deportivo'),
(26, 11, 26, 'Competidor', '2do Lugar', 'Excelente desempeño y espíritu deportivo'),
(27, 12, 27, 'Competidor', '3er Lugar', 'Excelente desempeño y espíritu deportivo'),
(28, 13, 28, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(29, 14, 29, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(30, 15, 30, 'Competidor', '1er Lugar', 'Excelente desempeño y espíritu deportivo'),
(31, 1, 31, 'Competidor', '2do Lugar', 'Excelente desempeño y espíritu deportivo'),
(32, 2, 32, 'Competidor', '3er Lugar', 'Excelente desempeño y espíritu deportivo'),
(33, 3, 33, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(34, 4, 34, 'Competidor', 'Participante', 'Excelente desempeño y espíritu deportivo'),
(35, 5, 35, 'Competidor', '1er Lugar', 'Excelente desempeño y espíritu deportivo');

-- 18. Tabla de Pagos (35 transacciones)
INSERT INTO pagos (id_pago, id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) VALUES
(1, 1, 1, 1, 650000.00, '2024-02-02', 'Efectivo', 'REC-2024-00001', 2024, 'I', 'Pago oportuno registrado en caja'),
(2, 2, 2, 2, 420000.00, '2024-02-03', 'Tarjeta Débito', 'REC-2024-00002', 2024, 'I', 'Pago oportuno registrado en caja'),
(3, 3, 3, 3, 280000.00, '2024-02-04', 'Tarjeta Crédito', 'REC-2024-00003', 2024, 'I', 'Pago oportuno registrado en caja'),
(4, 4, 4, 4, 190000.00, '2024-02-05', 'Transferencia', 'REC-2024-00004', 2024, 'I', 'Pago oportuno registrado en caja'),
(5, 5, 5, 5, 160000.00, '2024-02-06', 'Efectivo', 'REC-2024-00005', 2024, 'I', 'Pago oportuno registrado en caja'),
(6, 6, 6, 6, 75000.00, '2024-02-07', 'Tarjeta Débito', 'REC-2024-00006', 2024, 'I', 'Pago oportuno registrado en caja'),
(7, 7, 7, 7, 20000.00, '2024-02-08', 'Tarjeta Crédito', 'REC-2024-00007', 2024, 'I', 'Pago oportuno registrado en caja'),
(8, 8, 8, 1, 650000.00, '2024-02-09', 'Transferencia', 'REC-2024-00008', 2024, 'I', 'Pago oportuno registrado en caja'),
(9, 9, 9, 2, 420000.00, '2024-02-10', 'Efectivo', 'REC-2024-00009', 2024, 'I', 'Pago oportuno registrado en caja'),
(10, 10, 10, 3, 280000.00, '2024-02-11', 'Tarjeta Débito', 'REC-2024-00010', 2024, 'I', 'Pago oportuno registrado en caja'),
(11, 11, 11, 4, 190000.00, '2024-02-12', 'Tarjeta Crédito', 'REC-2024-00011', 2024, 'I', 'Pago oportuno registrado en caja'),
(12, 12, 12, 5, 160000.00, '2024-02-13', 'Transferencia', 'REC-2024-00012', 2024, 'I', 'Pago oportuno registrado en caja'),
(13, 13, 13, 6, 75000.00, '2024-02-14', 'Efectivo', 'REC-2024-00013', 2024, 'I', 'Pago oportuno registrado en caja'),
(14, 14, 14, 7, 20000.00, '2024-02-15', 'Tarjeta Débito', 'REC-2024-00014', 2024, 'I', 'Pago oportuno registrado en caja'),
(15, 15, 15, 1, 650000.00, '2024-02-16', 'Tarjeta Crédito', 'REC-2024-00015', 2024, 'I', 'Pago oportuno registrado en caja'),
(16, 16, 16, 2, 420000.00, '2024-02-17', 'Transferencia', 'REC-2024-00016', 2024, 'I', 'Pago oportuno registrado en caja'),
(17, 17, 17, 3, 280000.00, '2024-02-18', 'Efectivo', 'REC-2024-00017', 2024, 'I', 'Pago oportuno registrado en caja'),
(18, 18, 18, 4, 190000.00, '2024-02-19', 'Tarjeta Débito', 'REC-2024-00018', 2024, 'I', 'Pago oportuno registrado en caja'),
(19, 19, 19, 5, 160000.00, '2024-02-20', 'Tarjeta Crédito', 'REC-2024-00019', 2024, 'I', 'Pago oportuno registrado en caja'),
(20, 20, 20, 6, 75000.00, '2024-02-21', 'Transferencia', 'REC-2024-00020', 2024, 'I', 'Pago oportuno registrado en caja'),
(21, 21, 21, 7, 20000.00, '2024-02-22', 'Efectivo', 'REC-2024-00021', 2024, 'I', 'Pago oportuno registrado en caja'),
(22, 22, 22, 1, 650000.00, '2024-02-23', 'Tarjeta Débito', 'REC-2024-00022', 2024, 'I', 'Pago oportuno registrado en caja'),
(23, 23, 23, 2, 420000.00, '2024-02-24', 'Tarjeta Crédito', 'REC-2024-00023', 2024, 'I', 'Pago oportuno registrado en caja'),
(24, 24, 24, 3, 280000.00, '2024-02-25', 'Transferencia', 'REC-2024-00024', 2024, 'I', 'Pago oportuno registrado en caja'),
(25, 25, 25, 4, 190000.00, '2024-02-01', 'Efectivo', 'REC-2024-00025', 2024, 'I', 'Pago oportuno registrado en caja'),
(26, 26, 26, 5, 160000.00, '2024-02-02', 'Tarjeta Débito', 'REC-2024-00026', 2024, 'I', 'Pago oportuno registrado en caja'),
(27, 27, 27, 6, 75000.00, '2024-02-03', 'Tarjeta Crédito', 'REC-2024-00027', 2024, 'I', 'Pago oportuno registrado en caja'),
(28, 28, 28, 7, 20000.00, '2024-02-04', 'Transferencia', 'REC-2024-00028', 2024, 'I', 'Pago oportuno registrado en caja'),
(29, 29, 29, 1, 650000.00, '2024-02-05', 'Efectivo', 'REC-2024-00029', 2024, 'I', 'Pago oportuno registrado en caja'),
(30, 30, 30, 2, 420000.00, '2024-02-06', 'Tarjeta Débito', 'REC-2024-00030', 2024, 'I', 'Pago oportuno registrado en caja'),
(31, 31, 31, 3, 280000.00, '2024-02-07', 'Tarjeta Crédito', 'REC-2024-00031', 2024, 'I', 'Pago oportuno registrado en caja'),
(32, 32, 32, 4, 190000.00, '2024-02-08', 'Transferencia', 'REC-2024-00032', 2024, 'I', 'Pago oportuno registrado en caja'),
(33, 33, 33, 5, 160000.00, '2024-02-09', 'Efectivo', 'REC-2024-00033', 2024, 'I', 'Pago oportuno registrado en caja'),
(34, 34, 34, 6, 75000.00, '2024-02-10', 'Tarjeta Débito', 'REC-2024-00034', 2024, 'I', 'Pago oportuno registrado en caja'),
(35, 35, 35, 7, 20000.00, '2024-02-11', 'Tarjeta Crédito', 'REC-2024-00035', 2024, 'I', 'Pago oportuno registrado en caja');

-- 19. Tabla de Facturas Electrónicas (30 facturas generadas)
INSERT INTO facturas (id_factura, numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_pdf, ruta_archivo) VALUES
(1, 'FE-LH-2024-0001', 1, '2024-02-15', '80377399098813a898becb678e39c0c3ab250ac5390e61b17c9e375e7c510539', 'factura_FE-LH-2024-0001.pdf', 'invoices/factura_FE-LH-2024-0001.pdf'),
(2, 'FE-LH-2024-0002', 2, '2024-02-15', '6c7400564d9381a19a029aef220f8f0432b1f098e398e7f77dc8c942d72ac5b5', 'factura_FE-LH-2024-0002.pdf', 'invoices/factura_FE-LH-2024-0002.pdf'),
(3, 'FE-LH-2024-0003', 3, '2024-02-15', 'cff1f7c909e7128bc8deda7da3210c2703efeb2fcbd9e772f803c94bc4ef71fe', 'factura_FE-LH-2024-0003.pdf', 'invoices/factura_FE-LH-2024-0003.pdf'),
(4, 'FE-LH-2024-0004', 4, '2024-02-15', '2a0934e9064a60bbe1225e3fe1b3f87bf77c7315e120b1d8095db54700701e30', 'factura_FE-LH-2024-0004.pdf', 'invoices/factura_FE-LH-2024-0004.pdf'),
(5, 'FE-LH-2024-0005', 5, '2024-02-15', 'df08d0015a6f4b4c56c363c50701862610ce3aaabbc9e1a8ec60b0e7580bc18d', 'factura_FE-LH-2024-0005.pdf', 'invoices/factura_FE-LH-2024-0005.pdf'),
(6, 'FE-LH-2024-0006', 6, '2024-02-15', 'f26c17a924ddff7d59345a80c2e0e878531e99bda0301e5a4aa705aeef481776', 'factura_FE-LH-2024-0006.pdf', 'invoices/factura_FE-LH-2024-0006.pdf'),
(7, 'FE-LH-2024-0007', 7, '2024-02-15', 'ddc6b6010406e02bc040c2292bf1fdabf6e050db5ff8fab5593b45005fe4a5e0', 'factura_FE-LH-2024-0007.pdf', 'invoices/factura_FE-LH-2024-0007.pdf'),
(8, 'FE-LH-2024-0008', 8, '2024-02-15', 'c5b65423bbaaa98ba4e289372ff0464a40003c1629e7dab4c4bbc3691536403a', 'factura_FE-LH-2024-0008.pdf', 'invoices/factura_FE-LH-2024-0008.pdf'),
(9, 'FE-LH-2024-0009', 9, '2024-02-15', '71a0ee7b707e18282a1fe8de9e896889748489dfc55aa8a4ce43fefadf84d07b', 'factura_FE-LH-2024-0009.pdf', 'invoices/factura_FE-LH-2024-0009.pdf'),
(10, 'FE-LH-2024-0010', 10, '2024-02-15', 'db05f09138a53c783bd7892e231c7b0d29d6bd1c6f7a580f497f27c40e29cfc3', 'factura_FE-LH-2024-0010.pdf', 'invoices/factura_FE-LH-2024-0010.pdf'),
(11, 'FE-LH-2024-0011', 11, '2024-02-15', '0107ce8adb5e853a718075b0f7376a5eb4f8fff784d92c92185e560b921dd979', 'factura_FE-LH-2024-0011.pdf', 'invoices/factura_FE-LH-2024-0011.pdf'),
(12, 'FE-LH-2024-0012', 12, '2024-02-15', '356d0c1c8366715c4b2347070bde7a4b1e62359c7056d3ed79bba686fd131470', 'factura_FE-LH-2024-0012.pdf', 'invoices/factura_FE-LH-2024-0012.pdf'),
(13, 'FE-LH-2024-0013', 13, '2024-02-15', '6a10286efde5b39466c1aeb7cfd329ece32a59f52cf6cac83f6ac14a7c78b60e', 'factura_FE-LH-2024-0013.pdf', 'invoices/factura_FE-LH-2024-0013.pdf'),
(14, 'FE-LH-2024-0014', 14, '2024-02-15', '73d59d012d223e0e34ed5168537d4081dea584079db38dc9af284ae0c5f78d08', 'factura_FE-LH-2024-0014.pdf', 'invoices/factura_FE-LH-2024-0014.pdf'),
(15, 'FE-LH-2024-0015', 15, '2024-02-15', '0e5163b05117977544a864f7e30dad68f6bc806a5555c1a9eeb767c2143af27d', 'factura_FE-LH-2024-0015.pdf', 'invoices/factura_FE-LH-2024-0015.pdf'),
(16, 'FE-LH-2024-0016', 16, '2024-02-15', '88031b94edcc556042f104716b831b81bcae540f1d044690e7bd379aa8d8498e', 'factura_FE-LH-2024-0016.pdf', 'invoices/factura_FE-LH-2024-0016.pdf'),
(17, 'FE-LH-2024-0017', 17, '2024-02-15', '2a2baf88a5c0483bb9757599e18305bd036a1e51854d6e15a7cf74c6535cae47', 'factura_FE-LH-2024-0017.pdf', 'invoices/factura_FE-LH-2024-0017.pdf'),
(18, 'FE-LH-2024-0018', 18, '2024-02-15', '2d53c58d05bcf826c3b9f8a88bf676926e50cc8fdbe071d9f49a58119c02243c', 'factura_FE-LH-2024-0018.pdf', 'invoices/factura_FE-LH-2024-0018.pdf'),
(19, 'FE-LH-2024-0019', 19, '2024-02-15', '30b4fcb271f8d149e7eac119b1285efd69d1302a77478b502e6a205c588f7228', 'factura_FE-LH-2024-0019.pdf', 'invoices/factura_FE-LH-2024-0019.pdf'),
(20, 'FE-LH-2024-0020', 20, '2024-02-15', 'ca279a542cd8bdcae30a72bc606db26a3fa7b3cd8529fc90595de7d48f03b729', 'factura_FE-LH-2024-0020.pdf', 'invoices/factura_FE-LH-2024-0020.pdf'),
(21, 'FE-LH-2024-0021', 21, '2024-02-15', '5d53a181f058fbb65b3594c2c6a0d48ee24915961fcca5414e1dc4d81055a055', 'factura_FE-LH-2024-0021.pdf', 'invoices/factura_FE-LH-2024-0021.pdf'),
(22, 'FE-LH-2024-0022', 22, '2024-02-15', 'a31c6048f7ce9d1b40eb387d3f575e7a34838aca93fc56fc3eaff01b457dee06', 'factura_FE-LH-2024-0022.pdf', 'invoices/factura_FE-LH-2024-0022.pdf'),
(23, 'FE-LH-2024-0023', 23, '2024-02-15', '4937dc3429783517ba19fb4f146d3d7ce1544231bf8279591c6b3660484faa6f', 'factura_FE-LH-2024-0023.pdf', 'invoices/factura_FE-LH-2024-0023.pdf'),
(24, 'FE-LH-2024-0024', 24, '2024-02-15', 'f41de2a7acc16797bf0b3a7c640724f3e98a512765fc98ce567291d681da2297', 'factura_FE-LH-2024-0024.pdf', 'invoices/factura_FE-LH-2024-0024.pdf'),
(25, 'FE-LH-2024-0025', 25, '2024-02-15', 'a55d9b1e841776cb0ea04850317ac2bd11dd1ba3f88d12658ef39dcc7debd134', 'factura_FE-LH-2024-0025.pdf', 'invoices/factura_FE-LH-2024-0025.pdf'),
(26, 'FE-LH-2024-0026', 26, '2024-02-15', '2bd01e93d7b07ea5d06972732f67b96b43c3da20a53194578e26b9486512f6c0', 'factura_FE-LH-2024-0026.pdf', 'invoices/factura_FE-LH-2024-0026.pdf'),
(27, 'FE-LH-2024-0027', 27, '2024-02-15', '0b26fe0567df2e1cff9440fefacd1865aec84d3db5a16294a31a484445dbbddb', 'factura_FE-LH-2024-0027.pdf', 'invoices/factura_FE-LH-2024-0027.pdf'),
(28, 'FE-LH-2024-0028', 28, '2024-02-15', '527826a1cd9196fa6402d0d6abf97c5c117a85f6b9046af745746fe7ee9a3c76', 'factura_FE-LH-2024-0028.pdf', 'invoices/factura_FE-LH-2024-0028.pdf'),
(29, 'FE-LH-2024-0029', 29, '2024-02-15', '9a8cc48a03eaac73a6c05bae6825b99da3c248d248056300c16e14b031a606e4', 'factura_FE-LH-2024-0029.pdf', 'invoices/factura_FE-LH-2024-0029.pdf'),
(30, 'FE-LH-2024-0030', 30, '2024-02-15', '95d61aaf930288001816b8700f7440e8734f7025b19d1420e3d2423a5413c35a', 'factura_FE-LH-2024-0030.pdf', 'invoices/factura_FE-LH-2024-0030.pdf');

-- 20. Tabla de Recursos Prestables (35 recursos)
INSERT INTO recursos (id_recurso, codigo_recurso, nombre_recurso, id_categoria, descripcion, fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES
(1, 'LIB-LIT-001', 'Cien Años de Soledad - Edición Ilustrada', 1, 'Novela cumbre del realismo mágico de Gabriel García Márquez', '2020-03-10', 'Biblioteca - Estante A1', 'Disponible'),
(2, 'LIB-LIT-002', 'Don Quijote de la Mancha - Clásicos Escolares', 1, 'Obra cumbre de Miguel de Cervantes Saavedra con notas al pie', '2019-02-15', 'Biblioteca - Estante A2', 'Disponible'),
(3, 'LIB-MAT-003', 'Álgebra de Baldor con CD Interactivo', 1, 'Texto clásico de álgebra elemental con ejercicios resueltos', '2021-01-20', 'Biblioteca - Estante B1', 'Prestado'),
(4, 'LIB-CIE-004', 'Biología Celular y Molecular de Lodish', 1, 'Manual avanzado de biología para proyectos científicos de media', '2022-04-12', 'Biblioteca - Estante C1', 'Disponible'),
(5, 'LIB-FIS-005', 'Física Universitaria Sears Zemansky Vol. 1', 1, 'Texto fundamental de mecánica y cinemática para grado 10 y 11', '2021-08-10', 'Biblioteca - Estante C2', 'Prestado'),
(6, 'LIB-HIS-006', 'Historia de Colombia y sus Oligarquías - Caballero', 1, 'Crónica ilustrada de la historia política nacional', '2020-09-05', 'Biblioteca - Estante D1', 'Disponible'),
(7, 'LIB-ING-007', 'Oxford English Grammar in Use with Answers', 1, 'Guía de referencia gramatical y ejercicios prácticos B1-B2', '2022-01-18', 'Biblioteca - Estante E1', 'Disponible'),
(8, 'LIB-QUI-008', 'Química General de Raymond Chang 12va Ed.', 1, 'Tratado completo de química inorgánica y fisicoquímica', '2021-06-25', 'Biblioteca - Estante C3', 'Disponible'),
(9, 'LAB-MIC-001', 'Microscopio Óptico Binocular Celestron 2000x', 2, 'Microscopio de alta resolución con luz LED y lentes acromáticos', '2021-05-14', 'Laboratorio 1 - Mesa A', 'Disponible'),
(10, 'LAB-MIC-002', 'Microscopio Óptico Binocular Celestron 2000x', 2, 'Microscopio de alta resolución con luz LED y lentes acromáticos', '2021-05-14', 'Laboratorio 1 - Mesa B', 'Prestado'),
(11, 'LAB-BAL-001', 'Balanza Analítica Digital de Precisión Ohaus 0.001g', 2, 'Balanza de calibración interna para reactivos químicos', '2022-02-10', 'Laboratorio 1 - Armario 1', 'Disponible'),
(12, 'LAB-BUN-001', 'Set de 4 Mecheros Bunsen con Válvula de Seguridad', 2, 'Mecheros de gas propano con soporte universal y malla de asbesto', '2020-08-20', 'Laboratorio 1 - Armario 2', 'Disponible'),
(13, 'LAB-CEN-001', 'Centrífuga Clínica de 8 Tubos para Bioquímica', 2, 'Centrífuga eléctrica digital hasta 4000 RPM para separación de muestras', '2023-01-15', 'Laboratorio 2 - Mesón Central', 'Disponible'),
(14, 'LAB-MOD-001', 'Modelo Anatómico Humano Desarmable 85cm', 2, 'Torso anatómico de 27 piezas desmontables para estudio de órganos', '2019-10-18', 'Laboratorio 2 - Vitrina Este', 'Disponible'),
(15, 'LAB-KIT-001', 'Kit de Disección Quirúrgica de 14 Piezas en Acero', 2, 'Bisturís, pinzas, tijeras curvas y sondas acanaladas', '2022-09-01', 'Laboratorio 2 - Gaveta 3', 'Disponible'),
(16, 'DEP-BAL-001', 'Balón Oficial de Fútbol FIFA Pro Golty Invictus', 3, 'Balón termocargado de alta resistencia al agua para torneos', '2023-02-10', 'Bodega de Deportes - Casillero 1', 'Disponible'),
(17, 'DEP-BAL-002', 'Balón Oficial de Baloncesto Molten BG4500 #7', 3, 'Balón de cuero compuesto para juego en coliseo cubierto', '2023-02-10', 'Bodega de Deportes - Casillero 2', 'Disponible'),
(18, 'DEP-BAL-003', 'Balón Oficial de Voleibol Mikasa V200W', 3, 'Balón con diseño aerodinámico de 18 paneles', '2023-02-10', 'Bodega de Deportes - Casillero 3', 'Disponible'),
(19, 'DEP-CRO-001', 'Set de Cronómetros Profesionales Seiko de 100 Memorias', 3, 'Cronómetros digitales con precisión de 1/100 seg para atletismo', '2022-07-15', 'Bodega de Deportes - Gaveta A', 'Prestado'),
(20, 'DEP-COL-001', 'Colchoneta Gimnástica de Alta Densidad 2x1m', 3, 'Colchoneta protectora para salto, gimnasia y caídas', '2021-11-20', 'Bodega de Deportes - Zona Colchonetas', 'Disponible'),
(21, 'DEP-RAQ-001', 'Juego de 4 Raquetas y Malla de Tenis de Mesa Butterfly', 3, 'Paletas profesionales de tenis de mesa con estuche y 10 bolas', '2022-08-30', 'Bodega de Deportes - Casillero 4', 'Disponible'),
(22, 'TEC-TAB-001', 'Tablet Samsung Galaxy Tab A9 64GB - Aula Móvil 1', 4, 'Tablet para aplicaciones interactivas y lectura digital de primaria', '2023-01-25', 'Sala de Tecnología - Carrito 1', 'Disponible'),
(23, 'TEC-TAB-002', 'Tablet Samsung Galaxy Tab A9 64GB - Aula Móvil 2', 4, 'Tablet para aplicaciones interactivas y lectura digital de primaria', '2023-01-25', 'Sala de Tecnología - Carrito 1', 'Prestado'),
(24, 'TEC-TAB-003', 'Tablet Samsung Galaxy Tab A9 64GB - Aula Móvil 3', 4, 'Tablet para aplicaciones interactivas y lectura digital de primaria', '2023-01-25', 'Sala de Tecnología - Carrito 1', 'Disponible'),
(25, 'TEC-POR-001', 'Portátil Lenovo ThinkPad E14 Intel Core i5 16GB', 4, 'Computador portátil docente para proyecciones y evaluaciones', '2022-10-10', 'Dirección de Tecnología - Casillero L1', 'Disponible'),
(26, 'TEC-POR-002', 'Portátil Lenovo ThinkPad E14 Intel Core i5 16GB', 4, 'Computador portátil docente para proyecciones y evaluaciones', '2022-10-10', 'Dirección de Tecnología - Casillero L2', 'Prestado'),
(27, 'TEC-CAL-001', 'Calculadora Científica Gráfica Casio fx-CG50', 4, 'Calculadora con pantalla a color para cálculo diferencial y estadística', '2021-03-15', 'Biblioteca - Vitrina Tecnológica', 'Disponible'),
(28, 'TEC-CAL-002', 'Calculadora Científica Gráfica Casio fx-CG50', 4, 'Calculadora con pantalla a color para cálculo diferencial y estadística', '2021-03-15', 'Biblioteca - Vitrina Tecnológica', 'Prestado'),
(29, 'TEC-PRO-001', 'Video Proyector Epson PowerLite 3800 Lúmenes HDMI', 4, 'Proyector portátil de alta luminosidad para presentaciones en aula', '2022-05-18', 'Audiovisuales - Estante Proyectores', 'Disponible'),
(30, 'TEC-ROB-001', 'Kit de Robótica Educativa LEGO SPIKE Prime', 4, 'Kit de programación con motores, sensores de color y giroscopio', '2023-03-01', 'Taller de Robótica - Caja 1', 'Disponible'),
(31, 'MAT-GLO-001', 'Globo Terráqueo Físico y Político con Luz LED 30cm', 5, 'Esfera geográfica giratoria con relieve montañoso', '2020-04-10', 'Sala de Ciencias Sociales', 'Disponible'),
(32, 'MAT-MAP-001', 'Colección de Mapas Murales de Colombia y Continentes', 5, 'Set de 8 mapas plastificados enrollables de gran formato', '2019-06-15', 'Sala de Ciencias Sociales - Porta Mapas', 'Disponible'),
(33, 'MAT-MOL-001', 'Set de Modelos Moleculares Químicos Orgánicos 240 Pzs', 5, 'Kit de esferas y enlaces para construcción de hidrocarburos', '2021-09-20', 'Laboratorio 1 - Gaveta 4', 'Disponible'),
(34, 'MAT-GEO-001', 'Cuerpo Geométrico Tridimensional Transparente 12 Pzs', 5, 'Sólidos platónicos y pirámides para demostración de volumen y área', '2022-03-11', 'Sala de Matemáticas', 'Disponible'),
(35, 'MAT-OPT-001', 'Banco Óptico con Láser, Prismas y Lentes Cóncavos', 5, 'Dispositivo didáctico para refracción, reflexión y dispersión luminosa', '2023-04-05', 'Laboratorio de Física', 'Disponible');

-- 21. Préstamos de Recursos a Estudiantes
INSERT INTO prestamos_estudiante (id_prestamo, id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(1, 3, 5, '2024-03-01', '2024-03-15', NULL, 'Activo', 'Préstamo para preparación prueba Saber 11'),
(2, 5, 7, '2024-03-02', '2024-03-16', NULL, 'Activo', 'Guía para proyecto de física'),
(3, 23, 10, '2024-03-05', '2024-03-19', NULL, 'Activo', 'Lectura de material didáctico en biblioteca'),
(4, 28, 15, '2024-02-10', '2024-02-24', NULL, 'Vencido', 'Pendiente de devolución por estudiante'),
(5, 11, 6, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(6, 13, 7, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(7, 15, 8, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(8, 17, 9, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(9, 19, 10, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(10, 21, 11, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(11, 23, 12, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(12, 25, 13, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(13, 27, 14, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(14, 29, 15, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(15, 31, 16, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(16, 33, 17, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(17, 35, 18, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(18, 2, 19, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(19, 4, 20, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado'),
(20, 6, 21, '2024-02-01', '2024-02-08', '2024-02-08', 'Devuelto', 'Recurso entregado en perfecto estado');

-- 22. Préstamos de Recursos a Profesores
INSERT INTO prestamos_profesor (id_prestamo, id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_esperada, fecha_devolucion_real, estado_prestamo, observaciones) VALUES
(1, 10, 4, '2024-03-04', '2024-03-18', NULL, 'Activo', 'Uso en prácticas de laboratorio de grado 10'),
(2, 19, 9, '2024-03-01', '2024-03-15', NULL, 'Activo', 'Uso en entrenamientos de pista'),
(3, 26, 11, '2024-03-02', '2024-03-16', NULL, 'Activo', 'Preparación de material pedagógico multimedia'),
(4, 13, 5, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(5, 16, 6, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(6, 19, 7, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(7, 22, 8, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(8, 25, 9, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(9, 28, 10, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(10, 31, 11, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(11, 34, 12, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(12, 2, 13, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(13, 5, 14, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(14, 8, 15, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades'),
(15, 11, 16, '2024-02-01', '2024-02-15', '2024-02-14', 'Devuelto', 'Devolución completa sin novedades');

-- ========================================================
-- VERIFICACIÓN FINAL DE REGISTROS POR TABLA
-- ========================================================

SELECT 'grados' AS tabla, COUNT(*) AS total_registros FROM grados UNION ALL
SELECT 'acudientes', COUNT(*) FROM acudientes UNION ALL
SELECT 'estudiantes', COUNT(*) FROM estudiantes UNION ALL
SELECT 'profesores', COUNT(*) FROM profesores UNION ALL
SELECT 'materias', COUNT(*) FROM materias UNION ALL
SELECT 'registros_academicos', COUNT(*) FROM registros_academicos UNION ALL
SELECT 'clubes', COUNT(*) FROM clubes UNION ALL
SELECT 'pagos', COUNT(*) FROM pagos UNION ALL
SELECT 'facturas', COUNT(*) FROM facturas UNION ALL
SELECT 'recursos', COUNT(*) FROM recursos UNION ALL
SELECT 'prestamos_estudiante', COUNT(*) FROM prestamos_estudiante UNION ALL
SELECT 'prestamos_profesor', COUNT(*) FROM prestamos_profesor;