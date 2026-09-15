package com.sigu.controllers;

import static spark.Spark.*; // Importa los métodos estáticos principales de Spark (get, post, before, after, etc.).
import com.sigu.config.AccessControl;
import com.fasterxml.jackson.databind.ObjectMapper; // Utilidad para serializar/deserializar objetos Java a/desde JSON.
import com.sigu.config.DBConfigSingleton; // Clase Singleton para la configuración de la base de datos.
import com.sigu.models.Carrera;
import com.sigu.models.DocenteMateria;
import com.sigu.models.Materia;
import com.sigu.models.Teacher;
import com.sigu.services.TeacherService;
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

public class TeacherController {
    private static final TeacherService teacherService = new TeacherService();
    public static void register() {
        AccessControl.requireRole("/docente/new", "ADMIN", "SECRETARIA");
        AccessControl.requireRole("/docente/asignar-materia", "ADMIN", "SECRETARIA");
        AccessControl.requireRole("/docente/edit/*", "ADMIN", "SECRETARIA");
        AccessControl.requireRole("/docente/delete/*", "ADMIN", "SECRETARIA");
        AccessControl.requireRoleForPrefix("/docente/*", "DOCENTE",
            "/docente/new", "/docente/asignar-materia", "/docente/edit/", "/docente/delete/");

        get(
                    "/docente/new",
                    (req, res) -> {
                        Map<String, Object> model = new HashMap<>();

                        String successMessage = req.queryParams("message");
                        String errorMessage = req.queryParams("error");

                        if (successMessage != null && !successMessage.isEmpty()) {
                            model.put("successMessage", successMessage);
                        }
                        if (errorMessage != null && !errorMessage.isEmpty()) {
                            model.put("errorMessage", errorMessage);
                        }

                        List<Map> carreras = Base.findAll(
                            "SELECT id, nombre FROM Carrera ORDER BY nombre ASC"
                        );
                        model.put("carreras", carreras);

                        com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "teacher_from.mustache");
                    },
                    new MustacheTemplateEngine()
                );

        get(
                    "/docente/asignar-materia",
                    (req, res) -> {
                        String userRole = req.session().attribute("userRole");
                        if (
                            userRole == null ||
                            (!userRole.equals("SECRETARIA") &&
                                !userRole.equals("ADMIN"))
                        ) {
                            String errorMessage = URLEncoder.encode(
                                "Acceso denegado. Solo SECRETARIA puede asignar materias.",
                                StandardCharsets.UTF_8.toString()
                            );
                            res.redirect("/dashboard?error=" + errorMessage);
                            return null;
                        }

                        Map<String, Object> model = new HashMap<>();

                        String successMessage = req.queryParams("message");
                        String errorMessage = req.queryParams("error");
                        if (successMessage != null && !successMessage.isEmpty()) {
                            model.put("successMessage", successMessage);
                        }
                        if (errorMessage != null && !errorMessage.isEmpty()) {
                            model.put("errorMessage", errorMessage);
                        }

                        // Solo enviamos los Planes de Estudio vigentes.
                        List<Map> planesVigentes = Base.findAll(
                            "SELECT p.id AS id, c.id AS carrera_id, " +
                                "CONCAT(c.nombre, ' — Plan ', p.anio_resolucion) AS descripcion " +
                                "FROM Plan_Estudio p " +
                                "JOIN Carrera c ON p.carrera_id = c.id " +
                                "WHERE p.estado = 'VIGENTE' " +
                                "ORDER BY c.nombre ASC, p.anio_resolucion DESC"
                        );

                        // ya no docentes ni materias.
                        model.put("planes", planesVigentes);

                        com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "assign_materia_form.mustache");
                    },
                    new MustacheTemplateEngine()
                );

        post("/docente/new", (req, res) -> {
            String name = req.queryParams("teacher_name");
            String lastName = req.queryParams("teacher_lastname");
            String dni = req.queryParams("teacher_dni");
            String address = req.queryParams("teacher_address");
            String phone = req.queryParams("teacher_phone");
            String legajo = req.queryParams("legajo_docente");
            String cuil = req.queryParams("teacher_cuil");
            String email = req.queryParams("teacher_email");
            String especialidad = req.queryParams("especialidad");
            String carreraId = req.queryParams("carrera_id");

            try {
                teacherService.createTeacher(name, lastName, dni, address, phone, legajo, cuil, email, especialidad, carreraId);
                
                String mensajeExito = "Docente " + name + " registrado con éxito.";
                res.redirect("/docente/new?message=" + URLEncoder.encode(mensajeExito, StandardCharsets.UTF_8.toString()));
            } catch (IllegalArgumentException e) {
                res.redirect("/docente/new?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/docente/new?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return "";
        });

        post("/docente/asignar-materia", (req, res) -> {
            String teacherIdParam = req.queryParams("teacher_id");
            String materiaIdParam = req.queryParams("materia_id");

            if (teacherIdParam == null || teacherIdParam.isEmpty() || materiaIdParam == null || materiaIdParam.isEmpty()) {
                res.redirect("/docente/asignar-materia?error=" + URLEncoder.encode("Debes seleccionar un docente y una materia.", StandardCharsets.UTF_8.toString()));
                return "";
            }

            try {
                int teacherId = Integer.parseInt(teacherIdParam);
                int materiaId = Integer.parseInt(materiaIdParam);

                teacherService.assignMateria(teacherId, materiaId);

                res.redirect("/docente/asignar-materia?message=" + URLEncoder.encode("Materia asignada correctamente al docente.", StandardCharsets.UTF_8.toString()));
            } catch (NumberFormatException e) {
                res.redirect("/docente/asignar-materia?error=" + URLEncoder.encode("Los identificadores de docente y materia deben ser numéricos.", StandardCharsets.UTF_8.toString()));
            } catch (IllegalArgumentException | IllegalStateException e) {
                res.redirect("/docente/asignar-materia?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/docente/asignar-materia?error=" + URLEncoder.encode("Error interno al asignar la materia. Intente de nuevo.", StandardCharsets.UTF_8.toString()));
            }
            return "";
        });

        get(
                    "/docente/materias",
                    (req, res) -> {
                        String userRole = req.session().attribute("userRole");
                        if (userRole == null || !userRole.equals("DOCENTE")) {
                            res.redirect(
                                "/dashboard?error=" +
                                    URLEncoder.encode(
                                        "Acceso denegado. Solo docentes pueden acceder a esta sección.",
                                        StandardCharsets.UTF_8.toString()
                                    )
                            );
                            return null;
                        }

                        Map<String, Object> model = new HashMap<>();

                        String successMessage = req.queryParams("message");
                        String errorMessage = req.queryParams("error");
                        if (
                            successMessage != null && !successMessage.isEmpty()
                        ) model.put("successMessage", successMessage);
                        if (errorMessage != null && !errorMessage.isEmpty()) model.put(
                            "errorMessage",
                            errorMessage
                        );

                        // Obtenemos el Teacher a partir del userId guardado en sesión
                        Integer userId = req.session().attribute("userId");
                        Teacher teacher = Teacher.findFirst("usuario_id = ?", userId);

                        if (teacher == null) {
                            res.redirect(
                                "/dashboard?error=" +
                                    URLEncoder.encode(
                                        "No se encontró un perfil docente para tu usuario.",
                                        StandardCharsets.UTF_8.toString()
                                    )
                            );
                            return null;
                        }

                        int teacherId = teacher.getInteger("usuario_id");

                        // Buscamos las materias asignadas vía Docente_Materia
                        List<Map> materiasRaw = teacherService.getTeacherMaterias(teacherId);

                        model.put("materias", materiasRaw);
                        model.put("sinMaterias", materiasRaw.isEmpty());

                        com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "docente_materias.mustache");
                    },
                    new MustacheTemplateEngine()
                );

        get(
                    "/docente/materia/:materiaId",
                    (req, res) -> {
                        String userRole = req.session().attribute("userRole");
                        if (userRole == null || !userRole.equals("DOCENTE")) {
                            res.redirect(
                                "/dashboard?error=" +
                                    URLEncoder.encode(
                                        "Acceso denegado.",
                                        StandardCharsets.UTF_8.toString()
                                    )
                            );
                            return null;
                        }

                        Integer userId = req.session().attribute("userId");
                        Teacher teacher = Teacher.findFirst("usuario_id = ?", userId);
                        if (teacher == null) {
                            res.redirect("/dashboard");
                            return null;
                        }

                        int teacherId = teacher.getInteger("usuario_id");
                        int materiaId;
                        try {
                            materiaId = Integer.parseInt(req.params("materiaId"));
                        } catch (NumberFormatException e) {
                            res.redirect(
                                "/docente/materias?error=" +
                                    URLEncoder.encode(
                                        "Materia inválida.",
                                        StandardCharsets.UTF_8.toString()
                                    )
                            );
                            return null;
                        }

                        // Verificamos que la materia le pertenezca al docente
                        DocenteMateria asignacion = DocenteMateria.findFirst(
                            "teacher_id = ? AND materia_id = ?",
                            teacherId,
                            materiaId
                        );
                        if (asignacion == null) {
                            res.redirect(
                                "/docente/materias?error=" +
                                    URLEncoder.encode(
                                        "No tenés acceso a esa materia.",
                                        StandardCharsets.UTF_8.toString()
                                    )
                            );
                            return null;
                        }

                        Materia materia = Materia.findFirst("codigo = ?", materiaId);
                        if (materia == null) {
                            res.redirect("/docente/materias");
                            return null;
                        }

                        Map<String, Object> model;
                        try {
                            model = teacherService.getMateriaPanelData(teacherId, materiaId);
                        } catch (SecurityException | IllegalArgumentException e) {
                            res.redirect("/docente/materias?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
                            return null;
                        }

                        String successMessage = req.queryParams("message");
                        String errorMessage = req.queryParams("error");
                        if (
                            successMessage != null && !successMessage.isEmpty()
                        ) model.put("successMessage", successMessage);
                        if (errorMessage != null && !errorMessage.isEmpty()) model.put(
                            "errorMessage",
                            errorMessage
                        );

                        com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(
                            model,
                            "docente_panel_materia.mustache"
                        );
                    },
                    new MustacheTemplateEngine()
                );

        post("/docente/materia/:materiaId/aula", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            Teacher teacher = Teacher.findFirst("usuario_id = ?", userId);
            int teacherId = teacher.getInteger("usuario_id");
            int materiaId = Integer.parseInt(req.params("materiaId"));
            String aula = req.queryParams("aula");

            try {
                teacherService.assignAula(teacherId, materiaId, aula);
                res.redirect("/docente/materia/" + materiaId + "?message=" + URLEncoder.encode("Aula asignada correctamente.", StandardCharsets.UTF_8.toString()));
            } catch (SecurityException | IllegalArgumentException | IllegalStateException e) {
                res.redirect("/docente/materia/" + materiaId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/docente/materia/" + materiaId + "?error=" + URLEncoder.encode("Error al asignar el aula: " + e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return "";
        });

        post("/docente/materia/:materiaId/mesa", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            Teacher teacher = Teacher.findFirst("usuario_id = ?", userId);
            int teacherId = teacher.getInteger("usuario_id");
            int materiaId = Integer.parseInt(req.params("materiaId"));
            String fecha = req.queryParams("fecha_examen");

            try {
                teacherService.createMesaExamen(teacherId, materiaId, fecha);
                res.redirect("/docente/materia/" + materiaId + "?message=" + URLEncoder.encode("Mesa de Examen creada correctamente.", StandardCharsets.UTF_8.toString()));
            } catch (SecurityException | IllegalArgumentException e) {
                res.redirect("/docente/materia/" + materiaId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/docente/materia/" + materiaId + "?error=" + URLEncoder.encode("Error al crear la Mesa de Examen: " + e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return "";
        });

        get(
                    "/docente/materia/:materiaId/contenido",
                    (req, res) -> {
                        String userRole = req.session().attribute("userRole");
                        if (userRole == null || !userRole.equals("DOCENTE")) {
                            res.redirect(
                                "/dashboard?error=" +
                                    URLEncoder.encode(
                                        "Acceso denegado.",
                                        StandardCharsets.UTF_8.toString()
                                    )
                            );
                            return null;
                        }
                        return new ModelAndView(
                            new HashMap<>(),
                            "contenido_proximamente.mustache"
                        );
                    },
                    new MustacheTemplateEngine()
                );

        get("/docente/edit/:id", (req, res) -> {
                int docenteId = Integer.parseInt(req.params("id"));
                Map d = teacherService.getTeacherForEdit(docenteId);
                if (d == null) {
                    res.redirect("/configuracion?error=" + URLEncoder.encode(
                        "Docente no encontrado.", StandardCharsets.UTF_8.toString()));
                    return null;
                }
                Map<String, Object> model = new HashMap<>();
                model.put("id",             ((Number) d.get("id")).intValue());
                model.put("nombre",         d.get("nombre"));
                model.put("apellido",       d.get("apellido"));
                model.put("dni",            d.get("dni"));
                model.put("direccion",      d.get("direccion") != null ? d.get("direccion") : "");
                model.put("telefono",       d.get("telefono")  != null ? d.get("telefono")  : "");
                model.put("nombre_usuario", d.get("nombre_usuario"));
                model.put("legajo_docente", d.get("legajo_docente"));
                model.put("cuil",           d.get("cuil"));
                model.put("email",          d.get("email"));
                model.put("especialidad",   d.get("especialidad") != null ? d.get("especialidad") : "");
    
                String err = req.queryParams("error");
                if (err != null && !err.isEmpty()) model.put("errorMessage", err);
    
                com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "docente_edit_form.mustache");
            }, new MustacheTemplateEngine());

        post("/docente/edit/:id", (req, res) -> {
            int docenteId = Integer.parseInt(req.params("id"));
            String nombre      = req.queryParams("nombre");
            String apellido    = req.queryParams("apellido");
            String dni         = req.queryParams("dni");
            String direccion   = req.queryParams("direccion");
            String telefono    = req.queryParams("telefono");
            String email       = req.queryParams("email");
            String especialidad = req.queryParams("especialidad");

            try {
                teacherService.updateTeacher(docenteId, nombre, apellido, dni, direccion, telefono, email, especialidad);
                res.redirect("/configuracion?message=" + URLEncoder.encode("Docente actualizado correctamente.", StandardCharsets.UTF_8.toString()) + "#docentes");
            } catch (IllegalArgumentException e) {
                res.redirect("/docente/edit/" + docenteId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/docente/edit/" + docenteId + "?error=" + URLEncoder.encode("Error al guardar: " + e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return null;
        });

        post("/docente/delete/:id", (req, res) -> {
            int docenteId  = Integer.parseInt(req.params("id"));
            int myId = ((Number) req.session().attribute("userId")).intValue();

            try {
                teacherService.deleteTeacher(docenteId, myId);
                res.redirect("/configuracion?message=" + URLEncoder.encode("Docente eliminado correctamente.", StandardCharsets.UTF_8.toString()) + "#docentes");
            } catch (SecurityException | IllegalArgumentException e) {
                res.redirect("/configuracion?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()) + "#docentes");
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/configuracion?error=" + URLEncoder.encode("Error al eliminar: " + e.getMessage(), StandardCharsets.UTF_8.toString()) + "#docentes");
            }
            return null;
        });

    }
}
