# SiGU — Sistema de Gestión Universitaria

![Tests](https://github.com/lukacha3/SiGU/actions/workflows/tests.yml/badge.svg)

SiGU es una plataforma de gestión académica universitaria con roles diferenciados
para estudiantes, docentes, secretaría académica y administración: inscripción a
materias y exámenes, carga de notas, correlatividades, mesas de examen, anuncios
por materia y gestión de usuarios.

Nació como proyecto de la cátedra de Ingeniería de Software I y II, y después lo
seguí trabajando por mi cuenta: corregí bugs reales, cerré vulnerabilidades de
control de acceso, armé tests, y reorganizé la arquitectura
(controladores → capa de servicios).

## Funcionalidades por rol

**Estudiante**
- Inscripción a cursada (con motor de correlatividades: requisitos de materia
  aprobada/regular)
- Inscripción a mesas de examen final
- Aula virtual: ver anuncios y contenido de las materias en curso
- Edición de perfil y foto

**Docente**
- Panel de materias asignadas, con listado de alumnos inscriptos
- Carga de notas de cursada (con validación de reglas: regular, promoción,
  libre) y de notas finales por mesa de examen
- Creación de mesas de examen y asignación de aula
- Publicación de anuncios y parciales

**Secretaría académica / Administración**
- Alta de estudiantes, docentes y secretarías
- Asignación de materias a docentes
- ABM de carreras, planes de estudio y materias
- Panel de configuración: gestión de usuarios y sesiones activas (con
  invalidación remota)

## Stack técnico

- **Backend:** Java 11, [Spark Java](https://sparkjava.com/) (framework web
  minimalista), [ActiveJDBC](https://javalite.io/activejdbc) (ORM)
- **Base de datos:** MySQL/MariaDB en producción, SQLite aislada para tests
- **Frontend:** Mustache (server-side templates) + Tailwind CSS (build local, sin
  CDN)
- **Auth:** BCrypt para hashing de contraseñas, sesiones con rotación de ID
  (anti session-fixation) e invalidación de sesiones concurrentes
- **Tests:** JUnit 5, corriendo contra SQLite aislada — incluye tests del motor
  de correlatividades, de las reglas de carga de notas, un test de integración
  HTTP end-to-end, y un test de consistencia que valida que todo nombre de tabla
  usado en el código exista en el schema real
- **CI:** GitHub Actions corre la suite completa en cada push/PR

## Arquitectura

```text
src/main/java/com/sigu/
├── controllers/   # Rutas HTTP (Spark), manejo de sesión/request
├── services/       # Lógica de negocio (correlatividades, notas, docentes)
├── models/         # Modelos ActiveJDBC (mapeo a tablas)
└── config/         # Configuración de DB y control de acceso declarativo
```

## Instalación y ejecución local

1. Cloná el repo y copiá el archivo de variables de entorno:
```bash
git clone git@github.com:lukacha3/SiGU.git
cd SiGU
cp .env.example .env
```
2. Completá `DB_USER` y `DB_PASS` en `.env` con credenciales válidas de tu
   MySQL/MariaDB local.
3. Creá la base de datos y cargá el schema (ejemplo con el cliente `mysql`):
```bash
mysql -u <tu_usuario> -p -e "CREATE DATABASE IF NOT EXISTS sigu"
mysql -u <tu_usuario> -p sigu < src/main/resources/scheme.sql
```
4. Levantá el servidor:
```bash
mvn process-classes exec:java
```
   Las variables de `.env` se cargan automáticamente al iniciar.
5. Abrí `http://localhost:8080`.

### Correr los tests
```bash
mvn test -P test
```
Corre contra una base SQLite aislada (`./target/test.db`), no toca tu base de
desarrollo.

## Estilos (Tailwind CSS)

El proyecto usa un build local de Tailwind (no CDN). Si modificás clases en los
templates y necesitás que el CSS las incluya, regenerá el archivo compilado:

1. Descargá el CLI standalone (una vez): ver instrucciones en
   https://tailwindcss.com/blog/standalone-cli
2. Corré: `./tailwindcss -i ./tailwind-src/input.css -o ./public/css/tailwind.css --minify`
3. Commiteá `public/css/tailwind.css` junto con tu cambio.

## Capturas

<!-- TODO: agregar screenshots del dashboard, panel de docente, e inscripción a
materias cuando estén disponibles -->
