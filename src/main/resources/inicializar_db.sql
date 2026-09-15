USE sigu;

INSERT INTO Carrera (id, nombre, duracion_anios, titulo_otorgado) VALUES
(1, 'Analista en Computación', 3, 'Analista en Computación'),
(2, 'Licenciatura en Ciencias de la Computación', 5, 'Licenciado/a en Ciencias de la Computación');

INSERT INTO Plan_Estudio (id, carrera_id, anio_resolucion, estado) VALUES
(1, 1, 2024, 'VIGENTE'),
(2, 2, 2024, 'VIGENTE');

-- Compu obligatorias
INSERT INTO Materia (codigo, plan_estudio_id, nombre, anio_cursada, carga_horaria_total) VALUES
(3410, 1, 'Introducción a la Computación y Programación I', 1, 112),
(3376, 1, 'Introducción a la Matemática', 1, 112),
(3377, 1, 'Lógica y Resolución de Problemas', 1, 112),
(3411, 1, 'Introducción a la Computación y Programación II', 1, 112),
(3379, 1, 'Matemática Discreta', 1, 140),
(3412, 1, 'Estructura de Datos y Algoritmos', 2, 112),
(3381, 1, 'Organización de Computadoras', 2, 112),
(3382, 1, 'Computación y Sociedad', 2, 56),
(3402, 1, 'Inglés I', 2, 56),
(3383, 1, 'Análisis y Diseño de Algoritmos I', 2, 112),
(3384, 1, 'Bases de Datos', 2, 112),
(3385, 1, 'Ingeniería de Software I', 2, 112),
(3403, 1, 'Inglés II', 2, 56),
(3386, 1, 'Paradigmas y Lenguajes de Programación', 3, 112),
(3387, 1, 'Ingeniería de Software II', 3, 112),
(3388, 1, 'Sistemas Operativos y Redes', 3, 112),
(3389, 1, 'Seminario de Redacción Informativa', 3, 56),
(3390, 1, 'Sistemas Distribuidos', 3, 112);

-- Compu optativas
INSERT INTO Materia (codigo, plan_estudio_id, nombre, anio_cursada, carga_horaria_total) VALUES
(3414, 1, 'Seminario de Ciencias de la Computación', 3, 112),
(3124, 1, 'Diseño de Software Orientado a Objetos', 3, 112),
(3305, 1, 'Geometría y Álgebra Lineal', 3, 112),
(3335, 1, 'Bases de Datos II', 3, 112),
(3347, 1, 'Testing de software', 3, 112),
(3348, 1, 'Introducción a la transformación de modelos de software usando QVT', 3, 112),
(3327, 1, 'Geometría', 3, 112),
(1947, 1, 'Álgebra', 3, 112),
(1984, 1, 'Cálculo II', 3, 112),
(1998, 1, 'Proyecto', 3, 112),
(3395, 1, 'Análisis y Diseño de Algoritmos II', 3, 112),
(3391, 1, 'Lógica para Ciencias de la Computación', 3, 112);
-- Compu obligatorias correlatividades
INSERT INTO Correlatividad (materia_codigo, materia_correlativa_codigo, condicion, tipo_requisito) VALUES
(3411, 3410, 'REGULAR', 'CURSAR'),
(3411, 3410, 'APROBADA', 'RENDIR'),
(3379, 3376, 'REGULAR', 'CURSAR'),
(3379, 3376, 'APROBADA', 'RENDIR'),
(3412, 3411, 'REGULAR', 'CURSAR'),
(3412, 3379, 'REGULAR', 'CURSAR'),
(3412, 3377, 'REGULAR', 'CURSAR'),
(3412, 3410, 'APROBADA', 'CURSAR'),
(3412, 3410, 'APROBADA', 'RENDIR'),
(3412, 3411, 'APROBADA', 'RENDIR'),
(3412, 3379, 'APROBADA', 'RENDIR'),
(3381, 3410, 'REGULAR', 'CURSAR'),
(3381, 3376, 'REGULAR', 'CURSAR'),
(3381, 3410, 'APROBADA', 'RENDIR'),
(3381, 3376, 'APROBADA', 'RENDIR'),
(3381, 3377, 'APROBADA', 'RENDIR'),
(3382, 3410, 'REGULAR', 'CURSAR'),
(3382, 3410, 'APROBADA', 'RENDIR'),
(3383, 3412, 'REGULAR', 'CURSAR'),
(3383, 3411, 'APROBADA', 'CURSAR'),
(3383, 3411, 'APROBADA', 'RENDIR'),
(3383, 3412, 'APROBADA', 'RENDIR'),
(3384, 3411, 'REGULAR', 'CURSAR'),
(3384, 3410, 'APROBADA', 'CURSAR'),
(3384, 3411, 'APROBADA', 'RENDIR'),
(3384, 3410, 'APROBADA', 'RENDIR'),
(3385, 3411, 'REGULAR', 'CURSAR'),
(3385, 3410, 'APROBADA', 'CURSAR'),
(3385, 3410, 'APROBADA', 'RENDIR'),
(3385, 3411, 'APROBADA', 'RENDIR'),
(3403, 3402, 'REGULAR', 'CURSAR'),
(3403, 3402, 'APROBADA', 'RENDIR'),
(3386, 3381, 'REGULAR', 'CURSAR'),
(3386, 3411, 'REGULAR', 'CURSAR'),
(3386, 3410, 'APROBADA', 'CURSAR'),
(3386, 3410, 'APROBADA', 'RENDIR'),
(3386, 3381, 'APROBADA', 'RENDIR'),
(3386, 3411, 'APROBADA', 'RENDIR'),
(3387, 3385, 'REGULAR', 'CURSAR'),
(3387, 3411, 'APROBADA', 'CURSAR'),
(3387, 3382, 'APROBADA', 'CURSAR'),
(3387, 3412, 'APROBADA', 'RENDIR'),
(3387, 3382, 'APROBADA', 'RENDIR'),
(3387, 3385, 'APROBADA', 'RENDIR'),
(3388, 3381, 'REGULAR', 'CURSAR'),
(3388, 3411, 'REGULAR', 'CURSAR'),
(3388, 3410, 'APROBADA', 'CURSAR'),
(3388, 3411, 'APROBADA', 'RENDIR'),
(3388, 3381, 'APROBADA', 'RENDIR'),
(3388, 3410, 'APROBADA', 'RENDIR'),
(3389, 3382, 'REGULAR', 'CURSAR'),
(3389, 3382, 'APROBADA', 'RENDIR'),
(3390, 3388, 'REGULAR', 'CURSAR'),
(3390, 3412, 'REGULAR', 'CURSAR'),
(3390, 3384, 'APROBADA', 'CURSAR'),
(3390, 3384, 'APROBADA', 'RENDIR'),
(3390, 3412, 'APROBADA', 'RENDIR'),
(3390, 3388, 'APROBADA', 'RENDIR'),
-- Compu optativas correlatividades
(3414, 3385, 'REGULAR', 'CURSAR'),
(3414, 3383, 'REGULAR', 'CURSAR'),
(3414, 3385, 'APROBADA', 'RENDIR'),
(3414, 3383, 'APROBADA', 'RENDIR'),
(3124, 3387, 'REGULAR', 'CURSAR'),
(3124, 3387, 'APROBADA', 'RENDIR'),
(3305, 3379, 'APROBADA', 'CURSAR'),
(3335, 3384, 'REGULAR', 'CURSAR'),
(3335, 3384, 'APROBADA', 'RENDIR'),
(3347, 3387, 'REGULAR', 'CURSAR'),
(3347, 3387, 'APROBADA', 'RENDIR'),
(3348, 3387, 'REGULAR', 'CURSAR'),
(3348, 3384, 'APROBADA', 'CURSAR'),
(3348, 3387, 'APROBADA', 'RENDIR'),
(3327, 3379, 'REGULAR', 'CURSAR'),
(3327, 3379, 'APROBADA', 'RENDIR'),
(1947, 3379, 'REGULAR', 'CURSAR'),
(1947, 3379, 'APROBADA', 'RENDIR'),
(1984, 3376, 'APROBADA', 'CURSAR'),
(1998, 3385, 'REGULAR', 'CURSAR'),
(1998, 3384, 'REGULAR', 'CURSAR'),
(1998, 3385, 'APROBADA', 'RENDIR'),
(1998, 3384, 'APROBADA', 'RENDIR'),
(3395, 3390, 'REGULAR', 'CURSAR'),
(3395, 3383, 'APROBADA', 'CURSAR'),
(3395, 3390, 'APROBADA', 'RENDIR'),
(3395, 3383, 'APROBADA', 'RENDIR'),
(3391, 3383, 'REGULAR', 'CURSAR'),
(3391, 3412, 'APROBADA', 'CURSAR'),
(3391, 3383, 'APROBADA', 'RENDIR'),
(3391, 3412, 'APROBADA', 'RENDIR');



-- Códigos desplazados +10000 respecto al documento oficial para evitar
-- choque de primary key con los códigos ya usados por Analista en Computación.
INSERT INTO Materia (codigo, plan_estudio_id, nombre, anio_cursada, carga_horaria_total) VALUES
(13375, 2, 'Introducción a los Algoritmos', 1, 112),
(13376, 2, 'Introducción a la Matemática', 1, 112),
(13377, 2, 'Lógica y Resolución de Problemas', 1, 112),
(13378, 2, 'Algoritmos y Estructuras de Datos I', 1, 112),
(13379, 2, 'Matemática Discreta', 1, 140),
(13380, 2, 'Algoritmos y Estructuras de Datos II', 2, 112),
(13381, 2, 'Organización de Computadoras', 2, 112),
(13382, 2, 'Computación y Sociedad', 2, 56),
(13402, 2, 'Inglés I', 2, 56),
(13383, 2, 'Análisis y Diseño de Algoritmos I', 2, 112),
(13384, 2, 'Bases de Datos', 2, 112),
(13385, 2, 'Ingeniería de Software I', 2, 112),
(13403, 2, 'Inglés II', 2, 56),
(13386, 2, 'Paradigmas y Lenguajes de Programación', 3, 112),
(13387, 2, 'Ingeniería de Software II', 3, 112),
(13388, 2, 'Sistemas Operativos y Redes', 3, 112),
(13389, 2, 'Seminario de Redacción Informativa', 3, 56),
(13390, 2, 'Sistemas Distribuidos', 3, 112),
(13391, 2, 'Lógica para Ciencias de la Computación', 3, 112),
(13392, 2, 'Análisis Matemático', 2, 112),
(13393, 2, 'Metodología de la Investigación', 2, 56),
(13394, 2, 'Probabilidad y Estadística', 2, 112),
(13395, 2, 'Análisis y Diseño de Algoritmos II', 2, 112),
(13396, 2, 'Teoría de la Computación I', 2, 112),
(13397, 2, 'Simulación y Métodos Numéricos', 2, 112),
(13398, 2, 'Inteligencia Artificial', 2, 112),
(13399, 2, 'Teoría de la Computación II', 5, 112),
(13400, 2, 'Compiladores e Intérpretes', 5, 112),
(13401, 2, 'Trabajo Final', 5, 224);

-- Licenciatura optativas
INSERT INTO Materia (codigo, plan_estudio_id, nombre, anio_cursada, carga_horaria_total) VALUES
(11944, 2, 'Cálculo numérico', 5, 112),
(11999, 2, 'Semántica de los lenguajes de programación', 5, 112),
(13124, 2, 'Diseño de Software Orientado a Objetos', 5, 112),
(13305, 2, 'Geometría y Álgebra Lineal', 5, 112),
(13308, 2, 'Validación y Verificación de Software', 5, 112),
(13335, 2, 'Bases de Datos II', 5, 112),
(13343, 2, 'Análisis Estático de Programas', 5, 112),
(13345, 2, 'Computación Gráfica', 5, 112),
(13347, 2, 'Testing de software', 5, 112),
(13348, 2, 'Introducción a la transformación de modelos de software usando QVT', 5, 112),
(13367, 2, 'Concurrencia', 5, 112),
(13327, 2, 'Geometría', 5, 112),
(11947, 2, 'Álgebra', 5, 112),
(11984, 2, 'Cálculo II', 5, 112),
(13415, 2, 'Taller de Sistemas Operativos', 5, 112);
-- Licenciatura obligatorias correlatividades
INSERT INTO Correlatividad (materia_codigo, materia_correlativa_codigo, condicion, tipo_requisito) VALUES
(13378, 13375, 'REGULAR', 'CURSAR'),
(13378, 13375, 'APROBADA', 'RENDIR'),
(13379, 13376, 'REGULAR', 'CURSAR'),
(13379, 13376, 'APROBADA', 'RENDIR'),
(13380, 13378, 'REGULAR', 'CURSAR'),
(13380, 13379, 'REGULAR', 'CURSAR'),
(13380, 13377, 'REGULAR', 'CURSAR'),
(13380, 13375, 'APROBADA', 'CURSAR'),
(13380, 13378, 'APROBADA', 'RENDIR'),
(13380, 13379, 'APROBADA', 'RENDIR'),
(13380, 13375, 'APROBADA', 'RENDIR'),
(13381, 13375, 'REGULAR', 'CURSAR'),
(13381, 13376, 'REGULAR', 'CURSAR'),
(13381, 13375, 'APROBADA', 'RENDIR'),
(13381, 13376, 'APROBADA', 'RENDIR'),
(13381, 13377, 'APROBADA', 'RENDIR'),
(13382, 13375, 'REGULAR', 'CURSAR'),
(13382, 13375, 'APROBADA', 'RENDIR'),
(13383, 13380, 'REGULAR', 'CURSAR'),
(13383, 13378, 'APROBADA', 'CURSAR'),
(13383, 13378, 'APROBADA', 'RENDIR'),
(13383, 13380, 'APROBADA', 'RENDIR'),
(13384, 13378, 'REGULAR', 'CURSAR'),
(13384, 13375, 'APROBADA', 'CURSAR'),
(13384, 13378, 'APROBADA', 'RENDIR'),
(13384, 13375, 'APROBADA', 'RENDIR'),
(13385, 13378, 'REGULAR', 'CURSAR'),
(13385, 13375, 'APROBADA', 'CURSAR'),
(13385, 13375, 'APROBADA', 'RENDIR'),
(13385, 13378, 'APROBADA', 'RENDIR'),
(13403, 13402, 'REGULAR', 'CURSAR'),
(13403, 13402, 'APROBADA', 'RENDIR'),
(13386, 13381, 'REGULAR', 'CURSAR'),
(13386, 13378, 'REGULAR', 'CURSAR'),
(13386, 13375, 'APROBADA', 'CURSAR'),
(13386, 13375, 'APROBADA', 'RENDIR'),
(13386, 13381, 'APROBADA', 'RENDIR'),
(13386, 13378, 'APROBADA', 'RENDIR'),
(13387, 13385, 'REGULAR', 'CURSAR'),
(13387, 13378, 'APROBADA', 'CURSAR'),
(13387, 13382, 'APROBADA', 'CURSAR'),
(13387, 13385, 'APROBADA', 'RENDIR'),
(13387, 13380, 'APROBADA', 'RENDIR'),
(13387, 13382, 'APROBADA', 'RENDIR'),
(13388, 13381, 'REGULAR', 'CURSAR'),
(13388, 13378, 'REGULAR', 'CURSAR'),
(13388, 13375, 'APROBADA', 'CURSAR'),
(13388, 13378, 'APROBADA', 'RENDIR'),
(13388, 13381, 'APROBADA', 'RENDIR'),
(13388, 13375, 'APROBADA', 'RENDIR'),
(13389, 13382, 'REGULAR', 'CURSAR'),
(13389, 13382, 'APROBADA', 'RENDIR'),
(13390, 13388, 'REGULAR', 'CURSAR'),
(13390, 13380, 'REGULAR', 'CURSAR'),
(13390, 13384, 'APROBADA', 'CURSAR'),
(13390, 13388, 'APROBADA', 'RENDIR'),
(13390, 13380, 'APROBADA', 'RENDIR'),
(13390, 13384, 'APROBADA', 'RENDIR'),
(13391, 13383, 'REGULAR', 'CURSAR'),
(13391, 13380, 'APROBADA', 'CURSAR'),
(13391, 13380, 'APROBADA', 'RENDIR'),
(13391, 13383, 'APROBADA', 'RENDIR'),
(13392, 13378, 'REGULAR', 'CURSAR'),
(13392, 13379, 'APROBADA', 'CURSAR'),
(13392, 13378, 'APROBADA', 'RENDIR'),
(13392, 13379, 'APROBADA', 'RENDIR'),
(13393, 13385, 'APROBADA', 'CURSAR'),
(13393, 13385, 'APROBADA', 'RENDIR'),
(13394, 13392, 'REGULAR', 'CURSAR'),
(13394, 13392, 'APROBADA', 'RENDIR'),
(13395, 13390, 'REGULAR', 'CURSAR'),
(13395, 13383, 'APROBADA', 'CURSAR'),
(13395, 13390, 'APROBADA', 'RENDIR'),
(13395, 13383, 'APROBADA', 'RENDIR'),
(13396, 13391, 'APROBADA', 'CURSAR'),
(13396, 13391, 'APROBADA', 'RENDIR'),
(13397, 13394, 'REGULAR', 'CURSAR'),
(13397, 13392, 'APROBADA', 'RENDIR'),
(13397, 13394, 'APROBADA', 'RENDIR'),
(13398, 13395, 'REGULAR', 'CURSAR'),
(13398, 13380, 'APROBADA', 'CURSAR'),
(13398, 13380, 'APROBADA', 'RENDIR'),
(13398, 13395, 'APROBADA', 'RENDIR'),
(13399, 13396, 'REGULAR', 'CURSAR'),
(13399, 13391, 'APROBADA', 'CURSAR'),
(13399, 13391, 'APROBADA', 'RENDIR'),
(13399, 13396, 'APROBADA', 'RENDIR'),
(13400, 13396, 'REGULAR', 'CURSAR'),
(13400, 13386, 'APROBADA', 'CURSAR'),
(13400, 13386, 'APROBADA', 'RENDIR'),
(13400, 13396, 'APROBADA', 'RENDIR');



-- DOCENTES
INSERT INTO users (id, dni, nombre, apellido, direccion, telefono, nombre_usuario, password, nivel_acceso) VALUES
(11, '21222000', 'Silvia',  'Romero',   'Centro',       '3584100002', 'sromero',   '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'DOCENTE'),
(12, '22333000', 'Gustavo', 'Ferreyra', 'Alberdi',      '3584100003', 'gferreyra', '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'DOCENTE'),
(13, '23444000', 'Martín',  'Gómez',    'Banda Norte',  '3584100004', 'mgomez',    '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'DOCENTE'),
(14, '24555000', 'Ana',     'Paz',      'Centro',       '3584100005', 'apaz',      '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'DOCENTE');

-- SECRETARÍA
INSERT INTO users (id, dni, nombre, apellido, direccion, telefono, nombre_usuario, password, nivel_acceso) VALUES
(20, '30444000', 'Laura',   'Medina',   'Riverside',   '3584200001', 'lmedina',   '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'SECRETARIA');

-- ESTUDIANTES
INSERT INTO users (id, dni, nombre, apellido, direccion, telefono, nombre_usuario, password, nivel_acceso) VALUES
(30, '40111001', 'Valentina', 'López',    'Banda Norte',  '3584300001', 'vlopez',    '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'ESTUDIANTE'),
(31, '41222002', 'Matías',   'González', 'Centro',       '3584300002', 'mgonzalez', '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'ESTUDIANTE'),
(32, '42333003', 'Camila',   'Torres',   'Alberdi',      '3584300003', 'ctorres',   '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'ESTUDIANTE'),
(33, '43444004', 'Tomás',    'Sosa',     'Banda Norte',  '3584300004', 'tsosa',     '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'ESTUDIANTE'),
(34, '44555005', 'Juliana',  'Ríos',     'Centro',       '3584300005', 'jrios',     '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'ESTUDIANTE'),
(35, '45666006', 'Nicolás',  'Vega',     'Alberdi',      '3584300006', 'nvega',     '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'ESTUDIANTE'),
(36, '46777007', 'Lucía',    'Mora',     'Centro',       '3584300007', 'lmora',     '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'ESTUDIANTE'),
(37, '47888008', 'Facundo',  'Luna',     'Banda Norte',  '3584300008', 'fluna',     '$2a$10$cR1nJ4mhxvYeWZLL9FlwkeXIOkjWA5tf9BiBR26IdaI9R1wi6mP3u', 'ESTUDIANTE');

INSERT INTO teacher (usuario_id, legajo_docente, cuil, email, especialidad) VALUES
(11, 'D-3002', '27-21222000-4', 'sromero@exa.unrc.edu.ar',   'Ingeniería de Software y Metodologías'),
(12, 'D-3003', '20-22333000-5', 'gferreyra@exa.unrc.edu.ar', 'Sistemas Operativos y Redes'),
(13, 'D-3004', '20-23444000-6', 'mgomez@exa.unrc.edu.ar',    'Bases de Datos'),
(14, 'D-3005', '27-24555000-7', 'apaz@exa.unrc.edu.ar',      'Inteligencia Artificial');

INSERT INTO secretariaAcademica (usuario_id, oficina, interno) VALUES
(20, 'Pabellón 1 - Piso 2', '312');

INSERT INTO student (usuario_id, legajo, tipo_estudiante, plan_estudio_id) VALUES
(30, 'E-2001', 'REGULAR',     1),
(31, 'E-2002', 'REGULAR',     1),
(32, 'E-2003', 'REGULAR',     1),
(33, 'E-2004', 'REGULAR',     1),
(34, 'E-2005', 'REGULAR',     2),
(35, 'E-2006', 'VOCACIONAL',  2),
(36, 'E-2007', 'INTERCAMBIO', 2),
(37, 'E-2008', 'REGULAR',     2);

-- Contraseña para todos: 1234
INSERT INTO gestorSistema (usuario_id, area_responsabilidad) VALUES
(1, 'Administración general del sistema');

INSERT INTO Docente_Materia (teacher_id, materia_id) VALUES
(11, 3385), -- Ing Soft I (Analista)
(11, 3387), -- Ing Soft II (Analista)
(12, 3381), -- Org Comp (Analista)
(12, 3388), -- SO y Redes (Analista)
(13, 13384), -- BD (Lic)
(13, 13335), -- BD II (Lic)
(14, 13398), -- IA (Lic)
(14, 13395); -- ADA II (Lic)

INSERT INTO Docente_Carrera (teacher_id, carrera_id) VALUES
(11, 1),
(12, 1),
(13, 2),
(14, 2);

INSERT INTO Materia_Periodo (id, materia_codigo, anio_academico, tipo_cuatrimestre) VALUES
(1, 3410, 2026, 'PRIMER_CUATRIMESTRE'), -- Intro a la Comp (Analista)
(2, 3385, 2026, 'PRIMER_CUATRIMESTRE'), -- Ing Soft I (Analista)
(3, 13375, 2026, 'PRIMER_CUATRIMESTRE'), -- Intro Algor (Lic)
(4, 13384, 2026, 'PRIMER_CUATRIMESTRE'); -- BD (Lic)

-- Periodos generados automaticamente para que la grilla no diga N/A
INSERT INTO Materia_Periodo (id, materia_codigo, anio_academico, tipo_cuatrimestre) VALUES
(5, 3376, 2026, 'PRIMER_CUATRIMESTRE'),
(6, 3377, 2026, 'SEGUNDO_CUATRIMESTRE'),
(7, 3411, 2026, 'SEGUNDO_CUATRIMESTRE'),
(8, 3379, 2026, 'SEGUNDO_CUATRIMESTRE'),
(9, 3412, 2026, 'PRIMER_CUATRIMESTRE'),
(10, 3381, 2026, 'SEGUNDO_CUATRIMESTRE'),
(11, 3382, 2026, 'PRIMER_CUATRIMESTRE'),
(12, 3402, 2026, 'PRIMER_CUATRIMESTRE'),
(13, 3383, 2026, 'PRIMER_CUATRIMESTRE'),
(14, 3384, 2026, 'PRIMER_CUATRIMESTRE'),
(15, 3403, 2026, 'SEGUNDO_CUATRIMESTRE'),
(16, 3386, 2026, 'PRIMER_CUATRIMESTRE'),
(17, 3387, 2026, 'SEGUNDO_CUATRIMESTRE'),
(18, 3388, 2026, 'PRIMER_CUATRIMESTRE'),
(19, 3389, 2026, 'PRIMER_CUATRIMESTRE'),
(20, 3390, 2026, 'PRIMER_CUATRIMESTRE'),
(21, 3414, 2026, 'PRIMER_CUATRIMESTRE'),
(22, 3124, 2026, 'PRIMER_CUATRIMESTRE'),
(23, 3305, 2026, 'SEGUNDO_CUATRIMESTRE'),
(24, 3335, 2026, 'SEGUNDO_CUATRIMESTRE'),
(25, 3347, 2026, 'SEGUNDO_CUATRIMESTRE'),
(26, 3348, 2026, 'PRIMER_CUATRIMESTRE'),
(27, 3327, 2026, 'SEGUNDO_CUATRIMESTRE'),
(28, 1947, 2026, 'SEGUNDO_CUATRIMESTRE'),
(29, 1984, 2026, 'SEGUNDO_CUATRIMESTRE'),
(30, 1998, 2026, 'PRIMER_CUATRIMESTRE'),
(31, 3395, 2026, 'SEGUNDO_CUATRIMESTRE'),
(32, 3391, 2026, 'SEGUNDO_CUATRIMESTRE'),
(33, 13376, 2026, 'PRIMER_CUATRIMESTRE'),
(34, 13377, 2026, 'SEGUNDO_CUATRIMESTRE'),
(35, 13378, 2026, 'PRIMER_CUATRIMESTRE'),
(36, 13379, 2026, 'SEGUNDO_CUATRIMESTRE'),
(37, 13380, 2026, 'SEGUNDO_CUATRIMESTRE'),
(38, 13381, 2026, 'SEGUNDO_CUATRIMESTRE'),
(39, 13382, 2026, 'PRIMER_CUATRIMESTRE'),
(40, 13402, 2026, 'PRIMER_CUATRIMESTRE'),
(41, 13383, 2026, 'PRIMER_CUATRIMESTRE'),
(42, 13385, 2026, 'PRIMER_CUATRIMESTRE'),
(43, 13403, 2026, 'SEGUNDO_CUATRIMESTRE'),
(44, 13386, 2026, 'PRIMER_CUATRIMESTRE'),
(45, 13387, 2026, 'SEGUNDO_CUATRIMESTRE'),
(46, 13388, 2026, 'PRIMER_CUATRIMESTRE'),
(47, 13389, 2026, 'PRIMER_CUATRIMESTRE'),
(48, 13390, 2026, 'PRIMER_CUATRIMESTRE'),
(49, 13391, 2026, 'SEGUNDO_CUATRIMESTRE'),
(50, 13392, 2026, 'PRIMER_CUATRIMESTRE'),
(51, 13393, 2026, 'PRIMER_CUATRIMESTRE'),
(52, 13394, 2026, 'PRIMER_CUATRIMESTRE'),
(53, 13395, 2026, 'SEGUNDO_CUATRIMESTRE'),
(54, 13396, 2026, 'PRIMER_CUATRIMESTRE'),
(55, 13397, 2026, 'SEGUNDO_CUATRIMESTRE'),
(56, 13398, 2026, 'PRIMER_CUATRIMESTRE'),
(57, 13399, 2026, 'SEGUNDO_CUATRIMESTRE'),
(58, 13400, 2026, 'PRIMER_CUATRIMESTRE'),
(59, 13401, 2026, 'SEGUNDO_CUATRIMESTRE'),
(60, 11944, 2026, 'PRIMER_CUATRIMESTRE'),
(61, 11999, 2026, 'SEGUNDO_CUATRIMESTRE'),
(62, 13124, 2026, 'PRIMER_CUATRIMESTRE'),
(63, 13305, 2026, 'SEGUNDO_CUATRIMESTRE'),
(64, 13308, 2026, 'PRIMER_CUATRIMESTRE'),
(65, 13335, 2026, 'SEGUNDO_CUATRIMESTRE'),
(66, 13343, 2026, 'SEGUNDO_CUATRIMESTRE'),
(67, 13345, 2026, 'SEGUNDO_CUATRIMESTRE'),
(68, 13347, 2026, 'SEGUNDO_CUATRIMESTRE'),
(69, 13348, 2026, 'PRIMER_CUATRIMESTRE'),
(70, 13367, 2026, 'SEGUNDO_CUATRIMESTRE'),
(71, 13327, 2026, 'SEGUNDO_CUATRIMESTRE'),
(72, 11947, 2026, 'SEGUNDO_CUATRIMESTRE'),
(73, 11984, 2026, 'SEGUNDO_CUATRIMESTRE'),
(74, 13415, 2026, 'SEGUNDO_CUATRIMESTRE');


INSERT INTO Aula (id, numero_nombre, capacidad, estado_aula) VALUES
(1, 'Aula 10 - Pabellón 1',      60,  'DISPONIBLE'),
(2, 'Aula 11 - Pabellón 1',      60,  'DISPONIBLE'),
(3, 'Laboratorio 101',   40,  'DISPONIBLE'),
(4, 'Laboratorio 110',   40,  'DISPONIBLE'),
(5, 'Aula Magna',                250,  'OCUPADA'),
(6, 'Aula 3 - Pabellón 2',       40,  'EN_MANTENIMIENTO');

INSERT INTO SolicitudAula (aula_id, materia_periodo_id, dia_semana, horario_inicio, horario_fin, estado_solicitud) VALUES
(3, 1, 'LUNES',    '08:00:00', '10:00:00', 'APROBADA'),   
(1, 2, 'MARTES',   '10:00:00', '12:00:00', 'APROBADA'),   
(2, 3, 'MIERCOLES','14:00:00', '17:00:00', 'PENDIENTE'),  
(4, 4, 'JUEVES',   '09:00:00', '12:00:00', 'PENDIENTE');

INSERT INTO Aula_Asignacion (materia_periodo_id, teacher_id, aula) VALUES
(1, 12, 'Laboratorio 1 - Exactas'),
(2, 11, 'Aula 10 - Pabellón 1');

INSERT INTO Anuncio (id, materia_periodo_id, teacher_id, tipo, titulo, contenido, fecha_examen) VALUES
(1, 1, 12, 'GENERAL', 'Apunte Unidad 1 disponible', 'El material ya está en el campus virtual. Leer antes del jueves.', NULL),
(2, 1, 12, 'EXAMEN',  'Primer Parcial', 'Temas de la unidad 1 y 2.', '2026-04-25'),
(3, 3, 13, 'GENERAL', 'Bienvenidos', 'Arrancamos con Bases de Datos.', NULL);

INSERT INTO Estado_Academico (usuario_id, materia_codigo, estado) VALUES
(30, 3410, 'APROBADO'),
(30, 3385, 'REGULAR'),
(31, 3410, 'INSCRIPTO'),
(32, 3410, 'REPROBADO'),
(33, 3410, 'LIBRE'),
(34, 13375, 'PROMOCION'),
(35, 13375, 'REGULAR'),
(36, 13384, 'APROBADO'),
(37, 13384, 'INSCRIPTO');

INSERT INTO Nota (materia_periodo_id, student_id, teacher_id, valor, instancia) VALUES
(1, 30, 12, 8.00, 'PARCIAL'),
(1, 30, 12, 7.50, 'CURSADA'),
(1, 30, 12, 8.00, 'FINAL'),
(1, 31, 12, 5.00, 'PARCIAL'),
(1, 32, 12, 3.00, 'PARCIAL'),
(3, 34, 13, 9.50, 'CURSADA');

INSERT INTO Inscripcion_Parcial (usuario_id, anuncio_id) VALUES
(30, 2),
(31, 2),
(32, 2);

INSERT INTO mesas_examen (id, materia_codigo, fecha) VALUES
(1, 3410, '2026-07-14'),
(2, 3385, '2026-07-16'),
(3, 13375, '2026-07-21'),
(4, 13384, '2026-12-09');

INSERT INTO inscripciones_examen (usuario_id, mesa_id) VALUES
(31, 1),
(35, 3),
(33, 1);
