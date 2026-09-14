package com.is1.proyecto;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica que todo nombre de tabla referenciado en SQL crudo (Base.findAll,
 * Base.exec) dentro de controllers/ y services/ exista realmente en el schema.
 * Este test existe para evitar que se repita el bug de mesas_examen/mesa_examen:
 * un typo o un rename de tabla en el schema que el código no refleja (o viceversa)
 * ahora rompe el build en vez de romperse recién en producción.
 */
class SchemaConsistencyTest {

    // Nombres que aparecen en SQL crudo pero no son tablas (alias, columnas mal
    // capturadas por el regex, palabras reservadas, etc.) — agregar acá si el
    // regex trae algún falso positivo nuevo, no borrar tablas reales de la lista.
    private static final Set<String> IGNORAR = Set.of(
        "DUAL" // ejemplo de placeholder, ajustar según lo que aparezca al correr
    );

    @Test
    void todaTablaReferenciadaEnCodigoJavaExisteEnElSchema() throws Exception {
        Set<String> tablasEnSchema = extraerTablasDelSchema();
        Set<String> tablasEnCodigo = extraerTablasReferenciadasEnCodigo();

        tablasEnCodigo.removeAll(IGNORAR);

        Set<String> faltantes = new HashSet<>();
        for (String tabla : tablasEnCodigo) {
            boolean existe = tablasEnSchema.stream().anyMatch(t -> t.equalsIgnoreCase(tabla));
            if (!existe) faltantes.add(tabla);
        }

        assertTrue(faltantes.isEmpty(),
            "Las siguientes tablas se usan en SQL crudo pero no existen en " +
            "sqlite_scheme.sql: " + faltantes + " — revisá si es un typo, un " +
            "rename sin terminar, o si falta agregarlas al schema.");
    }

    private Set<String> extraerTablasDelSchema() throws Exception {
        String schema = Files.readString(Paths.get("src/main/resources/sqlite_scheme.sql"));
        Set<String> tablas = new HashSet<>();
        Matcher m = Pattern.compile(
            "CREATE TABLE(?: IF NOT EXISTS)?\\s+([A-Za-z_][A-Za-z0-9_]*)",
            Pattern.CASE_INSENSITIVE
        ).matcher(schema);
        while (m.find()) tablas.add(m.group(1));
        return tablas;
    }

    private Set<String> extraerTablasReferenciadasEnCodigo() throws Exception {
        Set<String> tablas = new HashSet<>();
        Pattern patronTabla = Pattern.compile(
            "\\b(?:FROM|JOIN|INTO|UPDATE)\\s+([A-Za-z_][A-Za-z0-9_]*)"
        );
        for (String carpeta : new String[]{
                "src/main/java/com/is1/proyecto/controllers",
                "src/main/java/com/is1/proyecto/services"}) {
            Files.walk(Paths.get(carpeta))
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> {
                    try {
                        String contenido = Files.readString(p);
                        Matcher m = patronTabla.matcher(contenido);
                        while (m.find()) tablas.add(m.group(1));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        }
        return tablas;
    }
}
