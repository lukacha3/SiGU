package com.is1.proyecto.services;

import com.is1.proyecto.models.Materia;
import com.is1.proyecto.models.Student;
import com.is1.proyecto.models.User;
import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentService {

    /**
     * Registra un nuevo estudiante en el sistema.
     */
    public void registrarEstudiante(String name, String lastName, String dni, String email, String legajo, String tipoEstudiante, int planEstudioId) throws Exception {
        try {
            Base.openTransaction();

            User u = new User();
            u.set("nombre", name, "apellido", lastName, "dni", dni);
            u.set("nombre_usuario", email);
            u.set("nivel_acceso", "ESTUDIANTE");
            u.set("password", BCrypt.hashpw("1234", BCrypt.gensalt()));
            u.saveIt();

            Base.exec(
                "INSERT INTO student (usuario_id, legajo, tipo_estudiante, plan_estudio_id) VALUES (?, ?, ?, ?)",
                u.getId(), legajo, tipoEstudiante, planEstudioId
            );

            Base.commitTransaction();
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Obtiene el ID del plan de estudios y su nombre formateado para la vista.
     */
    public Map<String, Object> getInformacionPlan(int userId) {
        List<Map> studentRows = Base.findAll(
            "SELECT s.plan_estudio_id, c.nombre AS nombre_carrera, p.anio_resolucion " +
            "FROM student s " +
            "JOIN Plan_Estudio p ON p.id = s.plan_estudio_id " +
            "JOIN Carrera c ON c.id = p.carrera_id " +
            "WHERE s.usuario_id = ?", userId
        );
        
        if (studentRows.isEmpty()) {
            return null;
        }

        Map studentRow = studentRows.get(0);
        int planEstudioId = ((Number) studentRow.get("plan_estudio_id")).intValue();
        String nombrePlan = studentRow.get("nombre_carrera") + " — Plan " + studentRow.get("anio_resolucion");
        
        Map<String, Object> result = new HashMap<>();
        result.put("planEstudioId", planEstudioId);
        result.put("nombrePlan", nombrePlan);
        return result;
    }

    /**
     * Obtiene la historia académica del alumno para su plan de estudio.
     */
    public List<Map<String, Object>> getHistoriaAcademica(int userId, int planEstudioId) {
        List<Map> rows = Base.findAll(
            "SELECT m.codigo, m.nombre, m.anio_cursada, mp_plan.tipo_cuatrimestre, ea.estado, " +
            "(SELECT n.valor FROM Nota n JOIN Materia_Periodo mp ON mp.id = n.materia_periodo_id " +
            "WHERE mp.materia_codigo = m.codigo AND n.student_id = ? ORDER BY n.fecha_carga DESC LIMIT 1) AS nota " +
            "FROM Materia m " +
            "LEFT JOIN Materia_Periodo mp_plan ON mp_plan.materia_codigo = m.codigo " +
            "AND mp_plan.id = (SELECT MIN(id) FROM Materia_Periodo WHERE materia_codigo = m.codigo) " +
            "LEFT JOIN Estado_Academico ea ON ea.materia_codigo = m.codigo AND ea.usuario_id = ? " +
            "WHERE m.plan_estudio_id = ? " +
            "ORDER BY m.anio_cursada ASC, " +
            "CASE mp_plan.tipo_cuatrimestre WHEN 'PRIMER_CUATRIMESTRE' THEN 1 WHEN 'ANUAL' THEN 1 " +
            "WHEN 'SEGUNDO_CUATRIMESTRE' THEN 2 WHEN 'VERANO' THEN 3 ELSE 4 END ASC, m.nombre ASC",
            userId, userId, planEstudioId
        );

        List<Map<String, Object>> materias = new ArrayList<>();
        String grupoAnterior = "";

        for (Map row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("codigo", row.get("codigo"));
            item.put("nombre", row.get("nombre"));
            item.put("anio_cursada", row.get("anio_cursada"));

            String estado = (String) row.get("estado");
            item.put("estado", estado != null ? estado : "-");

            Object notaVal = row.get("nota");
            if (notaVal != null) {
                java.math.BigDecimal bd = new java.math.BigDecimal(notaVal.toString());
                item.put("nota", bd.stripTrailingZeros().toPlainString());
            } else {
                item.put("nota", "-");
            }

            String estadoClase = "";
            switch (estado != null ? estado : "") {
                case "APROBADO": estadoClase = "bg-green-100 text-green-700 dark:bg-green-500/20 dark:text-green-300 border-green-200 dark:border-green-500/40"; break;
                case "PROMOCION": estadoClase = "bg-yellow-100 text-yellow-700 dark:bg-yellow-500/20 dark:text-yellow-300 border-yellow-200 dark:border-yellow-500/40"; break;
                case "REGULAR": estadoClase = "bg-[#eaf2ff] text-[#1a3a5c] dark:bg-[#2471a3]/30 dark:text-[#eaf2ff] border-[#2471a3]/30 dark:border-[#2471a3]/50"; break;
                case "INSCRIPTO": estadoClase = "bg-purple-100 text-purple-700 dark:bg-purple-500/20 dark:text-purple-300 border-purple-200 dark:border-purple-500/40"; break;
                case "REPROBADO": estadoClase = "bg-red-100 text-red-700 dark:bg-red-500/20 dark:text-red-300 border-red-200 dark:border-red-500/40"; break;
                case "LIBRE": estadoClase = "bg-red-50 text-red-600 dark:bg-red-500/10 dark:text-red-400 border-red-200 dark:border-red-500/30"; break;
                default: estadoClase = "bg-gray-100 text-gray-500 dark:bg-gray-800/50 dark:text-gray-400 border-gray-200 dark:border-gray-700"; break;
            }
            item.put("estadoClase", estadoClase);

            int anio = ((Number) row.get("anio_cursada")).intValue();
            String cuatrimestre = row.get("tipo_cuatrimestre") != null ? (String) row.get("tipo_cuatrimestre") : "PRIMER_CUATRIMESTRE";
            String grupoActual = anio + "_" + cuatrimestre;

            if (!grupoActual.equals(grupoAnterior)) {
                String labelCuatri = "";
                switch (cuatrimestre) {
                    case "PRIMER_CUATRIMESTRE": labelCuatri = "I Cuat."; break;
                    case "SEGUNDO_CUATRIMESTRE": labelCuatri = "II Cuat."; break;
                    case "ANUAL": labelCuatri = "Anual"; break;
                    case "VERANO": labelCuatri = "Verano"; break;
                    default: labelCuatri = cuatrimestre; break;
                }
                item.put("anioHeader", anio + "° Año - " + labelCuatri);
                grupoAnterior = grupoActual;
            }
            materias.add(item);
        }
        return materias;
    }

    /**
     * Obtiene el ID del alumno y su plan de estudios a partir de su username.
     */
    public Map<String, Integer> getAlumnoIds(String currentUsername) {
        List<Map> studentRows = Base.findAll(
            "SELECT s.usuario_id, s.plan_estudio_id FROM student s JOIN users u ON u.id = s.usuario_id WHERE u.nombre_usuario = ?",
            currentUsername
        );
        if (studentRows.isEmpty()) return null;
        
        Map<String, Integer> result = new HashMap<>();
        result.put("alumnoId", ((Number) studentRows.get(0).get("usuario_id")).intValue());
        result.put("planId", ((Number) studentRows.get(0).get("plan_estudio_id")).intValue());
        return result;
    }

    /**
     * Obtiene la lista de materias disponibles para inscripción de cursada.
     */
    public List<Map<String, Object>> getMateriasDisponiblesParaInscripcion(int alumnoId, int planId) {
        List<Map<String, Object>> materiasDisponibles = new ArrayList<>();
        List<Map> materiasPlan = Base.findAll("SELECT codigo, nombre, anio_cursada FROM Materia WHERE plan_estudio_id = ?", planId);

        for (Map m : materiasPlan) {
            int materiaCodigo = ((Number) m.get("codigo")).intValue();
            List<Map> estadoRows = Base.findAll("SELECT id FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", alumnoId, materiaCodigo);
            if (!estadoRows.isEmpty()) continue;

            List<Map> requisitos = Base.findAll(
                "SELECT materia_correlativa_codigo, condicion FROM Correlatividad WHERE materia_codigo = ? AND tipo_requisito = 'CURSAR'", materiaCodigo
            );

            boolean cumpleCorrelatividades = true;
            for (Map reqItem : requisitos) {
                int reqCodigo = ((Number) reqItem.get("materia_correlativa_codigo")).intValue();
                String condicionRequerida = (String) reqItem.get("condicion");

                List<Map> estadoReqRows = Base.findAll("SELECT estado FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", alumnoId, reqCodigo);
                if (estadoReqRows.isEmpty()) { cumpleCorrelatividades = false; break; }

                String estadoActual = (String) estadoReqRows.get(0).get("estado");
                if ("APROBADA".equals(condicionRequerida) && !"APROBADO".equals(estadoActual)) { cumpleCorrelatividades = false; break; }
                if ("REGULAR".equals(condicionRequerida) && (!"REGULAR".equals(estadoActual) && !"APROBADO".equals(estadoActual))) { cumpleCorrelatividades = false; break; }
            }

            if (cumpleCorrelatividades) {
                Map<String, Object> matData = new HashMap<>();
                matData.put("codigo", materiaCodigo);
                matData.put("nombre", m.get("nombre"));
                matData.put("anio_cursada", m.get("anio_cursada"));
                materiasDisponibles.add(matData);
            }
        }
        return materiasDisponibles;
    }

    /**
     * Inscribe a un estudiante en una materia.
     */
    public void inscribirACursada(int alumnoId, int planId, int materiaCodigo) throws Exception {
        Materia materiaObj = Materia.findById(materiaCodigo);
        if (materiaObj == null || materiaObj.getInteger("plan_estudio_id") != planId) {
            throw new Exception("Materia no valida");
        }

        List<Map> estadoRows = Base.findAll("SELECT id FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", alumnoId, materiaCodigo);
        if (!estadoRows.isEmpty()) {
            throw new Exception("Ya te encuentras inscripto en esta materia");
        }

        List<Map> requisitos = Base.findAll("SELECT materia_correlativa_codigo, condicion FROM Correlatividad WHERE materia_codigo = ? AND tipo_requisito = 'CURSAR'", materiaCodigo);
        for (Map reqItem : requisitos) {
            int reqCodigo = ((Number) reqItem.get("materia_correlativa_codigo")).intValue();
            String condicionRequerida = (String) reqItem.get("condicion");

            List<Map> estadoReqRows = Base.findAll("SELECT estado FROM Estado_Academico WHERE usuario_id = ? AND materia_codigo = ?", alumnoId, reqCodigo);
            if (estadoReqRows.isEmpty()) { throw new Exception("No cumples las correlatividades"); }

            String estadoActual = (String) estadoReqRows.get(0).get("estado");
            if ("APROBADA".equals(condicionRequerida) && !"APROBADO".equals(estadoActual)) { throw new Exception("No cumples las correlatividades"); }
            if ("REGULAR".equals(condicionRequerida) && (!"REGULAR".equals(estadoActual) && !"APROBADO".equals(estadoActual))) { throw new Exception("No cumples las correlatividades"); }
        }

        Base.exec("INSERT INTO Estado_Academico (usuario_id, materia_codigo, estado) VALUES (?, ?, ?)", alumnoId, materiaCodigo, "INSCRIPTO");
    }

    /**
     * Inscribe a un estudiante en un parcial.
     */
    public void confirmarAsistenciaParcial(int alumnoId, int anuncioId) throws Exception {
        List<Map> anuncioRows = Base.findAll("SELECT tipo FROM Anuncio WHERE id = ?", anuncioId);
        if (anuncioRows.isEmpty() || !"EXAMEN".equals(anuncioRows.get(0).get("tipo"))) {
            throw new Exception("El anuncio no corresponde a un parcial.");
        }
        
        try {
            Base.exec("INSERT INTO Inscripcion_Parcial (usuario_id, anuncio_id) VALUES (?, ?)", alumnoId, anuncioId);
        } catch (Exception e) {
            throw new Exception("Ya confirmaste asistencia a este parcial.");
        }
    }

    /**
     * Obtiene los datos de un estudiante para editarlos.
     */
    public Map<String, Object> getEstudianteParaEdicion(int estudianteId) {
        List<Map> rows = Base.findAll(
            "SELECT u.id, u.nombre, u.apellido, u.dni, u.direccion, u.telefono, u.nombre_usuario, s.legajo, s.tipo_estudiante " +
            "FROM users u JOIN student s ON s.usuario_id = u.id WHERE u.id = ? AND u.nivel_acceso = 'ESTUDIANTE'", estudianteId
        );
        if (rows.isEmpty()) return null;

        Map e = rows.get(0);
        String tipo = (String) e.get("tipo_estudiante");
        
        Map<String, Object> model = new HashMap<>();
        model.put("id", ((Number) e.get("id")).intValue());
        model.put("nombre", e.get("nombre"));
        model.put("apellido", e.get("apellido"));
        model.put("dni", e.get("dni"));
        model.put("direccion", e.get("direccion") != null ? e.get("direccion") : "");
        model.put("telefono", e.get("telefono") != null ? e.get("telefono") : "");
        model.put("nombre_usuario", e.get("nombre_usuario"));
        model.put("legajo", e.get("legajo"));
        model.put("esRegular", "REGULAR".equals(tipo));
        model.put("esVocacional", "VOCACIONAL".equals(tipo));
        model.put("esIntercambio", "INTERCAMBIO".equals(tipo));

        return model;
    }

    /**
     * Actualiza los datos de un estudiante.
     */
    public void actualizarEstudiante(int estudianteId, String nombre, String apellido, String dni, String direccion, String telefono, String tipoEstudiante) throws Exception {
        try {
            Base.openTransaction();
            Base.exec("UPDATE users SET nombre = ?, apellido = ?, dni = ?, direccion = ?, telefono = ? WHERE id = ?", 
                      nombre.trim(), apellido.trim(), dni != null ? dni.trim() : "", direccion, telefono, estudianteId);
            Base.exec("UPDATE student SET tipo_estudiante = ? WHERE usuario_id = ?", tipoEstudiante, estudianteId);
            Base.commitTransaction();
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw e;
        }
    }
}
