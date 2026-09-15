package com.sigu.services;

import com.sigu.models.DocenteMateria;
import com.sigu.models.EstadoAcademico;
import com.sigu.models.MateriaPeriodo;
import com.sigu.models.Nota;
import org.javalite.activejdbc.Base;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotaService {

    /** Registra una nota de cursada y actualiza el Estado_Academico del alumno. */
    public void registrarNotaCursada(int teacherId, int materiaId, int studentId, double valor, String estadoCursada) throws Exception {
        if (DocenteMateria.findFirst("teacher_id = ? AND materia_id = ?", teacherId, materiaId) == null) {
            throw new SecurityException("No tenés acceso a esa materia.");
        }

        MateriaPeriodo mp = MateriaPeriodo.findFirst("materia_codigo = ?", materiaId);
        if (mp == null) {
            throw new IllegalStateException("No existe un período activo para esta materia.");
        }

        if (valor < 0 || valor > 10) {
            throw new IllegalArgumentException("La nota debe estar entre 0 y 10.");
        }
        if (valor < 5 && ("REGULAR".equals(estadoCursada) || "PROMOCION".equals(estadoCursada))) {
            throw new IllegalArgumentException("La nota no puede ser cargada ya que no cumple con la condicion necesaria.");
        }
        if (valor >= 5 && "LIBRE".equals(estadoCursada)) {
            throw new IllegalArgumentException("La nota no puede ser cargada ya que no cumple con la condicion necesaria.");
        }
        if (valor < 7 && "PROMOCION".equals(estadoCursada)) {
            throw new IllegalArgumentException("La nota no puede ser cargada ya que no cumple con la condicion necesaria (la promoción requiere nota mayor o igual a 7).");
        }

        List<Map> estadoRows = Base.findAll(
            "SELECT estado FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", studentId, materiaId);
        Map estadoRow = estadoRows.isEmpty() ? null : estadoRows.get(0);
        if (estadoRow == null ||
            (!"INSCRIPTO".equals(estadoRow.get("estado")) && !"REGULAR".equals(estadoRow.get("estado")))) {
            throw new SecurityException("El alumno no está inscripto o no es válido para recibir nota en esta materia.");
        }

        Nota nota = new Nota();
        nota.set("materia_periodo_id", mp.getId());
        nota.set("student_id", studentId);
        nota.set("teacher_id", teacherId);
        nota.set("valor", valor);
        nota.set("instancia", "CURSADA");
        nota.saveIt();

        String estadoFinal = "PROMOCION".equals(estadoCursada) ? "PROMOCION" : estadoCursada;
        EstadoAcademico ea = EstadoAcademico.findFirst("usuario_id = ? AND materia_codigo = ?", studentId, materiaId);
        if (ea == null) {
            ea = new EstadoAcademico();
            ea.set("usuario_id", studentId);
            ea.set("materia_codigo", materiaId);
        }
        ea.set("estado", estadoFinal);
        ea.saveIt();
    }

    /** Mesas de examen de las materias que el docente tiene asignadas. */
    public List<Map<String, Object>> getMesasDelDocente(int teacherId) {
        List<Map> mesas = Base.findAll(
            "SELECT me.id, me.fecha, m.nombre AS nombreMateria, me.materia_codigo " +
            "FROM mesas_examen me " +
            "JOIN Materia m ON m.codigo = me.materia_codigo " +
            "JOIN Docente_Materia dm ON dm.materia_id = me.materia_codigo " +
            "WHERE dm.teacher_id = ? " +
            "ORDER BY me.fecha DESC", teacherId);

        List<Map<String, Object>> mesasList = new ArrayList<>();
        for (Map m : mesas) {
            Map<String, Object> mm = new HashMap<>();
            mm.put("id", ((Number) m.get("id")).intValue());
            mm.put("fecha", m.get("fecha"));
            mm.put("nombreMateria", m.get("nombreMateria"));
            mm.put("materiaCodigo", m.get("materia_codigo"));
            mesasList.add(mm);
        }
        return mesasList;
    }

    /** Datos de una mesa + sus inscriptos, validando que sea del docente indicado. */
    public Map<String, Object> getMesaConInscriptos(int mesaId, int teacherId) throws Exception {
        List<Map> mesaRows = Base.findAll(
            "SELECT me.id, me.fecha, me.materia_codigo, m.nombre AS nombreMateria " +
            "FROM mesas_examen me JOIN Materia m ON m.codigo = me.materia_codigo " +
            "JOIN Docente_Materia dm ON dm.materia_id = me.materia_codigo " +
            "WHERE me.id = ? AND dm.teacher_id = ?", mesaId, teacherId);
        if (mesaRows.isEmpty()) {
            throw new SecurityException("No tenés acceso a esa mesa.");
        }
        Map mesa = mesaRows.get(0);

        List<Map> inscriptosDB = Base.findAll(
            "SELECT ie.usuario_id, u.nombre, u.apellido, s.legajo " +
            "FROM inscripciones_examen ie " +
            "JOIN users u ON u.id = ie.usuario_id " +
            "JOIN student s ON s.usuario_id = ie.usuario_id " +
            "WHERE ie.mesa_id = ? " +
            "ORDER BY u.apellido ASC", mesaId);

        List<Map<String, Object>> inscriptosList = new ArrayList<>();
        for (Map i : inscriptosDB) {
            Map<String, Object> im = new HashMap<>();
            im.put("usuarioId", ((Number) i.get("usuario_id")).intValue());
            im.put("nombre", i.get("nombre") + " " + i.get("apellido"));
            im.put("legajo", i.get("legajo"));
            inscriptosList.add(im);
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("mesaId", mesaId);
        resultado.put("fecha", mesa.get("fecha"));
        resultado.put("nombreMateria", mesa.get("nombreMateria"));
        resultado.put("materiaCodigo", mesa.get("materia_codigo"));
        resultado.put("inscriptos", inscriptosList);
        return resultado;
    }

    /** Registra una nota final y actualiza el Estado_Academico (APROBADO/REGULAR). */
    public void registrarNotaFinal(int teacherId, int mesaId, int studentId, double valor) throws Exception {
        if (valor < 0 || valor > 10) {
            throw new IllegalArgumentException("Nota fuera de rango.");
        }

        List<Map> mesaRows = Base.findAll(
            "SELECT me.materia_codigo FROM mesas_examen me " +
            "JOIN Docente_Materia dm ON dm.materia_id = me.materia_codigo " +
            "WHERE me.id = ? AND dm.teacher_id = ?", mesaId, teacherId);
        if (mesaRows.isEmpty()) {
            throw new IllegalStateException("Mesa no autorizada.");
        }
        int materiaCodigo = ((Number) mesaRows.get(0).get("materia_codigo")).intValue();

        List<Map> periodoRows = Base.findAll(
            "SELECT id FROM Materia_Periodo WHERE materia_codigo = ? LIMIT 1", materiaCodigo);
        if (periodoRows.isEmpty()) {
            throw new IllegalStateException("No hay período activo.");
        }
        int periodoId = ((Number) periodoRows.get(0).get("id")).intValue();

        Nota nota = new Nota();
        nota.set("materia_periodo_id", periodoId);
        nota.set("student_id", studentId);
        nota.set("teacher_id", teacherId);
        nota.set("valor", valor);
        nota.set("instancia", "FINAL");
        nota.saveIt();

        String nuevoEstado = (valor >= 4.0) ? "APROBADO" : "REGULAR";
        EstadoAcademico ea = EstadoAcademico.findFirst("usuario_id = ? AND materia_codigo = ?", studentId, materiaCodigo);
        if (ea == null) {
            ea = new EstadoAcademico();
            ea.set("usuario_id", studentId);
            ea.set("materia_codigo", materiaCodigo);
        }
        ea.set("estado", nuevoEstado);
        ea.saveIt();
    }
}
