package com.is1.proyecto.config;

import static spark.Spark.before;
import static spark.Spark.halt;
import java.util.Arrays;

/**
 * Punto único para declarar qué rol(es) puede acceder a una ruta.
 * Se llama desde el register() de cada controlador, junto a las rutas que protege,
 * para que quede visible en el mismo archivo qué rutas tienen o no protección.
 */
public final class AccessControl {

    private AccessControl() {}

    /** Restringe una ruta exacta (o con wildcard "*") a los roles dados. */
    public static void requireRole(String path, String... allowedRoles) {
        before(path, (req, res) -> {
            String userRole = req.session().attribute("userRole");
            boolean permitido = userRole != null && Arrays.asList(allowedRoles).contains(userRole);
            if (!permitido) {
                res.redirect("/dashboard");
                halt();
            }
        });
    }

    /**
     * Restringe todo un prefijo (ej. "/docente/*") a un único rol, con excepciones
     * explícitas por sub-path (ej. rutas de ese mismo prefijo que en realidad son
     * de ADMIN, como "/docente/edit/"). Las excepciones se comparan con
     * startsWith, no con contains, para evitar falsos positivos.
     */
    public static void requireRoleForPrefix(String prefix, String role, String... exceptStartingWith) {
        before(prefix, (req, res) -> {
            String path = req.pathInfo();
            for (String exception : exceptStartingWith) {
                if (path.startsWith(exception)) return; // lo maneja otro filtro más específico
            }
            String userRole = req.session().attribute("userRole");
            if (userRole == null || !userRole.equals(role)) {
                res.redirect("/dashboard");
                halt();
            }
        });
    }
}
