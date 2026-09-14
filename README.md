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

## Estilos (Tailwind CSS)

El proyecto usa un build local de Tailwind (no CDN). Si modificás clases en los
templates y necesitás que el CSS las incluya, regenerá el archivo compilado:

1. Descargá el CLI standalone (una vez): ver instrucciones en
   https://tailwindcss.com/blog/standalone-cli
2. Corré: `./tailwindcss -i ./tailwind-src/input.css -o ./public/css/tailwind.css --minify`
3. Commiteá `public/css/tailwind.css` junto con tu cambio.
