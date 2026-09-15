package com.sigu.controllers;

import static spark.Spark.*;
import com.sigu.models.Teacher;
import com.sigu.services.NotaService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import spark.ModelAndView;
import spark.template.mustache.MustacheTemplateEngine;

public class NotaController {
    public static void register() {
        NotaService notaService = new NotaService();

        post("/docente/materia/:materiaId/nota", (req, res) -> {
            String userRole = req.session().attribute("userRole");
            if (userRole == null || !userRole.equals("DOCENTE")) {
                res.redirect("/dashboard?error=" + URLEncoder.encode("Acceso denegado.", StandardCharsets.UTF_8.toString()));
                return "";
            }

            Integer userId = req.session().attribute("userId");
            Teacher teacher = Teacher.findFirst("usuario_id = ?", userId);
            int teacherId = teacher.getInteger("usuario_id");
            int materiaId = Integer.parseInt(req.params("materiaId"));

            String studentIdParam = req.queryParams("student_id");
            String valorParam = req.queryParams("valor");

            if (studentIdParam == null || studentIdParam.isEmpty() || valorParam == null || valorParam.isEmpty()) {
                res.redirect("/docente/materia/" + materiaId + "?error=" + URLEncoder.encode("Debés seleccionar un alumno e ingresar una nota.", StandardCharsets.UTF_8.toString()));
                return "";
            }

            String estadoCursada = req.queryParams("estado_cursada");
            if (estadoCursada == null || estadoCursada.isEmpty()) estadoCursada = "REGULAR";

            try {
                int studentId = Integer.parseInt(studentIdParam);
                double valor = Double.parseDouble(valorParam);

                notaService.registrarNotaCursada(teacherId, materiaId, studentId, valor, estadoCursada);

                res.redirect("/docente/materia/" + materiaId + "?message=" + URLEncoder.encode("Nota y estado de cursada registrados correctamente.", StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/docente/materia/" + materiaId + "?error=" + URLEncoder.encode("Error al registrar la nota: " + e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return "";
        });

        get("/docente/notas-finales", (req, res) -> {
            String role = req.session().attribute("userRole");
            if (role == null || !"DOCENTE".equals(role)) {
                res.redirect("/dashboard");
                return null;
            }
            Integer userId = req.session().attribute("userId");
            Teacher teacher = Teacher.findFirst("usuario_id = ?", userId);
            if (teacher == null) {
                res.redirect("/dashboard");
                return null;
            }
            int teacherId = teacher.getInteger("usuario_id");

            Map<String, Object> model = new HashMap<>();
            model.put("mesas", notaService.getMesasDelDocente(teacherId));
            
            String success = req.queryParams("success");
            String error = req.queryParams("error");
            if (success != null) model.put("successMessage", success);
            if (error != null) model.put("errorMessage", error);

            com.sigu.config.SessionHelper.populateUserContext(req, model);
            return new ModelAndView(model, "carga_finales.mustache");
        }, new MustacheTemplateEngine());

        get("/docente/notas-finales/:mesaId", (req, res) -> {
            String role = req.session().attribute("userRole");
            if (role == null || !"DOCENTE".equals(role)) {
                res.redirect("/dashboard");
                return null;
            }
            Integer userId = req.session().attribute("userId");
            Teacher teacher = Teacher.findFirst("usuario_id = ?", userId);
            if (teacher == null) {
                res.redirect("/dashboard");
                return null;
            }
            int teacherId = teacher.getInteger("usuario_id");
            int mesaId = Integer.parseInt(req.params("mesaId"));

            try {
                Map<String, Object> model = notaService.getMesaConInscriptos(mesaId, teacherId);

                String success = req.queryParams("success");
                String error = req.queryParams("error");
                if (success != null) model.put("successMessage", success);
                if (error != null) model.put("errorMessage", error);

                com.sigu.config.SessionHelper.populateUserContext(req, model);
                return new ModelAndView(model, "carga_finales_acta.mustache");
            } catch (Exception e) {
                res.redirect("/docente/notas-finales?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8.toString()));
                return null;
            }
        }, new MustacheTemplateEngine());

        post("/docente/notas-finales/:mesaId", (req, res) -> {
            String role = req.session().attribute("userRole");
            if (role == null || !"DOCENTE".equals(role)) {
                res.redirect("/dashboard");
                return null;
            }
            Integer userId = req.session().attribute("userId");
            Teacher teacher = Teacher.findFirst("usuario_id = ?", userId);
            if (teacher == null) {
                res.redirect("/dashboard");
                return null;
            }
            int teacherId = teacher.getInteger("usuario_id");
            int mesaId = Integer.parseInt(req.params("mesaId"));

            String studentIdStr = req.queryParams("student_id");
            String valorStr = req.queryParams("valor");

            if (studentIdStr == null || valorStr == null || studentIdStr.isEmpty() || valorStr.isEmpty()) {
                res.redirect("/docente/notas-finales/" + mesaId + "?error=" + URLEncoder.encode("Datos incompletos.", StandardCharsets.UTF_8.toString()));
                return null;
            }

            try {
                int studentId = Integer.parseInt(studentIdStr);
                double valor = Double.parseDouble(valorStr);

                notaService.registrarNotaFinal(teacherId, mesaId, studentId, valor);

                res.redirect("/docente/notas-finales/" + mesaId + "?success=" + URLEncoder.encode("Calificación registrada.", StandardCharsets.UTF_8.toString()));
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/docente/notas-finales/" + mesaId + "?error=" + URLEncoder.encode("Error: " + e.getMessage(), StandardCharsets.UTF_8.toString()));
            }
            return null;
        });
    }
}
