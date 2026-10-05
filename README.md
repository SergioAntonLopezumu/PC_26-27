# Task Manager APIA

API REST para gestionar tareas con Spring Boot, JPA y H2. Esta aplicación permite crear, listar, consultar, actualizar y eliminar tareas con validaciones básicas y almacenamiento en memoria.

## Descripción

El proyecto consiste en un backend de gestión de tareas con los siguientes elementos:

- Spring Boot 3.3.4
- Java 21
- Maven
- Spring Web
- Spring Data JPA
- Hibernate + H2 Database
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

El proyecto ya incluye la configuración necesaria para arrancar con H2 en memoria. La base de datos se crea automáticamente al iniciar la aplicación.

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
| GET | `/api/tasks` | Lista tareas paginadas; admite `?page=0&size=10` y el filtro opcional `?status=PENDING`, `?status=IN_PROGRESS` o `?status=COMPLETED` |
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

## H2 Console

La aplicación incluye H2 Console habilitado para consultar la base de datos. Puedes acceder desde:

```text
http://localhost:8080/h2-console
```

Datos por defecto:

- URL JDBC: `jdbc:h2:mem:taskdb`
- Usuario: `sa`
- Contraseña: vacía

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
