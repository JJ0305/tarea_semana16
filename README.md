# Prototipo de gestión de clientes y solicitudes

Aplicación web sencilla en Java 21 y Spring Boot con interfaz independiente, API REST y persistencia MySQL. La autenticación utiliza contraseñas BCrypt, sesiones y protección CSRF.

## Ejecutar localmente

Requiere Docker y Docker Compose:

```powershell
docker compose up --build
```

Abre <http://localhost:8081>. Para la demostración local se crea el usuario `demo` con contraseña `demo1234`. Cambia estas credenciales antes de publicar o desplegar el prototipo. Se pueden configurar con `APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`, `DB_PASSWORD` y `MYSQL_ROOT_PASSWORD`; también se puede cambiar el puerto con `APP_PORT` (por defecto, `8081`).

La aplicación crea las tablas de MySQL en el primer inicio y conserva los datos en el volumen `mysql_data`. Para apagarla sin borrar los datos:

```powershell
docker compose down
```

## Funciones y API

- Inicio/cierre de sesión: `POST /api/auth/login`, `POST /api/auth/logout`; sesión actual: `GET /api/auth/me`.
- Token CSRF necesario para operaciones de escritura: `GET /api/auth/csrf`.
- Clientes: `GET /api/clients`, `POST /api/clients`.
- Solicitudes: `GET /api/requests`, `POST /api/requests`.
- Las rutas de datos requieren sesión autenticada. Las entradas se validan y el correo de cliente es único.

## Contenedores

- `frontend`: Nginx sirve los recursos web y reenvía `/api` al backend.
- `backend`: aplicación REST Spring Boot.
- `mysql`: base de datos MySQL 8.4 con volumen persistente.

El `Dockerfile` de la raíz construye la API y `frontend/Dockerfile` construye la interfaz. `docker-compose.yml` los ejecuta juntos.

## CI con GitHub Actions

El flujo `.github/workflows/ci.yml` se ejecuta en `push` y `pull_request` contra `main`/`master`. Descarga el código, compila y ejecuta `./mvnw clean verify`, analiza Java con CodeQL, construye las dos imágenes, ejecuta escaneo Trivy y, en eventos que no sean PR, publica ambas imágenes en GitHub Container Registry (GHCR) con el SHA del commit.

En GitHub, habilita Actions y asegúrate de permitir que el `GITHUB_TOKEN` publique paquetes. CodeQL puede requerir habilitar GitHub Code Security para el repositorio. Los escaneos fallan el flujo ante vulnerabilidades `HIGH` o `CRITICAL` con corrección disponible.

## CD con Jenkins

El `Jenkinsfile` despliega a un entorno de prueba en el host/agente que ejecuta Docker Compose. Configura un Pipeline multibranch o Pipeline desde SCM, instala Docker Compose v2 y el plugin Credentials Binding, y configura estos secretos Jenkins:

| ID | Tipo | Uso |
| --- | --- | --- |
| `ghcr-readonly` | Username with password | Usuario GitHub y PAT con permiso de lectura de paquetes |
| `calificada16-db-password` | Secret text | Contraseña de MySQL de aplicación |
| `calificada16-mysql-root-password` | Secret text | Contraseña root de MySQL |
| `calificada16-admin-password` | Secret text | Contraseña de la cuenta administradora |

Conecta el webhook de GitHub al job de Jenkins para iniciar el despliegue tras la publicación del pipeline CI. Al ejecutar Jenkins, especifica el propietario/organización GHCR en `GHCR_OWNER` y la etiqueta del commit en `IMAGE_TAG` (por ejemplo, el SHA publicado por Actions). Jenkins descarga las imágenes, levanta MySQL/API/frontend con Compose y comprueba la página en `http://localhost:8081`.

El host de prueba debe permitir tráfico al puerto configurado (`8081` por defecto) y mantener persistente el volumen de Docker. En un entorno real, cambia los secretos de ejemplo, limita el acceso a Jenkins/puerto y configura TLS mediante un proxy inverso.

## Pruebas locales

```powershell
.\mvnw.cmd -B clean verify
```

Las pruebas usan H2 en memoria; no requieren una instancia MySQL.
