# Task Manager APIA

[![CI Status](https://github.com/SergioAntonLopezumu/PC_26-27/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/SergioAntonLopezumu/PC_26-27/actions/workflows/ci.yml)

API REST para gestionar tareas con Spring Boot, JPA y PostgreSQL. Esta aplicación permite crear, listar, consultar, actualizar y eliminar tareas con validaciones básicas y persistencia en base de datos.

## Descripción

El proyecto consiste en un backend de gestión de tareas con los siguientes elementos:

- Spring Boot 3.3.4
- Java 21
- Maven
- Spring Web
- Spring Data JPA
- Hibernate + PostgreSQL
- Spring Boot Actuator (`/actuator/health`)
- Docker y Docker Compose
- Validación de datos con Bean Validation
- Endpoints REST bajo la ruta `/api`

## Requisitos previos

Antes de empezar, asegúrate de tener instalado lo siguiente:

- Java 21 o superior
- Maven 3.9+
- Git
- Un editor como IntelliJ IDEA, VS Code o Eclipse

## Clonar el repositorio

```bash
git clone https://github.com/SergioAntonLopezumu/PC_26-27
cd Boletín1
```

> Si el repositorio ya está creado y enlazado con GitHub, solo necesitas entrar en la carpeta del proyecto y continuar con los siguientes pasos.

## Configuración inicial

La aplicación usa PostgreSQL. Las tablas las crea Hibernate al arrancar (`spring.jpa.hibernate.ddl-auto=update`). La forma recomendada de ejecutarla es con Docker (ver más abajo), que levanta la API y la base de datos juntas.

## Compilar el proyecto

```bash
mvn clean package
```

Esto genera el artefacto ejecutable en:

```bash
target/task-manager-0.0.1-SNAPSHOT.jar
```

## Ejecutar la aplicación

### Opción 1: con Maven

```bash
mvn spring-boot:run
```

### Opción 2: con el JAR generado

```bash
java -jar target/task-manager-0.0.1-SNAPSHOT.jar
```

La API quedará disponible en:

```text
http://localhost:8080
```

## Probar la API

### Endpoints disponibles

Base URL:

```text
/api
```

| Método | Ruta | Descripción |
| --- | --- | --- |
| GET | `/api/tasks` | Lista tareas paginadas; admite `?page=0&size=10`, el filtro opcional por estado `?status=PENDING`, `?status=IN_PROGRESS` o `?status=COMPLETED` y el filtro por rango de fechas `?from=2026-10-01&to=2026-10-31` |
| GET | `/api/tasks/{id}` | Devuelve una tarea por su identificador |
| POST | `/api/tasks` | Crea una nueva tarea |
| PUT | `/api/tasks/{id}` | Actualiza una tarea existente |
| DELETE | `/api/tasks/{id}` | Elimina una tarea |

### Ejemplo de creación de tarea

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Estudiar GitHub",
    "description": "Preparar el boletín de colaboración con pull requests",
    "status": "PENDING",
    "priority": "HIGH",
    "dueDate": "2026-10-10"
  }'
```

### Ejemplo de listado

```bash
curl http://localhost:8080/api/tasks
```

El listado usa `page=0` y `size=10` por defecto. La respuesta contiene las tareas en
`content`, el total de elementos en `totalElements` y el total de páginas en
`totalPages`. Por ejemplo:

```bash
curl "http://localhost:8080/api/tasks?page=0&size=10"
```

Para listar únicamente las tareas en un estado:

```bash
curl "http://localhost:8080/api/tasks?status=PENDING"
```

### Filtrar por rango de fechas límite

Los parámetros `from` y `to` (formato ISO `YYYY-MM-DD`) permiten acotar las tareas
cuya fecha límite (`dueDate`) cae dentro del rango, ambos inclusive. Son opcionales,
se pueden usar por separado y se combinan con `status` y con la paginación:

```bash
curl "http://localhost:8080/api/tasks?from=2026-10-01&to=2026-10-31"
curl "http://localhost:8080/api/tasks?from=2026-10-01"
curl "http://localhost:8080/api/tasks?to=2026-10-31&status=PENDING&page=0&size=5"
```

Si `from` es posterior a `to`, la API responde `400 Bad Request`.

## Validar el proyecto

Ejecuta la suite de pruebas:

```bash
mvn test
```

Si deseas compilar y ejecutar desde cero en un entorno limpio, puedes usar:

```bash
mvn clean test
mvn spring-boot:run
```

## Ejecutar con Docker

Requisitos: [Docker Desktop](https://www.docker.com/products/docker-desktop/) (incluye Docker Compose).

1. Crea tu fichero de configuración local a partir de la plantilla (`.env` no se sube al repositorio):

```bash
cp .env.example .env
```

2. Edita `.env` y cambia `POSTGRES_PASSWORD` por una contraseña propia. Variables disponibles:

| Variable | Descripción |
| --- | --- |
| `POSTGRES_DB` | Nombre de la base de datos |
| `POSTGRES_USER` | Usuario de PostgreSQL |
| `POSTGRES_PASSWORD` | Contraseña de PostgreSQL (obligatoria) |

3. Construye la imagen y levanta la API y PostgreSQL:

```bash
docker compose up --build -d
docker compose ps
```

4. Comprueba que está lista (ambos servicios en estado `healthy`):

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/tasks
```

5. Para parar el stack:

```bash
docker compose down      # elimina contenedores y red; los datos se conservan
docker compose down -v   # elimina además el volumen: se pierden los datos
```

> Si cambias las credenciales en `.env` con el volumen ya creado, PostgreSQL seguirá usando las antiguas. En desarrollo, ejecuta `docker compose down -v` y vuelve a levantar.

### Construir solo la imagen

El `Dockerfile` es multi-stage (compila con Maven y ejecuta con un JRE, con usuario sin privilegios):

```bash
docker build -t tareas-api:0.1 .
```

La imagen necesita una base de datos PostgreSQL accesible, por lo que no arranca sola: usa Compose.

## Ejecutar con Dev Container

Un dev container da el mismo entorno de desarrollo (JDK 21, Maven, Docker y extensiones de VS Code) sin instalar nada en local.

Requisitos: Docker Desktop y VS Code con la extensión [Dev Containers](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers).

1. Abre la carpeta del proyecto en VS Code.
2. Ejecuta `Dev Containers: Reopen in Container` desde la paleta de comandos (`Ctrl+Shift+P`). La primera vez tarda porque construye el contenedor; el `postCreateCommand` crea `.env` desde `.env.example` si no existe y descarga las dependencias de Maven.
3. Dentro del contenedor, abre una terminal y ejecuta:

```bash
docker compose up --build -d
```

4. Comprueba desde el navegador de tu máquina `http://127.0.0.1:8080/actuator/health` (los puertos 8080 y 5432 se reenvían automáticamente).

## Hooks de Git (Boletín 1)

Si en tu proyecto has configurado hooks del boletín anterior, actívalos para automatizar comprobaciones antes de cada commit:

```bash
git config core.hooksPath .githooks
chmod +x .githooks/*
```

Si aún no has creado los hooks, puedes crear la carpeta y dejar allí los scripts que necesites para validar el proyecto antes de hacer commits.

## Flujo recomendado de trabajo

1. Crear una rama para cada funcionalidad:

```bash
git checkout -b feat/nueva-funcionalidad
```

2. Implementar cambios y ejecutar pruebas:

```bash
mvn test
```

3. Hacer commit y push:

```bash
git add .
git commit -m "feat: nueva funcionalidad"
git push origin feat/nueva-funcionalidad
```

4. Abrir un Pull Request y fusionarlo tras revisión.

## Estructura del proyecto

```text
Boletín1/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── .devcontainer/
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/taskmanager/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
└── target/
```

## Resumen

Este proyecto sirve como base para el boletín de GitHub y colaboración con Pull Requests. El objetivo final es publicar el repositorio en GitHub, trabajar con ramas, abrir Pull Requests y seguir buenas prácticas de revisión y coordinación en equipo.
