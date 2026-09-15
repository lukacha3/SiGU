package com.sigu.controllers;

import static spark.Spark.*; // Importa los métodos estáticos principales de Spark (get, post, before, after, etc.).
import com.sigu.config.AccessControl;
import com.fasterxml.jackson.databind.ObjectMapper; // Utilidad para serializar/deserializar objetos Java a/desde JSON.
import com.sigu.config.DBConfigSingleton; // Clase Singleton para la configuración de la base de datos.
import com.sigu.models.Anuncio;
import com.sigu.models.Carrera;
import com.sigu.models.Correlatividad;
import com.sigu.models.Materia;
import com.sigu.models.Nota;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap; // Para crear mapas de datos (modelos para las plantillas).
import java.util.List;
import java.util.Map; // Interfaz Map, utilizada para Map.of() o HashMap.
import org.javalite.activejdbc.Base; // Clase central de ActiveJDBC para gestionar la conexión a la base de datos.
import org.mindrot.jbcrypt.BCrypt; // Utilidad para hashear y verificar contraseñas de forma segura.
import spark.ModelAndView; // Representa un modelo de datos y el nombre de la vista a renderizar.
import spark.template.mustache.MustacheTemplateEngine; // Motor de plantillas Mustache para Spark.
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class ConfiguracionController {
    public static void register() {
        AccessControl.requireRole("/configuracion", "ADMIN", "SECRETARIA");
        AccessControl.requireRole("/configuracion/sesiones/*", "ADMIN", "SECRETARIA");
        AccessControl.requireRole("/estudiante/delete/*", "ADMIN", "SECRETARIA");
        AccessControl.requireRole("/materia/edit/*", "ADMIN", "SECRETARIA");
        AccessControl.requireRole("/materia/delete/*", "ADMIN", "SECRETARIA");

        post("/configuracion/sesiones/:usuarioId/cerrar", (req, res) -> {
                int usuarioId = Integer.parseInt(req.params("usuarioId"));
                com.sigu.models.Sesion.delete("usuario_id = ?", usuarioId);

                res.redirect("/configuracion?message=" + URLEncoder.encode(
                    "Sesión cerrada correctamente.", StandardCharsets.UTF_8.toString()) + "#sesiones");
                return null;
            });
        get("/configuracion", (req, res) -> {
                // Docentes
                List<Map> docentesDB = Base.findAll(
                    "SELECT u.id, u.nombre, u.apellido, u.dni, " +
                    "       t.legajo_docente, t.email, t.especialidad " +
                    "FROM users u " +
                    "JOIN teacher t ON t.usuario_id = u.id " +
                    "ORDER BY u.apellido ASC, u.nombre ASC"
                );
                List<Map<String, Object>> docentesList = new ArrayList<>();
                for (Map d : docentesDB) {
                    Map<String, Object> dm = new HashMap<>();
                    dm.put("id",             ((Number) d.get("id")).intValue());
                    dm.put("nombre",         d.get("nombre") + " " + d.get("apellido"));
                    dm.put("dni",            d.get("dni"));
                    dm.put("legajo",         d.get("legajo_docente"));
                    dm.put("email",          d.get("email"));
                    dm.put("especialidad",   d.get("especialidad") != null ? d.get("especialidad") : "—");
                    docentesList.add(dm);
                }
    
                // Estudiantes
                List<Map> estudiantesDB = Base.findAll(
                    "SELECT u.id, u.nombre, u.apellido, u.dni, " +
                    "       s.legajo, s.tipo_estudiante, c.nombre AS carrera " +
                    "FROM users u " +
                    "JOIN student s      ON s.usuario_id  = u.id " +
                    "JOIN Plan_Estudio pe ON pe.id         = s.plan_estudio_id " +
                    "JOIN Carrera c       ON c.id          = pe.carrera_id " +
                    "ORDER BY u.apellido ASC, u.nombre ASC"
                );
                List<Map<String, Object>> estudiantesList = new ArrayList<>();
                for (Map e : estudiantesDB) {
                    Map<String, Object> em = new HashMap<>();
                    em.put("id",      ((Number) e.get("id")).intValue());
                    em.put("nombre",  e.get("nombre") + " " + e.get("apellido"));
                    em.put("dni",     e.get("dni"));
                    em.put("legajo",  e.get("legajo"));
                    em.put("tipo",    e.get("tipo_estudiante"));
                    em.put("carrera", e.get("carrera"));
                    estudiantesList.add(em);
                }
    
                // Materias
                List<Map> materiasDB = Base.findAll(
                    "SELECT m.codigo, m.nombre, m.anio_cursada, m.carga_horaria_total, " +
                    "       c.nombre AS carrera " +
                    "FROM Materia m " +
                    "JOIN Plan_Estudio pe ON pe.id = m.plan_estudio_id " +
                    "JOIN Carrera c       ON c.id  = pe.carrera_id " +
                    "ORDER BY c.nombre ASC, m.anio_cursada ASC, m.nombre ASC"
                );
                List<Map<String, Object>> materiasList = new ArrayList<>();
                for (Map m : materiasDB) {
                    Map<String, Object> mm = new HashMap<>();
                    mm.put("codigo",  ((Number) m.get("codigo")).intValue());
                    mm.put("nombre",  m.get("nombre"));
                    mm.put("anio",    m.get("anio_cursada"));
                    mm.put("carga",   m.get("carga_horaria_total") != null ? m.get("carga_horaria_total") : "—");
                    mm.put("carrera", m.get("carrera"));
                    materiasList.add(mm);
                }
    
                Map<String, Object> model = new HashMap<>();
                model.put("docentes",    docentesList);
                model.put("estudiantes", estudiantesList);
                model.put("materias",    materiasList);
    
                String success = req.queryParams("message");
                String error   = req.queryParams("error");
                if (success != null && !success.isEmpty()) model.put("successMessage", success);
                if (error   != null && !error.isEmpty())   model.put("errorMessage",   error);
    
                com.sigu.config.SessionHelper.populateUserContext(req, model);
                // Sesiones
                List<Map> sesionesDB = Base.findAll(
                    "SELECT s.usuario_id, u.nombre, u.apellido, u.nombre_usuario, u.nivel_acceso, " +
                    "       s.fecha_inicio, s.fecha_expiracion " +
                    "FROM sesion s JOIN users u ON u.id = s.usuario_id " +
                    "ORDER BY s.fecha_inicio DESC"
                );
                List<Map<String, Object>> sesionesList = new ArrayList<>();
                for (Map f : sesionesDB) {
                    Map<String, Object> sm = new HashMap<>();
                    sm.put("usuarioId",     ((Number) f.get("usuario_id")).intValue());
                    sm.put("nombre",        f.get("nombre") + " " + f.get("apellido"));
                    sm.put("username",      f.get("nombre_usuario"));
                    sm.put("rol",           f.get("nivel_acceso"));
                    sm.put("fechaInicio",   f.get("fecha_inicio"));
                    sm.put("fechaExpira",   f.get("fecha_expiracion"));
                    sesionesList.add(sm);
                }
                model.put("sesiones", sesionesList);

return new ModelAndView(model, "configuracion.mustache");
            }, new MustacheTemplateEngine());

        post("/estudiante/delete/:id", (req, res) -> {
                int estudianteId = Integer.parseInt(req.params("id"));
                Object currentUserId = req.session().attribute("userId");
                int    myId = ((Number) currentUserId).intValue();
    
                if (estudianteId == myId) {
                    res.redirect("/configuracion?error=" + URLEncoder.encode(
                        "No podés eliminar tu propio usuario.", StandardCharsets.UTF_8.toString()) + "#estudiantes");
                    return null;
                }
    
                List<Map> check = Base.findAll(
                    "SELECT id FROM users WHERE id = ? AND nivel_acceso = 'ESTUDIANTE'", estudianteId
                );
                if (check.isEmpty()) {
                    res.redirect("/configuracion?error=" + URLEncoder.encode(
                        "Estudiante no encontrado.", StandardCharsets.UTF_8.toString()) + "#estudiantes");
                    return null;
                }
    
                try {
                    // ON DELETE CASCADE cubre: student, Estado_Academico, inscripciones_examen,
                    // Nota, Inscripcion_Parcial, sesion → limpieza completa
                    Base.exec("DELETE FROM users WHERE id = ?", estudianteId);
                    res.redirect("/configuracion?message=" + URLEncoder.encode(
                        "Estudiante eliminado correctamente.", StandardCharsets.UTF_8.toString()) + "#estudiantes");
                } catch (Exception e) {
                    e.printStackTrace();
                    res.redirect("/configuracion?error=" + URLEncoder.encode(
                        "Error al eliminar: " + e.getMessage(), StandardCharsets.UTF_8.toString()) + "#estudiantes");
                }
                return null;
            });

        get("/materia/edit/:codigo", (req, res) -> {
                int codigo = Integer.parseInt(req.params("codigo"));
                List<Map> rows = Base.findAll(
                    "SELECT m.codigo, m.nombre, m.anio_cursada, m.carga_horaria_total, " +
                    "       c.nombre AS carrera " +
                    "FROM Materia m " +
                    "JOIN Plan_Estudio pe ON pe.id = m.plan_estudio_id " +
                    "JOIN Carrera c       ON c.id  = pe.carrera_id " +
                    "WHERE m.codigo = ?",
                    codigo
                );
                if (rows.isEmpty()) {
                    res.redirect("/configuracion?error=" + URLEncoder.encode(
                        "Materia no encontrada.", StandardCharsets.UTF_8.toString()));
                    return null;
                }
                Map m = rows.get(0);
                Map<String, Object> model = new HashMap<>();
                model.put("codigo",               ((Number) m.get("codigo")).intValue());
                model.put("nombre",               m.get("nombre"));
                model.put("anio_cursada",         m.get("anio_cursada"));
                model.put("carga_horaria_total",  m.get("carga_horaria_total") != null ? m.get("carga_horaria_total") : "");
                model.put("carrera",              m.get("carrera"));
    
                String err = req.queryParams("error");
                if (err != null && !err.isEmpty()) model.put("errorMessage", err);
    
                com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "materia_edit_form.mustache");
            }, new MustacheTemplateEngine());

        post("/materia/edit/:codigo", (req, res) -> {
                int codigo          = Integer.parseInt(req.params("codigo"));
                String nombre       = req.queryParams("nombre");
                String anioStr      = req.queryParams("anio_cursada");
                String cargaStr     = req.queryParams("carga_horaria_total");
    
                if (nombre == null || nombre.isBlank() || anioStr == null || anioStr.isBlank()) {
                    res.redirect("/materia/edit/" + codigo + "?error=" + URLEncoder.encode(
                        "Nombre y año de cursada son obligatorios.", StandardCharsets.UTF_8.toString()));
                    return null;
                }
    
                try {
                    int anio = Integer.parseInt(anioStr.trim());
                    Integer carga = (cargaStr != null && !cargaStr.isBlank())
                        ? Integer.parseInt(cargaStr.trim()) : null;
    
                    Base.exec(
                        "UPDATE Materia SET nombre = ?, anio_cursada = ?, carga_horaria_total = ? " +
                        "WHERE codigo = ?",
                        nombre.trim(), anio, carga, codigo
                    );
                    res.redirect("/configuracion?message=" + URLEncoder.encode(
                        "Materia actualizada correctamente.", StandardCharsets.UTF_8.toString()) + "#materias");
                } catch (NumberFormatException e) {
                    res.redirect("/materia/edit/" + codigo + "?error=" + URLEncoder.encode(
                        "Año y carga horaria deben ser números.", StandardCharsets.UTF_8.toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                    res.redirect("/materia/edit/" + codigo + "?error=" + URLEncoder.encode(
                        "Error al guardar: " + e.getMessage(), StandardCharsets.UTF_8.toString()));
                }
                return null;
            });

        post("/materia/delete/:codigo", (req, res) -> {
                int codigo = Integer.parseInt(req.params("codigo"));
    
                List<Map> check = Base.findAll("SELECT codigo FROM Materia WHERE codigo = ?", codigo);
                if (check.isEmpty()) {
                    res.redirect("/configuracion?error=" + URLEncoder.encode(
                        "Materia no encontrada.", StandardCharsets.UTF_8.toString()) + "#materias");
                    return null;
                }
    
                try {
                    // ON DELETE CASCADE cubre: Correlatividad, Materia_Periodo, Docente_Materia,
                    // Estado_Academico, inscripciones_examen (via mesas_examen), mesas_examen,
                    // Anuncio, Nota, Aula_Asignacion, SolicitudAula → limpieza completa
                    Base.exec("DELETE FROM Materia WHERE codigo = ?", codigo);
                    res.redirect("/configuracion?message=" + URLEncoder.encode(
                        "Materia eliminada correctamente.", StandardCharsets.UTF_8.toString()) + "#materias");
                } catch (Exception e) {
                    e.printStackTrace();
                    res.redirect("/configuracion?error=" + URLEncoder.encode(
                        "Error al eliminar: " + e.getMessage(), StandardCharsets.UTF_8.toString()) + "#materias");
                }
                return null;
            });

    }
}
