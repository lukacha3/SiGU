package com.sigu.controllers;

import static spark.Spark.*; // Importa los métodos estáticos principales de Spark (get, post, before, after, etc.).
import com.fasterxml.jackson.databind.ObjectMapper; // Utilidad para serializar/deserializar objetos Java a/desde JSON.
import com.sigu.config.DBConfigSingleton; // Clase Singleton para la configuración de la base de datos.
import com.sigu.models.Anuncio;
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

public class AulaVirtualController {
    public static void register() {
        get(
                    "/estudiante/aula-virtual",
                    (req, res) -> {
                        if (
                            req.session().attribute("userRole") == null ||
                            !req.session().attribute("userRole").equals("ESTUDIANTE")
                        ) {
                            res.redirect("/login");
                            return null;
                        }

                        int alumnoId = req.session().attribute("userId");
                        Map<String, Object> model = new HashMap<>();

                        // Buscar materias válidas para el alumno (incluimos PROMOCION por si acaso ya se cargaron así)
                        List<Map> materias = Base.findAll(
                            "SELECT m.codigo, m.nombre, ea.estado " +
                                "FROM Estado_Academico ea " +
                                "JOIN Materia m ON ea.materia_codigo = m.codigo " +
                                "WHERE ea.usuario_id = ? AND ea.estado IN ('INSCRIPTO', 'REGULAR', 'APROBADO', 'PROMOCION')",
                            alumnoId
                        );

                        model.put("materias", materias);
                        com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(
                            model,
                            "aula_virtual_selector.mustache"
                        );
                    },
                    new MustacheTemplateEngine()
                );

        get(
                    "/estudiante/aula-virtual/materia",
                    (req, res) -> {
                        if (
                            req.session().attribute("userRole") == null ||
                            !req.session().attribute("userRole").equals("ESTUDIANTE")
                        ) {
                            res.redirect("/login");
                            return null;
                        }

                        int alumnoId = req.session().attribute("userId");
                        String materiaCodigoStr = req.queryParams("materia_codigo");

                        if (materiaCodigoStr == null || materiaCodigoStr.isBlank()) {
                            res.redirect("/estudiante/aula-virtual");
                            return null;
                        }

                        int materiaCodigo = Integer.parseInt(materiaCodigoStr);

                        // 1. Validar Seguridad: ¿El estudiante está realmente inscripto en esta materia?
                        List<Map> estadoAcademico = Base.findAll(
                            "SELECT ea.estado, m.nombre " +
                                "FROM Estado_Academico ea " +
                                "JOIN Materia m ON ea.materia_codigo = m.codigo " +
                                "WHERE ea.usuario_id = ? AND ea.materia_codigo = ?",
                            alumnoId,
                            materiaCodigo
                        );

                        if (estadoAcademico.isEmpty()) {
                            res.redirect(
                                "/dashboard?error=No+tienes+acceso+a+esta+materia"
                            );
                            return null;
                        }

                        String estadoAlumno = (String) estadoAcademico
                            .get(0)
                            .get("estado");
                        String materiaNombre = (String) estadoAcademico
                            .get(0)
                            .get("nombre");

                        Map<String, Object> model = new HashMap<>();
                        model.put("materia_codigo", materiaCodigo);
                        model.put("materia_nombre", materiaNombre);
                        model.put("estado_alumno", estadoAlumno);

                        // 2. Buscar el ID del periodo activo de la materia para cruzar anuncios y notas
                        List<Map> periodos = Base.findAll(
                            "SELECT id FROM Materia_Periodo WHERE materia_codigo = ? ORDER BY anio_academico DESC LIMIT 1",
                            materiaCodigo
                        );

                        if (!periodos.isEmpty()) {
                            int materiaPeriodoId = (
                                (Number) periodos.get(0).get("id")
                            ).intValue();

                            // A. Buscar Nota
                            List<Map> notas = Base.findAll(
                                "SELECT valor FROM Nota WHERE student_id = ? AND materia_periodo_id = ?",
                                alumnoId,
                                materiaPeriodoId
                            );
                            if (notas.isEmpty()) {
                                model.put("sinNota", true);
                            } else {
                                model.put("sinNota", false);
                                model.put("nota_valor", notas.get(0).get("valor"));
                            }

                            // B. Buscar Docentes y Aulas
                            List<Map> docentesAulas = Base.findAll(
                                "SELECT u.nombre as docente_nombre, u.apellido as docente_apellido, a.aula " +
                                    "FROM Aula_Asignacion a " +
                                    "JOIN users u ON a.teacher_id = u.id " +
                                    "WHERE a.materia_periodo_id = ?",
                                materiaPeriodoId
                            );
                            model.put("docentes_aulas", docentesAulas);

                            // C. Buscar Anuncios
                            List<Map> anuncios = Base.findAll(
                                "SELECT an.titulo, an.contenido, an.tipo, an.fecha_examen, " +
                                    "u.nombre, u.apellido, " +
                                    "an.fecha_creacion " +
                                    "FROM Anuncio an " +
                                    "JOIN users u ON an.teacher_id = u.id " +
                                    "WHERE an.materia_periodo_id = ? " +
                                    "ORDER BY an.fecha_creacion DESC",
                                materiaPeriodoId
                            );

                            List<Map<String, Object>> anunciosProcesados =
                                new java.util.ArrayList<>();
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                            for (Map an : anuncios) {
                                Map<String, Object> anDto = new HashMap<>(an);
                                anDto.put("esExamen", "EXAMEN".equals(an.get("tipo")));
                                anDto.put("autor", an.get("nombre") + " " + an.get("apellido"));
                                
                                Object fechaObj = an.get("fecha_creacion");
                                if (fechaObj != null) {
                                    if (fechaObj instanceof java.util.Date) {
                                        anDto.put("fecha", sdf.format((java.util.Date)fechaObj));
                                    } else {
                                        anDto.put("fecha", fechaObj.toString());
                                    }
                                } else {
                                    anDto.put("fecha", "");
                                }
                                anunciosProcesados.add(anDto);
                            }
                            model.put("anuncios", anunciosProcesados);
                        } else {
                            // Si la materia no tiene periodos configurados
                            model.put("sinNota", true);
                            model.put("docentes_aulas", new java.util.ArrayList<>());
                            model.put("anuncios", new java.util.ArrayList<>());
                        }

                        com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "aula_virtual_tablero.mustache");
                    },
                    new MustacheTemplateEngine()
                );

        get(
                    "/estudiante/aula-virtual",
                    (req, res) -> {
                        String username = req
                            .session()
                            .attribute("currentUserUsername");
                        String role = req.session().attribute("userRole");
                        if (username == null || !"ESTUDIANTE".equals(role)) {
                            res.redirect("/login");
                            return null;
                        }

                        List<Map> studentRows = Base.findAll(
                            "SELECT s.usuario_id FROM student s JOIN users u ON u.id = s.usuario_id WHERE u.nombre_usuario = ?",
                            username
                        );
                        if (studentRows.isEmpty()) {
                            res.redirect("/dashboard");
                            return null;
                        }
                        int alumnoId = (
                            (Number) studentRows.get(0).get("usuario_id")
                        ).intValue();

                        // Materias en las que el alumno está INSCRIPTO (cursando)
                        List<Map> filas = Base.findAll(
                            "SELECT ea.materia_codigo, m.nombre AS materia_nombre, " +
                            "       a.id AS anuncio_id, a.tipo, a.titulo, a.contenido, a.fecha_examen, " +
                            "       ip.id AS inscripcion_parcial_id " +
                            "FROM Estado_Academico ea " +
                            "JOIN Materia m ON m.codigo = ea.materia_codigo " +
                            "LEFT JOIN Materia_Periodo mp ON mp.materia_codigo = ea.materia_codigo " +
                            "LEFT JOIN Anuncio a ON a.materia_periodo_id = mp.id " +
                            "LEFT JOIN Inscripcion_Parcial ip ON ip.anuncio_id = a.id AND ip.usuario_id = ea.usuario_id " +
                            "WHERE ea.usuario_id = ? AND ea.estado = 'INSCRIPTO' " +
                            "ORDER BY m.nombre ASC, a.fecha_creacion DESC",
                            alumnoId
                        );

                        Map<Integer, Map<String, Object>> materiasPorCodigo = new java.util.LinkedHashMap<>();
                        for (Map fila : filas) {
                            int codigo = ((Number) fila.get("materia_codigo")).intValue();

                            Map<String, Object> materiaMap = materiasPorCodigo.computeIfAbsent(codigo, k -> {
                                Map<String, Object> m = new HashMap<>();
                                m.put("codigo", codigo);
                                m.put("nombre", fila.get("materia_nombre"));
                                m.put("anuncios", new ArrayList<Map<String, Object>>());
                                return m;
                            });

                            if (fila.get("anuncio_id") != null) {
                                Map<String, Object> anuncioMap = new HashMap<>();
                                anuncioMap.put("id", fila.get("anuncio_id"));
                                anuncioMap.put("tipo", fila.get("tipo"));
                                anuncioMap.put("titulo", fila.get("titulo"));
                                anuncioMap.put("contenido", fila.get("contenido"));
                                anuncioMap.put("fechaExamen", fila.get("fecha_examen"));
                                boolean esExamen = "EXAMEN".equals(fila.get("tipo"));
                                anuncioMap.put("esExamen", esExamen);
                                if (esExamen) {
                                    anuncioMap.put("yaInscripto", fila.get("inscripcion_parcial_id") != null);
                                }
                                ((List<Map<String, Object>>) materiaMap.get("anuncios")).add(anuncioMap);
                            }
                        }

                        List<Map<String, Object>> materiasConAnuncios = new ArrayList<>(materiasPorCodigo.values());

                        Map<String, Object> model = new HashMap<>();
                        model.put("materias", materiasConAnuncios);
                        String success = req.queryParams("success");
                        String error = req.queryParams("error");
                        if (success != null) model.put("successMessage", success);
                        if (error != null) model.put("errorMessage", error);

                        com.sigu.config.SessionHelper.populateUserContext(req, model);
return new ModelAndView(model, "aula_virtual.mustache");
                    },
                    new MustacheTemplateEngine()
                );

    }
}
