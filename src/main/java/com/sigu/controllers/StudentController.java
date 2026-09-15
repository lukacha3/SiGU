package com.sigu.controllers;

import static spark.Spark.*;
import com.sigu.config.AccessControl;
import com.sigu.services.StudentService;
import org.javalite.activejdbc.Base;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentController {

    private static final StudentService studentService = new StudentService();

    public static void register() {
        AccessControl.requireRole("/estudiante/new", "ADMIN", "SECRETARIA");
        AccessControl.requireRole("/estudiante/edit/*", "ADMIN", "SECRETARIA");
        AccessControl.requireRoleForPrefix("/estudiante/*", "ESTUDIANTE",
            "/estudiante/new", "/estudiante/edit/");

        get("/estudiante/new", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String successMessage = req.queryParams("message");
            String errorMessage = req.queryParams("error");
            if (successMessage != null) model.put("successMessage", successMessage);
            if (errorMessage != null) model.put("errorMessage", errorMessage);

            List<Map> planesVigentes = Base.findAll(
                "SELECT p.id AS id, CONCAT(c.nombre, ' (Plan Resol: ', p.anio_resolucion, ')') AS descripcion " +
                "FROM Plan_Estudio p JOIN Carrera c ON p.carrera_id = c.id WHERE p.estado = 'VIGENTE' ORDER BY c.nombre ASC"
            );
            model.put("planes", planesVigentes);

            com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "student_form.mustache");
        }, new MustacheTemplateEngine());

        post("/estudiante/new", (req, res) -> {
            String name = req.queryParams("name");
            String lastName = req.queryParams("lastname");
            String dni = req.queryParams("dni");
            String email = req.queryParams("email");
            String legajo = req.queryParams("legajo");
            String tipoEstudiante = req.queryParams("tipo_estudiante");
            String planEstudioId = req.queryParams("plan_estudio_id");

            if (name == null || lastName == null || dni == null || email == null || legajo == null || tipoEstudiante == null || planEstudioId == null || planEstudioId.isBlank()) {
                res.redirect("/estudiante/new?error=" + URLEncoder.encode("Todos los campos (incluyendo legajo) son obligatorios.", StandardCharsets.UTF_8.toString()));
                return "";
            }

            try {
                studentService.registrarEstudiante(name, lastName, dni, email, legajo, tipoEstudiante, Integer.parseInt(planEstudioId));
                res.redirect("/estudiante/new?message=" + URLEncoder.encode("Estudiante registrado exitosamente con clave 1234.", StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                res.redirect("/estudiante/new?error=" + URLEncoder.encode("Error al registrar: " + e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return "";
        });

        get("/estudiante/materias", (req, res) -> {
            Object userIdObj = req.session().attribute("userId");
            if (userIdObj == null) {
                res.redirect("/login");
                return null;
            }

            int userId = ((Number) userIdObj).intValue();
            Map<String, Object> model = new HashMap<>();

            Map<String, Object> infoPlan = studentService.getInformacionPlan(userId);
            if (infoPlan == null) {
                model.put("errorMessage", "No se encontraron datos de tu cuenta de estudiante.");
                com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "historia_academica.mustache");
            }

            model.put("nombrePlan", infoPlan.get("nombrePlan"));
            List<Map<String, Object>> materias = studentService.getHistoriaAcademica(userId, (Integer) infoPlan.get("planEstudioId"));
            model.put("materias", materias);

            com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "historia_academica.mustache");
        }, new MustacheTemplateEngine());

        get("/estudiante/inscripcion/cursada", (req, res) -> {
            String currentUsername = req.session().attribute("currentUserUsername");
            if (currentUsername == null) {
                res.redirect("/");
                return null;
            }

            Map<String, Integer> ids = studentService.getAlumnoIds(currentUsername);
            if (ids == null) {
                res.redirect("/dashboard?error=No+se+encontro+el+estudiante");
                return null;
            }

            List<Map<String, Object>> materiasDisponibles = studentService.getMateriasDisponiblesParaInscripcion(ids.get("alumnoId"), ids.get("planId"));

            Map<String, Object> viewData = new HashMap<>();
            viewData.put("materias", materiasDisponibles);

            String successMessage = req.queryParams("message");
            String errorMessage = req.queryParams("error");
            if (successMessage != null) viewData.put("successMessage", successMessage);
            if (errorMessage != null) viewData.put("errorMessage", errorMessage);

            if (req.session().attribute("loggedIn") != null && req.session().attribute("loggedIn").equals(true)) {
    if (req.session().attribute("fotoPerfil") != null) {
        viewData.put("foto_perfil", req.session().attribute("fotoPerfil"));
    } else {
        viewData.put("foto_perfil", "/img/default-avatar.png");
    }
    if (!viewData.containsKey("username") && req.session().attribute("currentUserUsername") != null) {
        viewData.put("username", req.session().attribute("currentUserUsername"));
    }
}
return new ModelAndView(viewData, "inscripcion_materias.mustache");
        }, new MustacheTemplateEngine());

        post("/estudiante/inscripcion/cursada", (req, res) -> {
            String currentUsername = req.session().attribute("currentUserUsername");
            if (currentUsername == null) {
                res.status(403);
                return "No autorizado";
            }

            Map<String, Integer> ids = studentService.getAlumnoIds(currentUsername);
            String materiaCodigoStr = req.queryParams("materia_codigo");
            if (materiaCodigoStr == null || materiaCodigoStr.isEmpty() || ids == null) {
                res.redirect("/estudiante/inscripcion/cursada?error=Materia+no+especificada");
                return null;
            }

            try {
                studentService.inscribirACursada(ids.get("alumnoId"), ids.get("planId"), Integer.parseInt(materiaCodigoStr));
                res.redirect("/estudiante/inscripcion/cursada?message=Inscripcion+exitosa");
            } catch (Exception e) {
                res.redirect("/estudiante/inscripcion/cursada?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return null;
        });

        post("/estudiante/inscripcion-parcial", (req, res) -> {
            String currentUsername = req.session().attribute("currentUserUsername");
            if (currentUsername == null) {
                res.status(403);
                return "No autorizado";
            }

            Map<String, Integer> ids = studentService.getAlumnoIds(currentUsername);
            String anuncioIdStr = req.queryParams("anuncio_id");

            if (anuncioIdStr == null || anuncioIdStr.isEmpty() || ids == null) {
                res.redirect("/estudiante/aula-virtual?error=" + URLEncoder.encode("Parcial no especificado.", StandardCharsets.UTF_8.toString()));
                return null;
            }

            try {
                studentService.confirmarAsistenciaParcial(ids.get("alumnoId"), Integer.parseInt(anuncioIdStr));
                res.redirect("/estudiante/aula-virtual?success=" + URLEncoder.encode("Asistencia confirmada al parcial.", StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                res.redirect("/estudiante/aula-virtual?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return null;
        });

        get("/estudiante/edit/:id", (req, res) -> {
            int estudianteId = Integer.parseInt(req.params("id"));
            Map<String, Object> model = studentService.getEstudianteParaEdicion(estudianteId);

            if (model == null) {
                res.redirect("/configuracion?error=" + URLEncoder.encode("Estudiante no encontrado.", StandardCharsets.UTF_8.toString()));
                return null;
            }

            String err = req.queryParams("error");
            if (err != null && !err.isEmpty()) model.put("errorMessage", err);

            com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "estudiante_edit_form.mustache");
        }, new MustacheTemplateEngine());

        post("/estudiante/edit/:id", (req, res) -> {
            int estudianteId = Integer.parseInt(req.params("id"));
            String nombre = req.queryParams("nombre");
            String apellido = req.queryParams("apellido");
            String dni = req.queryParams("dni");
            String direccion = req.queryParams("direccion");
            String telefono = req.queryParams("telefono");
            String tipoEstudiante = req.queryParams("tipo_estudiante");

            if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
                res.redirect("/estudiante/edit/" + estudianteId + "?error=" + URLEncoder.encode("Nombre y apellido son obligatorios.", StandardCharsets.UTF_8.toString()));
                return null;
            }
            List<String> tiposValidos = Arrays.asList("REGULAR", "VOCACIONAL", "INTERCAMBIO");
            if (tipoEstudiante == null || !tiposValidos.contains(tipoEstudiante)) {
                res.redirect("/estudiante/edit/" + estudianteId + "?error=" + URLEncoder.encode("Tipo de estudiante inválido.", StandardCharsets.UTF_8.toString()));
                return null;
            }

            try {
                studentService.actualizarEstudiante(estudianteId, nombre, apellido, dni, direccion, telefono, tipoEstudiante);
                res.redirect("/configuracion?message=" + URLEncoder.encode("Estudiante actualizado correctamente.", StandardCharsets.UTF_8.toString()) + "#estudiantes");
            } catch (Exception e) {
                res.redirect("/estudiante/edit/" + estudianteId + "?error=" + URLEncoder.encode("Error al guardar: " + e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return null;
        });
    }
}
