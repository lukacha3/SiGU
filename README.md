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
   mvn process-classes exec:java
   ```

   Las variables de `.env` se cargan automáticamente al iniciar — no hace falta exportarlas a mano.

   > **Nota:** La tarea de VSCode incluida (`SiGU: Run Server`) también funciona directo por este mismo motivo.
