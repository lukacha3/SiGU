DROP TABLE IF EXISTS sesion;
DROP TABLE IF EXISTS gestorSistema;
DROP TABLE IF EXISTS secretariaAcademica;
DROP TABLE IF EXISTS Docente_Carrera;
DROP TABLE IF EXISTS teacher;
DROP TABLE IF EXISTS Docente_Materia;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS SolicitudAula;
DROP TABLE IF EXISTS Aula;
DROP TABLE IF EXISTS Aula_Asignacion;
DROP TABLE IF EXISTS Inscripcion_Parcial;
DROP TABLE IF EXISTS Nota;
DROP TABLE IF EXISTS Anuncio;
DROP TABLE IF EXISTS Materia_Periodo;
DROP TABLE IF EXISTS Correlatividad;
DROP TABLE IF EXISTS Materia;
DROP TABLE IF EXISTS Plan_Estudio;
DROP TABLE IF EXISTS Carrera;
DROP TABLE IF EXISTS inscripciones_examen;
DROP TABLE IF EXISTS mesas_examen;
DROP TABLE IF EXISTS Estado_Academico;
 
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(50),
    nombre_usuario VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nivel_acceso VARCHAR(50) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    foto_perfil VARCHAR(255) DEFAULT '/img/default-avatar.png'
);
 
CREATE TABLE student (
    usuario_id INT PRIMARY KEY,
    legajo VARCHAR(20) NOT NULL UNIQUE,
    tipo_estudiante VARCHAR(50) NOT NULL,
    plan_estudio_id INT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (plan_estudio_id) REFERENCES Plan_Estudio(id) ON DELETE RESTRICT
);
 
CREATE TABLE teacher (
    usuario_id INT PRIMARY KEY,
    legajo_docente VARCHAR(50) NOT NULL UNIQUE,
    cuil VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    titulo VARCHAR(100),
    especialidad VARCHAR(150),
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE
);
 
CREATE TABLE secretariaAcademica (
    usuario_id INT PRIMARY KEY,
    oficina VARCHAR(50),
    interno VARCHAR(20),
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE
);
 
CREATE TABLE gestorSistema (
    usuario_id INT PRIMARY KEY,
    area_responsabilidad VARCHAR(100),
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE
);
 
CREATE TABLE sesion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    fecha_inicio DATETIME NOT NULL,
    fecha_expiracion DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE
);
 
CREATE TABLE Carrera (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    duracion_anios INT NOT NULL,
    titulo_otorgado VARCHAR(150) NOT NULL
);
 
CREATE TABLE Plan_Estudio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    carrera_id INT NOT NULL,
    anio_resolucion INT NOT NULL,
    estado VARCHAR(50) DEFAULT 'VIGENTE',
    FOREIGN KEY (carrera_id) REFERENCES Carrera(id) ON DELETE CASCADE
);
 
CREATE TABLE Materia (
    codigo INT PRIMARY KEY, 
    plan_estudio_id INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    anio_cursada INT NOT NULL,
    carga_horaria_total INT,
    FOREIGN KEY (plan_estudio_id) REFERENCES Plan_Estudio(id) ON DELETE CASCADE
);
 
CREATE TABLE Docente_Materia (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    teacher_id INT NOT NULL,
    materia_id INT NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES teacher(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (materia_id) REFERENCES Materia(codigo) ON DELETE CASCADE,
    UNIQUE (teacher_id, materia_id)
);
 
CREATE TABLE Correlatividad (
    materia_codigo INT NOT NULL,
    materia_correlativa_codigo INT NOT NULL,
    condicion VARCHAR(50) NOT NULL DEFAULT 'APROBADA',
    tipo_requisito VARCHAR(50) NOT NULL DEFAULT 'CURSAR',
    PRIMARY KEY (materia_codigo, materia_correlativa_codigo, tipo_requisito),
    FOREIGN KEY (materia_codigo) REFERENCES Materia(codigo) ON DELETE CASCADE,
    FOREIGN KEY (materia_correlativa_codigo) REFERENCES Materia(codigo) ON DELETE CASCADE,
    CHECK (materia_codigo != materia_correlativa_codigo)
);
 
CREATE TABLE Materia_Periodo (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    materia_codigo INT NOT NULL,         
    anio_academico INT NOT NULL,
    tipo_cuatrimestre VARCHAR(50) NOT NULL,
    FOREIGN KEY (materia_codigo) REFERENCES Materia(codigo) ON DELETE CASCADE
);
 
CREATE TABLE Docente_Carrera (
    teacher_id INT NOT NULL,
    carrera_id INT NOT NULL,
    PRIMARY KEY (teacher_id, carrera_id),
    FOREIGN KEY (teacher_id)  REFERENCES teacher(usuario_id)  ON DELETE CASCADE,
    FOREIGN KEY (carrera_id)  REFERENCES Carrera(id) ON DELETE CASCADE
);
 
CREATE TABLE Aula (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_nombre VARCHAR(50) NOT NULL UNIQUE,
    capacidad INT NOT NULL,
    estado_aula VARCHAR(50) DEFAULT 'DISPONIBLE'
);
 
CREATE TABLE SolicitudAula (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    aula_id INT NOT NULL,
    materia_periodo_id INT NOT NULL,
    dia_semana VARCHAR(50) NOT NULL,
    horario_inicio TIME NOT NULL,
    horario_fin TIME NOT NULL,
    estado_solicitud VARCHAR(50) DEFAULT 'PENDIENTE',
    FOREIGN KEY (aula_id) REFERENCES Aula(id) ON DELETE CASCADE,
    FOREIGN KEY (materia_periodo_id) REFERENCES Materia_Periodo(id) ON DELETE CASCADE
);
 
CREATE TABLE Anuncio (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    materia_periodo_id INT NOT NULL,
    teacher_id INT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    contenido TEXT NOT NULL,
    fecha_examen DATE NULL,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (materia_periodo_id) REFERENCES Materia_Periodo(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(usuario_id) ON DELETE CASCADE
);
 
CREATE TABLE Nota (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    materia_periodo_id INT NOT NULL,
    student_id INT NOT NULL,
    teacher_id INT NOT NULL,
    valor DECIMAL(5,2) NOT NULL,
    fecha_carga DATETIME DEFAULT CURRENT_TIMESTAMP,
    instancia VARCHAR(50) NOT NULL DEFAULT 'CURSADA',
    FOREIGN KEY (materia_periodo_id) REFERENCES Materia_Periodo(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(usuario_id) ON DELETE CASCADE
);
 
CREATE TABLE Aula_Asignacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    materia_periodo_id INT NOT NULL,
    teacher_id INT NOT NULL,
    aula VARCHAR(50) NOT NULL,
    fecha_asignacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (materia_periodo_id) REFERENCES Materia_Periodo(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(usuario_id) ON DELETE CASCADE
);
 
CREATE TABLE mesas_examen (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    materia_codigo INT NOT NULL,
    fecha DATE NOT NULL,
    FOREIGN KEY (materia_codigo) REFERENCES Materia(codigo) ON DELETE CASCADE
);
 
CREATE TABLE inscripciones_examen (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INT NOT NULL,
    mesa_id INT NOT NULL,
    fecha_inscripcion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES student(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (mesa_id) REFERENCES mesas_examen(id) ON DELETE CASCADE,
    UNIQUE (usuario_id, mesa_id)
);
 
CREATE TABLE Estado_Academico (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INT NOT NULL,
    materia_codigo INT NOT NULL,
    estado VARCHAR(50) NOT NULL,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES student(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (materia_codigo) REFERENCES Materia(codigo) ON DELETE CASCADE,
    UNIQUE (usuario_id, materia_codigo)
);
 
CREATE TABLE Inscripcion_Parcial (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INT NOT NULL,
    anuncio_id INT NOT NULL,
    fecha_inscripcion DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES student(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (anuncio_id) REFERENCES Anuncio(id) ON DELETE CASCADE,
    UNIQUE (usuario_id, anuncio_id)
);

INSERT OR IGNORE INTO users (dni, nombre, apellido, nombre_usuario, password, nivel_acceso) 
VALUES (
    '00000000', 
    'Administrador', 
    'Sistema', 
    'admin', 
    '$2a$10$tzGyrad6vMs9/BPymyxxv.JdZ8KEaDipWPuj1UqE1U6KuzRbDciy6',
    'ADMIN'
);
