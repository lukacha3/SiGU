package com.is1.proyecto.services;

import com.is1.proyecto.models.Anuncio;
import com.is1.proyecto.models.AulaAsignacion;
import com.is1.proyecto.models.Carrera;
import com.is1.proyecto.models.DocenteCarrera;
import com.is1.proyecto.models.DocenteMateria;
import com.is1.proyecto.models.Materia;
import com.is1.proyecto.models.MateriaPeriodo;
import com.is1.proyecto.models.Teacher;
import com.is1.proyecto.models.User;
import org.javalite.activejdbc.Base;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherService {

    public void createTeacher(String name, String lastName, String dni, String address, String phone,
                              String legajo, String cuil, String email, String especialidad, String carreraId) {
        if (name == null || name.isBlank() || lastName == null || dni == null || carreraId == null || carreraId.isBlank()) {
            throw new IllegalArgumentException("Los campos Nombre, Apellido, DNI y Carrera son obligatorios.");
        }

        Base.openTransaction();
        try {
            User u = new User();
            u.set("nombre", name, "apellido", lastName, "dni", dni);
            u.set("nombre_usuario", email);
            u.set("nivel_acceso", "DOCENTE");
            u.set("direccion", address);
            u.set("telefono", phone);

            String defaultPassHashed = BCrypt.hashpw("1234", BCrypt.gensalt());
            u.set("password", defaultPassHashed);
            u.saveIt();

            Teacher t = new Teacher();
            t.set("usuario_id", u.getId());
            t.set("legajo_docente", legajo, "cuil", cuil);
            t.set("email", email, "especialidad", especialidad);
            t.saveIt();

            DocenteCarrera dc = new DocenteCarrera();
            dc.set("teacher_id", u.getId());
            dc.set("carrera_id", Integer.parseInt(carreraId));
            dc.saveIt();

            Base.commitTransaction();
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error interno: Verifique que el legajo, CUIL o Email no estén duplicados.");
        }
    }

    public void assignMateria(int teacherId, int materiaId) {
        Teacher teacher = Teacher.findFirst("usuario_id = ?", teacherId);
        Materia materia = Materia.findFirst("codigo = ?", materiaId);

        if (teacher == null || materia == null) {
            throw new IllegalArgumentException("Docente o materia no válida.");
        }

        DocenteMateria existing = DocenteMateria.findFirst("teacher_id = ? AND materia_id = ?", teacherId, materiaId);
        if (existing != null) {
            throw new IllegalStateException("Esta asignación ya existe.");
        }

        DocenteMateria nuevo = new DocenteMateria();
        nuevo.set("teacher_id", teacherId, "materia_id", materiaId);
        nuevo.saveIt();
    }

    public List<Map> getTeacherMaterias(int teacherId) {
        return Base.findAll(
            "SELECT m.codigo, m.nombre, m.anio_cursada " +
            "FROM Materia m " +
            "JOIN Docente_Materia dm ON m.codigo = dm.materia_id " +
            "WHERE dm.teacher_id = ? " +
            "ORDER BY m.anio_cursada ASC, m.nombre ASC",
            teacherId
        );
    }

    public void assignAula(int teacherId, int materiaId, String aula) {
        if (aula == null || aula.isBlank()) {
            throw new IllegalArgumentException("Debés ingresar el nombre o número del aula.");
        }
        
        DocenteMateria asignacion = DocenteMateria.findFirst("teacher_id = ? AND materia_id = ?", teacherId, materiaId);
        if (asignacion == null) {
            throw new SecurityException("No tenés acceso a esa materia.");
        }

        MateriaPeriodo mp = MateriaPeriodo.findFirst("materia_codigo = ?", materiaId);
        if (mp == null) {
            throw new IllegalStateException("No existe un período activo para esta materia.");
        }

        AulaAsignacion asig = new AulaAsignacion();
        asig.set("materia_periodo_id", mp.getId());
        asig.set("teacher_id", teacherId);
        asig.set("aula", aula);
        asig.saveIt();
    }

    public Map getTeacherForEdit(int docenteId) {
        List<Map> rows = Base.findAll(
            "SELECT u.id, u.nombre, u.apellido, u.dni, u.direccion, u.telefono, " +
            "       u.nombre_usuario, t.legajo_docente, t.cuil, t.email, t.especialidad " +
            "FROM users u JOIN teacher t ON t.usuario_id = u.id " +
            "WHERE u.id = ? AND u.nivel_acceso = 'DOCENTE'",
            docenteId
        );
        if (rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    public void updateTeacher(int docenteId, String nombre, String apellido, String dni, String direccion, String telefono, String email, String especialidad) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Nombre, apellido y email son obligatorios.");
        }

        try {
            Base.openTransaction();
            Base.exec(
                "UPDATE users SET nombre = ?, apellido = ?, dni = ?, " +
                "                 direccion = ?, telefono = ? WHERE id = ?",
                nombre.trim(), apellido.trim(), dni != null ? dni.trim() : "",
                direccion, telefono, docenteId
            );
            Base.exec(
                "UPDATE teacher SET email = ?, especialidad = ? WHERE usuario_id = ?",
                email.trim(), especialidad, docenteId
            );
            Base.commitTransaction();
        } catch (Exception e) {
            Base.rollbackTransaction();
            throw new RuntimeException("Error al guardar los datos del docente.");
        }
    }

    public void deleteTeacher(int docenteId, int currentUserId) {
        if (docenteId == currentUserId) {
            throw new SecurityException("No podés eliminar tu propio usuario.");
        }
        
        List<Map> check = Base.findAll("SELECT id FROM users WHERE id = ? AND nivel_acceso = 'DOCENTE'", docenteId);
        if (check.isEmpty()) {
            throw new IllegalArgumentException("Docente no encontrado.");
        }
        
        Base.exec("DELETE FROM users WHERE id = ?", docenteId);
    }

    public void createMesaExamen(int teacherId, int materiaId, String fecha) {
        if (fecha == null || fecha.isBlank()) {
            throw new IllegalArgumentException("La fecha de la mesa es obligatoria.");
        }

        // Verificar acceso del docente a la materia
        DocenteMateria asignacion = DocenteMateria.findFirst("teacher_id = ? AND materia_id = ?", teacherId, materiaId);
        if (asignacion == null) {
            throw new SecurityException("No tenés acceso a esa materia.");
        }

        // Crear registro
        com.is1.proyecto.models.MesaExamen mesa = new com.is1.proyecto.models.MesaExamen();
        mesa.set("materia_codigo", materiaId);
        mesa.set("fecha", fecha);
        mesa.saveIt();
    }

    public Map<String, Object> getMateriaPanelData(int teacherId, int materiaId) throws Exception {
        DocenteMateria asignacion = DocenteMateria.findFirst("teacher_id = ? AND materia_id = ?", teacherId, materiaId);
        if (asignacion == null) {
            throw new SecurityException("No tenés acceso a esa materia.");
        }

        Materia materia = Materia.findFirst("codigo = ?", materiaId);
        if (materia == null) {
            throw new IllegalArgumentException("Materia no encontrada.");
        }

        MateriaPeriodo periodo = MateriaPeriodo.findFirst("materia_codigo = ?", materiaId);
        String periodoLabel = "";
        if (periodo != null) {
            String raw = periodo.getString("tipo_cuatrimestre");
            if ("PRIMER_CUATRIMESTRE".equals(raw)) periodoLabel = "I Cuatrimestre";
            else if ("SEGUNDO_CUATRIMESTRE".equals(raw)) periodoLabel = "II Cuatrimestre";
            else if ("ANUAL".equals(raw)) periodoLabel = "Anual";
            else if ("VERANO".equals(raw)) periodoLabel = "Verano";
        }

        List<Map> inscriptosRows = Base.findAll(
            "SELECT u.id as usuario_id, u.nombre, u.apellido, s.legajo " +
            "FROM users u " +
            "JOIN student s ON u.id = s.usuario_id " +
            "JOIN Estado_Academico ea ON s.usuario_id = ea.usuario_id " +
            "WHERE ea.materia_codigo = ? AND ea.estado IN ('INSCRIPTO', 'REGULAR')",
            materiaId
        );
        List<Map<String, Object>> alumnosOptions = new ArrayList<>();
        for (Map row : inscriptosRows) {
            String label = row.get("apellido") + ", " + row.get("nombre") + " — " + row.get("legajo");
            Map<String, Object> opt = new HashMap<>();
            opt.put("id", row.get("usuario_id"));
            opt.put("label", label);
            alumnosOptions.add(opt);
        }

        // Anuncios del período con contador de inscriptos a parciales.
        // (Antes esto se armaba dos veces con la misma query en el controlador — se
        // dejó una sola pasada acá.)
        List<Map<String, Object>> anuncios = new ArrayList<>();
        if (periodo != null) {
            List<Map> anunciosDB = Base.findAll(
                "SELECT id, tipo, titulo, contenido, fecha_examen FROM Anuncio WHERE materia_periodo_id = ? ORDER BY fecha_creacion DESC",
                periodo.getId()
            );
            for (Map a : anunciosDB) {
                Map<String, Object> anuncioMap = new HashMap<>();
                anuncioMap.put("id", a.get("id"));
                anuncioMap.put("tipo", a.get("tipo"));
                anuncioMap.put("titulo", a.get("titulo"));
                anuncioMap.put("contenido", a.get("contenido"));
                anuncioMap.put("fechaExamen", a.get("fecha_examen"));
                anuncioMap.put("esExamen", "EXAMEN".equals(a.get("tipo")));
                if ("EXAMEN".equals(a.get("tipo"))) {
                    List<Map> conteoRows = Base.findAll(
                        "SELECT COUNT(*) AS total FROM Inscripcion_Parcial WHERE anuncio_id = ?",
                        ((Number) a.get("id")).intValue()
                    );
                    int total = conteoRows.isEmpty() ? 0 : ((Number) conteoRows.get(0).get("total")).intValue();
                    anuncioMap.put("inscriptosCount", total);
                }
                anuncios.add(anuncioMap);
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("codigoMateria", materiaId);
        resultado.put("nombreMateria", materia.getString("nombre"));
        resultado.put("anioMateria", materia.getInteger("anio_cursada"));
        resultado.put("periodoMateria", periodoLabel);
        resultado.put("alumnos", alumnosOptions);
        resultado.put("hayAlumnos", !alumnosOptions.isEmpty());
        resultado.put("anuncios", anuncios);
        return resultado;
    }
}
