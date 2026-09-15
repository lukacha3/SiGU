package com.sigu.config;

public class SessionHelper {
    public static void populateUserContext(spark.Request req, java.util.Map<String, Object> model) {
        Boolean loggedIn = req.session().attribute("loggedIn");
        if (Boolean.TRUE.equals(loggedIn)) {
            Object foto = req.session().attribute("fotoPerfil");
            model.put("foto_perfil", foto != null ? foto : "/img/default-avatar.png");
            if (!model.containsKey("username") && req.session().attribute("currentUserUsername") != null) {
                model.put("username", req.session().attribute("currentUserUsername"));
            }
        }
    }
}
