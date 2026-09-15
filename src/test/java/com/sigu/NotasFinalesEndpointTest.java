package com.sigu;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

class NotasFinalesEndpointTest {

    private static final int TEST_PORT = 8099;
    private static HttpClient client;

    @BeforeAll
    static void setup() throws Exception {
        TestDatabaseSupport.resetSchema();

        Base.exec("INSERT INTO users (id, dni, nombre, apellido, nombre_usuario, password, nivel_acceso) VALUES (99, '22222222', 'Docente', 'Test', 'docente.test', ?, 'DOCENTE')",
            BCrypt.hashpw("password123", BCrypt.gensalt()));
        Base.exec("INSERT INTO teacher (usuario_id, legajo_docente, cuil, email) VALUES (99, 'DOC-001', '20-22222222-2', 'docente@test.com')");

        App.init(TEST_PORT);
        spark.Spark.awaitInitialization();

        client = HttpClient.newBuilder()
            .cookieHandler(new CookieManager())
            .build();
    }

    @AfterAll
    static void teardown() {
        spark.Spark.stop();
        spark.Spark.awaitStop();
    }

    @Test
    void notasFinalesEndpoint_respondeSinErrorDeSchema() throws Exception {
        String base = "http://localhost:" + TEST_PORT;

        HttpRequest login = HttpRequest.newBuilder()
            .uri(URI.create(base + "/login"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(
                "username=docente.test&password=password123"))
            .build();
        client.send(login, HttpResponse.BodyHandlers.ofString());

        HttpRequest notasFinales = HttpRequest.newBuilder()
            .uri(URI.create(base + "/docente/notas-finales"))
            .GET()
            .build();
        HttpResponse<String> response = client.send(notasFinales, HttpResponse.BodyHandlers.ofString());

        // Antes del fix de mesas_examen/mesa_examen, esto tiraba 500 por
        // SQLSyntaxErrorException. Si vuelve a pasar (drift de schema), este
        // test lo detecta.
        assertEquals(200, response.statusCode());
        assertFalse(response.body().contains("error"), "La respuesta no debería contener un mensaje de error");
    }
}
