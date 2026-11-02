
# Boletín 1

## Resumen de commits

| # | Mensaje del commit | Qué se hizo | Autor | 
|---|--------------------|-------------|-------|
| 1 | `chore: proyecto Maven inicial de la API de tareas` | Se ha inicializado el proyecto Maven para la API de gestión de tareas. | Sergio Antón |
| 2 | `chore(editor): añade .editorconfig` | Se ha añadido la configuración del editor para la indentación y el estilo del proyecto. | Sergio Antón | 
| 3 | `feat(task): añade ordenación de tareas por prioridad y fecha` | Se ha añadido la ordenación de la lista de tareas por prioridad y fecha límite. | Sergio Antón | 
| 4 | `chore(build): añade Spotless para formateo automático` | Se ha configurado Spotless para formatear el código de forma automática. | Sergio Antón |
| 5 | `chore(hooks): añade hook de pre-commit con Spotless` | Se ha añadido el hook de pre-commit para ejecutar el formateo antes de confirmar cambios. | Sergio Antón | 
| 6 | `feat: cambio en rama 1` | Se ha cambiado la ruta del endpoint de tareas a `/tasksRAMA1`. | Sergio Antón |
| 7 | `feat: cambio en rama 2` | Se ha cambiado la misma ruta del endpoint a `/tasksCONFLICTORAMA2` para provocar un conflicto. | Sergio Antón |
| 8 | `fix: resuelve conflicto de fusión entre rama-1 y rama-2` | Se ha resuelto el conflicto entre las dos ramas y se ha dejado la ruta final correcta. | Sergio Antón | 

## Detalle por commit

### 1. `chore: proyecto Maven inicial de la API de tareas`
- **Qué se hizo:** Se ha creado la estructura inicial del proyecto Maven para la API de tareas.
- **Autor:** Sergio Antón
- **Relación con el boletín:** Parte B. Se ha creado una API REST con Spring Boot, Java 21 y Maven. La aplicación tiene una entidad `Task`, operaciones CRUD, separación por capas y persistencia con Spring Data JPA y H2.
- **Uso de IA:** Se ha utilizado la IA integrada de Visual Studio Code para crear parte de la estructura inicial y hacer algunos ajustes.

### 2. `chore(editor): añade .editorconfig`
- **Qué se hizo:** Se ha añadido la configuración del editor para que el proyecto use siempre la misma indentación y los mismos finales de línea.
- **Autor:** Sergio Antón
- **Relación con el boletín:** Parte D. El archivo indica que se usan finales de línea LF, cuatro espacios para Java y dos espacios para los archivos de configuración.
- **Uso de IA:** Ninguno

### 3. `feat(task): añade ordenación de tareas por prioridad y fecha`
- **Qué se hizo:** Se ha añadido la ordenación de la lista de tareas según prioridad y fecha límite.
- **Autor:** Sergio Antón
- **Relación con el boletín:** Parte D. Es el cambio pequeño pero no trivial que se hizo en la rama de funcionalidad. Las tareas se devuelven ordenadas primero por prioridad y después por fecha límite.
- **Uso de IA:** Se ha utilizado la IA integrada de Visual Studio Code para hacer este cambio.
  - Prompt: "Haz un pequeño cambio en el código añadiendo que se devuelvan las listas ordenadas."
### 4. `chore(build): añade Spotless para formateo automático`
- **Qué se hizo:** Se ha añadido Spotless para formatear el código automáticamente.
- **Autor:** Sergio Antón
- **Relación con el boletín:** Parte E. Se ha añadido el plugin de Spotless en el `pom.xml` con Google Java Format, eliminación de imports no usados, eliminación de espacios finales y salto de línea final.
- **Uso de IA:** Ninguno

### 5. `chore(hooks): añade hook de pre-commit con Spotless`
- **Qué se hizo:** Se ha añadido un hook de pre-commit para que el formateo se ejecute antes de cada commit.
- **Autor:** Sergio Antón
- **Relación con el boletín:** Parte E. El hook ejecuta `mvn -q spotless:apply` y vuelve a añadir los archivos que Spotless haya modificado.
- **Uso de IA:** Ninguno.

### 6. `feat: cambio en rama 1`
- **Qué se hizo:** Se ha cambiado la URL del método getAllTask de _@GetMapping("/tasks")_ a _@GetMapping("/tasksRAMA1")_.
- **Autor:** Sergio Antón
- **Relación con el boletín:** Parte F
- **Uso de IA:** Ninguno

### 7. `feat: cambio en rama 2`
- **Qué se hizo:** Se ha cambiado la URL del método getAllTask de _@GetMapping("/tasks")_ a _@GetMapping("/tasksCONFLICTORAMA2")_
- **Autor:** Sergio Antón
- **Relación con el boletín:** Parte F
- **Uso de IA:** Ninguno

### 8. `fix: resuelve conflicto de fusión entre rama-1 y rama-2`
- **Qué se hizo:** Se ha resuelto el conflicto eliminando los marcadores de Git y dejando la versión final correcta del endpoint.
- **Autor:** Sergio Antón
- **Relación con el boletín:** Parte F
- **Uso de IA:** Ninguno

## Descripción narrativa de lo hecho en la sesión y relación con los commits

### Parte A

Se ha comprobado que el entorno tiene Git, Java 21 y Maven instalados. También se ha configurado la identidad de Git y la rama principal del repositorio.

### Parte B

Se ha creado una API REST de gestión de tareas con Spring Boot y Maven. La aplicación tiene una entidad `Task` con título, descripción, estado, prioridad y fecha límite. También tiene validaciones, las operaciones CRUD y las capas de controlador, servicio y repositorio.

Además, se ha añadido el manejo centralizado de errores con `@RestControllerAdvice`. De esta forma, si no existe una tarea se devuelve un error 404 y si los datos no son válidos se devuelve un error 400.

![Captura de la estructura del proyecto](image.png)


### Parte C

Se ha compilado, probado y empaquetado el proyecto con Maven. El resultado es un JAR ejecutable dentro de la carpeta `target`. También se ha arrancado la aplicación y se han probado los endpoints registrando y recuperando tareas.

![Captura de la aplicación funcionando y petición a la API](<Captura de pantalla 2026-09-22 145759.png>)

### Parte D

Se ha inicializado el repositorio Git y se han añadido los archivos de configuración iniciales. Después se ha añadido el `.editorconfig` en un commit separado.

También se ha creado una rama para hacer un cambio pequeño pero no trivial. En este caso se ha añadido la ordenación de las tareas por prioridad y fecha límite. Después se ha fusionado la rama en la rama principal.

![Captura del historial de commits y ramas](image-1.png)

### Parte E

Se ha añadido Spotless al `pom.xml` para que el código Java tenga el mismo formato. Se han probado los comandos `mvn spotless:check` y `mvn spotless:apply`.

Después se ha creado el hook de pre-commit dentro de `.githooks`. El hook ejecuta Spotless antes de cada commit, de forma que el código se formatea automáticamente antes de guardarse en el historial.

![Captura de Spotless funcionando](image-2.png)

### Parte F

Se han creado dos ramas a partir de `main` que modificaban la misma línea del controlador de tareas. La primera rama cambiaba la ruta a `/tasksRAMA1` y la segunda a `/tasksCONFLICTORAMA2`.

Al fusionar la segunda rama se produjo un conflicto. Se han eliminado manualmente los marcadores `<<<<<<<`, `=======` y `>>>>>>>`, dejando la versión correcta del código. Finalmente se ha añadido el archivo resuelto y se ha creado el commit del conflicto.
