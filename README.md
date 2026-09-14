# SiGU - Sistema de Gestión Universitaria

## Instrucciones de Instalación y Ejecución

Antes de ejecutar el proyecto, debes configurar las credenciales de la base de datos:

1. Copia el archivo de ejemplo para las variables de entorno:
   ```bash
   cp .env.example .env
   ```
2. Edita el archivo `.env` recién creado y completa `DB_USER` y `DB_PASS` con credenciales válidas para MySQL.
3. Levantá el servidor:

   ```bash
   ./run.sh
   ```

   Esto exporta las variables de `.env` y ejecuta `mvn process-classes exec:java` por vos.

   <details>
   <summary>Alternativa manual (sin el script, o en Fish/Windows)</summary>

   **Bash/Zsh:**
   ```bash
   export $(grep -v '^#' .env | xargs) && mvn process-classes exec:java
   ```

   **Fish:**
   ```fish
   env (grep -v '^#' .env | xargs) mvn process-classes exec:java
   ```
   </details>

   > **Nota:** La tarea de VSCode incluida (`SiGU: Run Server`) se encargará de esto si las variables están configuradas a nivel sistema o si usas una extensión para cargar `.env`.
