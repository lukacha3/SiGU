package com.is1.proyecto.services;

import com.is1.proyecto.TestDatabaseSupport;
import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class NotaServiceTest {

    private static final int TEACHER_ID = 1;
    private static final int STUDENT_ID = 2;
    private static final int MATERIA_ID = 101;

    private NotaService service;

    @BeforeAll
    static void setupSchema() throws Exception {
        TestDatabaseSupport.resetSchema();
    }

    @BeforeEach
    void seedData() {
        Base.exec("DELETE FROM Nota");
        Base.exec("DELETE FROM Estado_Academico");
        Base.exec("DELETE FROM Docente_Materia");
        Base.exec("DELETE FROM Materia_Periodo");
        Base.exec("DELETE FROM Materia");
        Base.exec("DELETE FROM Plan_Estudio");
        Base.exec("DELETE FROM mesas_examen");
        Base.exec("DELETE FROM teacher");
        Base.exec("DELETE FROM student");
        Base.exec("DELETE FROM users");

        Base.exec("INSERT INTO Plan_Estudio (id, carrera_id, anio_resolucion) VALUES (1, 1, 2026)");
        Base.exec("INSERT INTO Materia (codigo, plan_estudio_id, nombre, anio_cursada, carga_horaria_total) VALUES (?, 1, 'Materia Test', 1, 60)", MATERIA_ID);
        Base.exec("INSERT INTO Materia_Periodo (materia_codigo, anio_academico, tipo_cuatrimestre) VALUES (?, 2026, '1C')", MATERIA_ID);

        Base.exec("INSERT INTO users (id, nombre, apellido, dni, nombre_usuario, password, nivel_acceso) VALUES (?, 'Docente', 'Test', '111111', 'docente.test', 'x', 'DOCENTE')", TEACHER_ID);
        Base.exec("INSERT INTO teacher (usuario_id, legajo_docente, cuil, email) VALUES (?, 'D001', '20-1-1', 'd@test.com')", TEACHER_ID);
        Base.exec("INSERT INTO Docente_Materia (teacher_id, materia_id) VALUES (?, ?)", TEACHER_ID, MATERIA_ID);

        Base.exec("INSERT INTO users (id, nombre, apellido, dni, nombre_usuario, password, nivel_acceso) VALUES (?, 'Alumno', 'Test', '222222', 'alumno.test', 'x', 'ESTUDIANTE')", STUDENT_ID);
        Base.exec("INSERT INTO student (usuario_id, legajo, tipo_estudiante, plan_estudio_id) VALUES (?, '999', 'REGULAR', 1)", STUDENT_ID);
        Base.exec("INSERT INTO Estado_Academico (usuario_id, materia_codigo, estado) VALUES (?, ?, 'REGULAR')", STUDENT_ID, MATERIA_ID);

        service = new NotaService();
    }

    @Test
    void notaFueraDeRango_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarNotaCursada(TEACHER_ID, MATERIA_ID, STUDENT_ID, 11, "REGULAR"));
    }

    @Test
    void notaMenorA5ConRegular_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarNotaCursada(TEACHER_ID, MATERIA_ID, STUDENT_ID, 4.5, "REGULAR"));
    }

    @Test
    void notaAprobatoriaConLibre_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarNotaCursada(TEACHER_ID, MATERIA_ID, STUDENT_ID, 5.0, "LIBRE"));
    }

    @Test
    void promocionConMenosDe7_lanzaExcepcion() {
        assertThrows(IllegalArgumentException.class,
            () -> service.registrarNotaCursada(TEACHER_ID, MATERIA_ID, STUDENT_ID, 6.5, "PROMOCION"));
    }

    @Test
    void promocionCon7OMas_seRegistraCorrectamente() throws Exception {
        service.registrarNotaCursada(TEACHER_ID, MATERIA_ID, STUDENT_ID, 8.0, "PROMOCION");

        java.util.List<java.util.Map> estado = Base.findAll(
            "SELECT estado FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", STUDENT_ID, MATERIA_ID);
        assertEquals("PROMOCION", estado.get(0).get("estado"));
    }

    @Test
    void docenteSinAccesoALaMateria_lanzaExcepcion() {
        assertThrows(SecurityException.class,
            () -> service.registrarNotaCursada(TEACHER_ID, 999, STUDENT_ID, 8.0, "REGULAR"));
    }

    @Test
    void alumnoNoInscripto_lanzaExcepcion() {
        Base.exec("DELETE FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", STUDENT_ID, MATERIA_ID);
        assertThrows(SecurityException.class,
            () -> service.registrarNotaCursada(TEACHER_ID, MATERIA_ID, STUDENT_ID, 8.0, "REGULAR"));
    }

    @Test
    void notaFinalAprobatoria_actualizaEstadoAAprobado() throws Exception {
        Base.exec("INSERT INTO mesas_examen (id, materia_codigo, fecha) VALUES (1, ?, '2026-12-01')", MATERIA_ID);
        service.registrarNotaFinal(TEACHER_ID, 1, STUDENT_ID, 7.0);

        java.util.List<java.util.Map> estado = Base.findAll(
            "SELECT estado FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", STUDENT_ID, MATERIA_ID);
        assertEquals("APROBADO", estado.get(0).get("estado"));
    }

    @Test
    void notaFinalNoAprobatoria_dejaEstadoRegular() throws Exception {
        Base.exec("INSERT INTO mesas_examen (id, materia_codigo, fecha) VALUES (1, ?, '2026-12-01')", MATERIA_ID);
        service.registrarNotaFinal(TEACHER_ID, 1, STUDENT_ID, 3.0);

        java.util.List<java.util.Map> estado = Base.findAll(
            "SELECT estado FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", STUDENT_ID, MATERIA_ID);
        assertEquals("REGULAR", estado.get(0).get("estado"));
    }
}
