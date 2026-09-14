// Archivo: com/is1/proyecto/config/DBConfigSingleton.java
package com.is1.proyecto.config;

import org.javalite.activejdbc.Base;

public final class DBConfigSingleton {

    private static DBConfigSingleton instance;

    private final String dbUrl;
    private final String user;
    private final String pass;
    private final String driver;

    private DBConfigSingleton() {
        String testDbUrl = System.getProperty("db.url");
        if (testDbUrl != null && !testDbUrl.isBlank()) {
            // Perfil de test (ver pom.xml, profile "test"): SQLite aislado,
            // no toca la base de desarrollo/producción real.
            this.dbUrl = testDbUrl;
            this.driver = "org.sqlite.JDBC";
            this.user = "";
            this.pass = "";
            return;
        }

        // Forzamos el uso de MySQL ya que el proyecto fue migrado completamente.
        // Ignoramos System.getenv("DB_URL") porque tu terminal local seguía inyectando
        // una URL de SQLite antigua.
        this.dbUrl = "jdbc:mysql://127.0.0.1:3306/proyecto_is_ii?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        this.driver = "com.mysql.cj.jdbc.Driver";

        String envUser = System.getenv("DB_USER");
        if (envUser == null || envUser.isBlank()) {
            envUser = System.getProperty("DB_USER");
        }
        if (envUser == null || envUser.isBlank()) {
            throw new IllegalStateException(
                "La variable DB_USER no está definida (ni como env var ni en .env). " +
                "Revisá el archivo .env.example."
            );
        }
        this.user = envUser;

        String envPass = System.getenv("DB_PASS");
        if (envPass == null || envPass.isBlank()) {
            envPass = System.getProperty("DB_PASS");
        }
        if (envPass == null || envPass.isBlank()) {
            throw new IllegalStateException(
                "La variable DB_PASS no está definida (ni como env var ni en .env). " +
                "Revisá el archivo .env.example."
            );
        }
        this.pass = envPass;
    }

    public static synchronized DBConfigSingleton getInstance() {
        if (instance == null) {
            instance = new DBConfigSingleton();
        }
        return instance;
    }

    public void openConnection() {
        if (!Base.hasConnection()) {
            Base.open(this.driver, this.dbUrl, this.user, this.pass);
        }
    }

    public void closeConnection() {
        if (Base.hasConnection()) {
            Base.close();
        }
    }

    // Getters
    public String getDbUrl() {
        return dbUrl;
    }

    public String getUser() {
        return user;
    }

    public String getPass() {
        return pass;
    }

    public String getDriver() {
        return driver;
    }
}