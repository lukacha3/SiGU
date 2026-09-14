package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import com.is1.proyecto.config.DBConfigSingleton;
import java.nio.file.*;

public class TestDatabaseSupport {

    public static void resetSchema() throws Exception {
        DBConfigSingleton.getInstance().closeConnection();
        Path dbFile = Paths.get("./target/test.db");
        Files.deleteIfExists(dbFile);

        DBConfigSingleton.getInstance().openConnection();

        String schemaSql = Files.readString(
            Paths.get("src/main/resources/sqlite_scheme.sql")
        );
        for (String stmt : schemaSql.split(";")) {
            String trimmed = stmt.trim();
            if (!trimmed.isEmpty()) {
                Base.exec(trimmed);
            }
        }
    }
}
