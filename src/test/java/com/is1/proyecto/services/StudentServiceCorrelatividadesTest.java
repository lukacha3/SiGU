package com.is1.proyecto.services;

import com.is1.proyecto.TestDatabaseSupport;
import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.*;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class StudentServiceCorrelatividadesTest {

    private static final int PLAN_ID = 1;
    private static final int ALUMNO_ID = 99;
    private static final int MATERIA_A = 101; // sin requisitos
    private static final int MATERIA_B = 102; // requiere A REGULAR para cursar
    private static final int MATERIA_C = 103; // requiere A APROBADA para cursar

    private StudentService service;

    @BeforeAll
    static void setupSchema() throws Exception {
        TestDatabaseSupport.resetSchema();
    }

    @BeforeEach
    void seedData() {
        // Limpiá las tablas relevantes antes de cada test para aislar casos
        Base.exec("DELETE FROM Estado_Academico");
        Base.exec("DELETE FROM Correlatividad");
        Base.exec("DELETE FROM Materia");
        Base.exec("DELETE FROM Plan_Estudio");
        Base.exec("DELETE FROM Carrera");
        Base.exec("DELETE FROM users");
        Base.exec("DELETE FROM student");

        Base.exec("INSERT INTO Carrera (id, nombre, duracion_anios, titulo_otorgado) VALUES (1, 'Analista en Sistemas', 3, 'Analista')");
        Base.exec("INSERT INTO Plan_Estudio (id, carrera_id, anio_resolucion, estado) VALUES (?, 1, 2020, 'VIGENTE')", PLAN_ID);

        Base.exec("INSERT INTO Materia (codigo, nombre, plan_estudio_id, anio_cursada, carga_horaria_total) VALUES (?, 'Materia A', ?, 1, 60)", MATERIA_A, PLAN_ID);
        Base.exec("INSERT INTO Materia (codigo, nombre, plan_estudio_id, anio_cursada, carga_horaria_total) VALUES (?, 'Materia B', ?, 1, 60)", MATERIA_B, PLAN_ID);
        Base.exec("INSERT INTO Materia (codigo, nombre, plan_estudio_id, anio_cursada, carga_horaria_total) VALUES (?, 'Materia C', ?, 2, 60)", MATERIA_C, PLAN_ID);

        Base.exec("INSERT INTO Correlatividad (materia_codigo, materia_correlativa_codigo, tipo_requisito, condicion) VALUES (?, ?, 'CURSAR', 'REGULAR')", MATERIA_B, MATERIA_A);
        Base.exec("INSERT INTO Correlatividad (materia_codigo, materia_correlativa_codigo, tipo_requisito, condicion) VALUES (?, ?, 'CURSAR', 'APROBADA')", MATERIA_C, MATERIA_A);

        Base.exec("INSERT INTO users (id, dni, nombre, apellido, nombre_usuario, password, nivel_acceso) VALUES (?, '11111111', 'Test', 'Alumno', 'test.alumno', 'x', 'ESTUDIANTE')", ALUMNO_ID);
        Base.exec("INSERT INTO student (usuario_id, legajo, tipo_estudiante, plan_estudio_id) VALUES (?, '999', 'REGULAR', ?)", ALUMNO_ID, PLAN_ID);

        service = new StudentService();
    }

    @Test
    void materiaSinRequisitos_apareceDisponibleDeEntrada() {
        List<Map<String, Object>> disponibles = service.getMateriasDisponiblesParaInscripcion(ALUMNO_ID, PLAN_ID);
        assertTrue(disponibles.stream().anyMatch(m -> (int) m.get("codigo") == MATERIA_A));
    }

    @Test
    void materiaConRequisitoRegular_noApareceSinCumplirlo() {
        List<Map<String, Object>> disponibles = service.getMateriasDisponiblesParaInscripcion(ALUMNO_ID, PLAN_ID);
        assertFalse(disponibles.stream().anyMatch(m -> (int) m.get("codigo") == MATERIA_B));
    }

    @Test
    void materiaConRequisitoRegular_apareceAlCumplirRegular() {
        Base.exec("INSERT INTO Estado_Academico (usuario_id, materia_codigo, estado) VALUES (?, ?, 'REGULAR')", ALUMNO_ID, MATERIA_A);
        List<Map<String, Object>> disponibles = service.getMateriasDisponiblesParaInscripcion(ALUMNO_ID, PLAN_ID);
        assertTrue(disponibles.stream().anyMatch(m -> (int) m.get("codigo") == MATERIA_B));
    }

    @Test
    void materiaConRequisitoAprobada_noAlcanzaConSoloRegular() {
        Base.exec("INSERT INTO Estado_Academico (usuario_id, materia_codigo, estado) VALUES (?, ?, 'REGULAR')", ALUMNO_ID, MATERIA_A);
        List<Map<String, Object>> disponibles = service.getMateriasDisponiblesParaInscripcion(ALUMNO_ID, PLAN_ID);
        assertFalse(disponibles.stream().anyMatch(m -> (int) m.get("codigo") == MATERIA_C));
    }

    @Test
    void materiaConRequisitoAprobada_apareceAlEstarAprobada() {
        Base.exec("INSERT INTO Estado_Academico (usuario_id, materia_codigo, estado) VALUES (?, ?, 'APROBADO')", ALUMNO_ID, MATERIA_A);
        List<Map<String, Object>> disponibles = service.getMateriasDisponiblesParaInscripcion(ALUMNO_ID, PLAN_ID);
        assertTrue(disponibles.stream().anyMatch(m -> (int) m.get("codigo") == MATERIA_C));
    }

    @Test
    void inscribirSinCumplirCorrelatividad_lanzaExcepcion() {
        assertThrows(Exception.class, () -> service.inscribirACursada(ALUMNO_ID, PLAN_ID, MATERIA_B));
    }

    @Test
    void inscribirCumpliendoCorrelatividad_creaEstadoInscripto() throws Exception {
        Base.exec("INSERT INTO Estado_Academico (usuario_id, materia_codigo, estado) VALUES (?, ?, 'REGULAR')", ALUMNO_ID, MATERIA_A);
        service.inscribirACursada(ALUMNO_ID, PLAN_ID, MATERIA_B);

        List<Map> estado = Base.findAll("SELECT estado FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", ALUMNO_ID, MATERIA_B);
        assertEquals(1, estado.size());
        assertEquals("INSCRIPTO", estado.get(0).get("estado"));
    }

    @Test
    void inscribirDosVecesEnLaMismaMateria_lanzaExcepcion() throws Exception {
        Base.exec("INSERT INTO Estado_Academico (usuario_id, materia_codigo, estado) VALUES (?, ?, 'INSCRIPTO')", ALUMNO_ID, MATERIA_A);
        assertThrows(Exception.class, () -> service.inscribirACursada(ALUMNO_ID, PLAN_ID, MATERIA_A));
    }
}
