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
}
