# vet-system — Clínica Veterinaria "Patitas Felices"

Universidad de Palermo · Microservicios y APIs Escalables · 2026
Ing. Jorge Agustín Pereyra

Sistema de gestión de una clínica veterinaria, construido sprint a sprint desde un monolito MVC
(Fase 1) hasta una arquitectura de microservicios completa (Fase 2).

## Sprint actual: Sprint 7 — Swagger + frontend + análisis del monolito (cierre Fase 1)

API REST completa de la clínica: CRUD de Dueño, Mascota, Veterinario y Turno, con DTOs,
validación de entrada y respuestas de error uniformes.

> **Para estudiar: [`docs/GUIA-SPRINTS.md`](docs/GUIA-SPRINTS.md)** — qué se construyó en
> cada sprint, qué concepto hay que poder defender, dónde está en el código y las preguntas
> probables del oral.

| Sprint | Rama | Tema |
|--------|------|------|
| 1 | `main` | Dominio + Setup (4 entidades JPA, MySQL) |
| 2 | `sprint-02` | MVC + REST + CRUD Dueño |
| 3 | `sprint-03` | Relaciones + CRUD Mascota + JSON circular |
| 4 | `sprint-04` | DTOs + MapStruct + CRUD Turno y Veterinario |
| 5 | `sprint-05` | Bean Validation + `@ControllerAdvice` |
| 6 | `sprint-06` | Tests: JUnit 5 + Mockito + MockMvc (`./mvnw test`) |
| 7 | `sprint-07` | Swagger UI + CORS + frontend Bootstrap + [análisis del monolito](docs/analisis-monolito.md) |

## Stack

- Java 21 · Spring Boot 4.1.0
- Spring Web, Spring Data JPA (Hibernate 7), Bean Validation, Lombok, DevTools
- MapStruct 1.5.5.Final (mappers entidad ↔ DTO generados en compilación)
- springdoc-openapi 3.1.1 (Swagger UI) · Bootstrap 5.3.3 + Fetch API en el frontend
- MySQL — verificado contra 9.7.1 (la fórmula `mysql` de Homebrew). El dialecto lo
  autodetecta Hibernate, así que 8.x también funciona sin cambiar nada.

## Requisitos previos

En macOS con Homebrew:

```bash
brew install openjdk@21 mysql
```

`openjdk@21` es *keg-only*: Homebrew no lo linkea al PATH global. Hay que apuntar `JAVA_HOME`
a mano, una sola vez, en `~/.zshenv`:

```bash
cat >> ~/.zshenv <<'EOF'
export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
export PATH="$JAVA_HOME/bin:$PATH"
EOF
```

Va en `.zshenv` y no en `.zshrc` porque `.zshrc` solo lo leen las shells **interactivas**:
un script, un hook de git o el build de IntelliJ no lo verían y fallarían con
`Unable to locate a Java Runtime`. `.zshenv` lo leen todas.

No hace falta instalar Maven: el repo incluye el Maven Wrapper (`mvnw`), que descarga la
versión correcta de Maven la primera vez.

## Cómo levantar el proyecto

1. Arrancar MySQL:

   ```bash
   brew services start mysql
   ```

2. La base `vet_system` se crea sola en el primer arranque
   (`createDatabaseIfNotExist=true` en la URL de conexión). Si preferís crearla a mano,
   el script está en [`docs/setup_db.sql`](docs/setup_db.sql).

3. Contraseña de MySQL. Homebrew instala MySQL con el usuario `root` **sin contraseña**, que
   es el default que asume `application.properties`. Si tu `root` sí tiene contraseña,
   exportala antes de correr — nunca la commitees:

   ```bash
   export DB_PASSWORD="tu_password"
   ```

4. Levantar la aplicación:

   ```bash
   ./mvnw spring-boot:run
   ```

5. Verificar en los logs que aparece `Started VetSystemApplication` y los `create table`
   de Hibernate (`show-sql=true`).

6. Probar el endpoint de verificación:

   ```bash
   curl http://localhost:8080/api/duenos
   ```

   Respuesta esperada: `[]` (array vacío) con HTTP 200 si no hay dueños cargados.

7. Confirmar las tablas en MySQL:

   ```bash
   mysql -u root -e "USE vet_system; SHOW TABLES;"
   ```

   Debe listar `duenos`, `mascotas`, `turnos`, `veterinarios`.
   La salida real de esta verificación está guardada en
   [`docs/evidencia-sprint-01.txt`](docs/evidencia-sprint-01.txt).

## Documentación y frontend

- **Swagger UI:** <http://localhost:8080/swagger-ui.html> (especificación OpenAPI en `/v3/api-docs`)
- **Frontend:** con el backend corriendo,

  ```bash
  cd frontend && python3 -m http.server 5500
  ```

  y abrir <http://localhost:5500>. Lista los dueños y permite dar de alta uno nuevo.

## Modelo de dominio

Ver [`docs/diagrama-clases.md`](docs/diagrama-clases.md) para el diagrama de clases y las
relaciones entre entidades (Dueño, Mascota, Veterinario, Turno).

## Estructura de paquetes

```
src/main/java/com/vetSystem/vet_system/
├── VetSystemApplication.java
├── controller/     DuenoController · MascotaController · VeterinarioController · TurnoController
├── service/        DuenoService · MascotaService · VeterinarioService · TurnoService
├── repository/     DuenoRepository · MascotaRepository · VeterinarioRepository · TurnoRepository
├── dto/            DuenoDTO · MascotaDTO · VeterinarioDTO · TurnoRequestDTO · TurnoResponseDTO
├── mapper/         DuenoMapper · MascotaMapper · VeterinarioMapper · TurnoMapper  (MapStruct)
├── config/        SwaggerConfig · WebConfig (CORS)
├── exception/      ResourceNotFoundException · DuplicateResourceException
│                   TurnoSuperpuestoException · ErrorResponse · GlobalExceptionHandler
└── model/          Dueno · Mascota · Veterinario · Turno · EstadoTurno  (entidades JPA)
```

**Regla de las capas:** el Controller no tiene lógica de negocio, el Service no habla
directamente con la base, el Repository no valida reglas de negocio. Las entidades JPA no
tienen anotaciones de serialización: eso es responsabilidad de los DTOs.

## API REST

### Dueño — `/api/duenos`

| Método | Ruta | Éxito | Error |
|--------|------|-------|-------|
| GET | `/api/duenos` | `200` + lista | — |
| GET | `/api/duenos/{id}` | `200` | `404` |
| GET | `/api/duenos/{id}/mascotas` | `200` + lista | `404` si el dueño no existe |
| POST | `/api/duenos` | `201` | `400` inválido · `409` DNI duplicado |
| PUT | `/api/duenos/{id}` | `200` | `400` · `404` |
| DELETE | `/api/duenos/{id}` | `204` | `404` · `409` si tiene turnos agendados |

### Mascota — `/api/mascotas`

| Método | Ruta | Éxito | Error |
|--------|------|-------|-------|
| GET | `/api/mascotas` | `200` + lista | — |
| GET | `/api/mascotas/{id}` | `200` | `404` |
| POST | `/api/mascotas?duenoId={id}` | `201` | `400` · `404` dueño · `409` nombre repetido |
| PUT | `/api/mascotas/{id}` | `200` | `400` · `404` |
| DELETE | `/api/mascotas/{id}` | `204` | `404` · `409` si tiene turnos |

### Veterinario — `/api/veterinarios`

| Método | Ruta | Éxito | Error |
|--------|------|-------|-------|
| GET | `/api/veterinarios` | `200` + lista | — |
| GET | `/api/veterinarios/{id}` | `200` | `404` |
| POST | `/api/veterinarios` | `201` | `400` · `409` matrícula duplicada |
| PUT | `/api/veterinarios/{id}` | `200` | `400` · `404` |
| DELETE | `/api/veterinarios/{id}` | `204` | `404` · `409` si tiene turnos |

### Turno — `/api/turnos`

| Método | Ruta | Éxito | Error |
|--------|------|-------|-------|
| GET | `/api/turnos` | `200` + lista | — |
| GET | `/api/turnos/{id}` | `200` | `404` |
| GET | `/api/turnos/agenda?veterinarioId={id}&fecha={yyyy-MM-dd}` | `200` + lista | — |
| POST | `/api/turnos` | `201` | `400` · `404` · `409` horario superpuesto |
| PATCH | `/api/turnos/{id}/estado?estado={E}&observaciones={txt}` | `200` | `400` · `404` |

Un turno **no se borra**: se cancela con `PATCH ?estado=CANCELADO`, para no perder el
historial clínico de la mascota. `DELETE /api/turnos/{id}` devuelve `405 Method Not Allowed`.

Reglas de negocio aplicadas:
- El `PUT` de Dueño **no actualiza el DNI**; el de Veterinario **no actualiza la matrícula**.
  Son identificadores de negocio.
- Un veterinario no puede tener dos turnos la misma fecha y hora → `409`.
- Un dueño no puede tener dos mascotas con el mismo nombre → `409`.

### Formato de error

Todas las respuestas de error usan la misma estructura:

```json
{
    "timestamp": "2026-09-01T19:39:14.557147",
    "status": 404,
    "error": "Not Found",
    "mensaje": "Dueno con id 9999 no fue encontrado",
    "path": "/api/duenos/9999"
}
```

### Probar la API

Colección de Postman:
[`docs/vet-system-sprint-02.postman_collection.json`](docs/vet-system-sprint-02.postman_collection.json)
(cubre el CRUD de Dueño del Sprint 2).

Salida real de las pruebas de cada sprint:
[`evidencia-sprint-01`](docs/evidencia-sprint-01.txt) ·
[`02`](docs/evidencia-sprint-02.txt) ·
[`03`](docs/evidencia-sprint-03.txt) ·
[`04`](docs/evidencia-sprint-04.txt) ·
[`05`](docs/evidencia-sprint-05.txt)

## Definition of Done

**Sprint 7** — springdoc + `/swagger-ui.html` · `@Tag`/`@Operation`/`@ApiResponse` en
`DuenoController` y `TurnoController` · `@Schema` en `DuenoDTO` y `TurnoRequestDTO` · CORS ·
`frontend/index.html` (GET + POST + errores en pantalla) · `docs/analisis-monolito.md` ·
13 tests siguen verdes. ✅

**Sprint 6** — `DuenoServiceTest` (6) · `TurnoServiceTest` (2) · `DuenoControllerTest` (5) ·
patrón AAA · `verify(never())` en los casos de error · sin `@SpringBootTest` ·
`./mvnw test` → BUILD SUCCESS con 13 tests. ✅

**Sprint 5** — Bean Validation en los 4 DTOs de entrada · `@Valid` en todos los POST/PUT ·
`ErrorResponse` · `GlobalExceptionHandler` con 9 handlers · excepciones tipadas ·
try-catch eliminados de los 4 controllers · ningún endpoint devuelve 500 por error del
cliente. ✅

**Sprint 4** — MapStruct configurado · 5 DTOs · 4 mappers · CRUD Veterinario · CRUD Turno con
validación de superposición · `PATCH /estado` · `GET /agenda` · Dueño y Mascota
refactorizados a DTOs · anotaciones Jackson eliminadas de las entidades. ✅

**Sprint 3** — `@JsonManagedReference`/`@JsonBackReference` · `MascotaRepository` ·
`MascotaService` · `MascotaController` · `GET /api/duenos/{id}/mascotas` · JSON circular
resuelto (37.982 → 105 bytes). ✅

**Sprint 2** — `DuenoRepository` · `DuenoService` · `DuenoController` con 5 endpoints ·
200/201/204/404/409 correctos · colección Postman. ✅

**Sprint 1** — 4 entidades JPA · `EstadoTurno` · conexión MySQL · 4 tablas + 3 FKs creadas
por Hibernate. ✅

**Pendiente en todos los sprints:** push de las ramas a GitHub y PR hacia `main`.

## Próximo: Fase 2 — Microservicios

- Sprint 8: Parcial 1
- Sprint 9: Arquitectura Hexagonal
- Sprint 10: Eureka + Config Server
- Sprint 11: API Gateway + Resilience4J
- Sprint 12: Spring Security + JWT
- Sprint 13: OpenFeign
- Sprint 14: Redis + MongoDB + Docker Compose

## Parcial 1 — Decisiones de diseño

### Relación Turno–Medicamento

Elegí una entidad intermedia, `TurnoMedicamento`, en lugar de un `@ManyToMany`. Un `@ManyToMany` genera una tabla de unión que solo guarda los dos IDs, y yo necesitaba guardar un dato propio de la receta: el precio del medicamento en el momento en que se recetó. Así, si mañana cambia el `precioUnitario` del medicamento, lo que ya se vendió en un turno no se modifica, que es como funciona cualquier comprobante de venta. La tabla `turno_medicamentos` tiene dos `@ManyToOne` obligatorios (hacia `turnos` y hacia `medicamentos`) y una restricción `UNIQUE (turno_id, medicamento_id)`, porque en la clínica no tiene sentido recetar dos veces el mismo medicamento en un mismo turno. La relación es unidireccional: `Turno` no tiene una lista de medicamentos, y la consulta se hace con `findByTurnoId` en el repositorio, así no toqué la entidad `Turno` ni corro riesgo de serialización circular. Como las FKs protegen el historial, un medicamento que ya fue recetado no se puede borrar y la API responde 409. El esquema lo genera Hibernate con `spring.jpa.hibernate.ddl-auto=update`, que para este trabajo alcanza porque solo agrega tablas y columnas nuevas y nunca borra datos existentes; en producción usaría migraciones versionadas con Flyway o Liquibase, porque `update` no permite revisar ni revertir los cambios.

### Validación de stock

El control está en la capa de servicio, en `TurnoMedicamentoService.recetarMedicamento`. Antes de tocar el stock valido, en este orden, que exista el turno y el medicamento (404) y que ese medicamento no esté ya recetado en ese turno (409), para que un pedido inválido nunca descuente una unidad. Para el stock no leo el valor y después lo guardo restado, porque esos dos pasos separados permiten que dos pedidos simultáneos con una sola unidad pasen la validación y dejen el stock en -1. En cambio, uso una sola sentencia en el repositorio: `UPDATE Medicamento m SET m.stock = m.stock - 1 WHERE m.id = :id AND m.stock > 0`, que valida y descuenta de forma atómica en la base. El método devuelve la cantidad de filas modificadas: si es 0, no había stock y lanzo `StockInsuficienteException`, que el `GlobalExceptionHandler` convierte en un 422 con el `ErrorResponse` estándar. Lo probé mandando dos pedidos al mismo tiempo sobre un medicamento con stock 1: uno recibió 201, el otro 422, y el stock quedó en 0. Usé 422 y no 400 porque el pedido está bien formado, y no 409 porque no choca con otro registro: lo que falla es una regla de negocio.

### Solapamiento

La validación está en `TurnoService.createTurno`, después de comprobar que existen la mascota y el veterinario, para que un ID inexistente dé 404 y no un falso 409. La consulta es `findFirstByVeterinarioIdAndFechaAndHoraAndEstadoNot`, que compara el mismo veterinario, la misma fecha y la misma hora exacta del turno pedido. Usé un `findFirst` que devuelve el turno, y no un `exists` que devuelve true o false, porque la consigna pide informar cuál es el turno en conflicto: el mensaje del 409 incluye su ID, su fecha y su hora. Además excluyo los turnos en estado `CANCELADO`, porque si no, un turno cancelado dejaría ese horario bloqueado para siempre aunque el veterinario esté libre. Una limitación conocida es que el algoritmo compara la hora exacta y no una duración, así que un turno a las 10:30 y otro a las 10:45 no se detectan como superpuestos. Para resolverlo haría falta agregar la duración del turno y buscar por rango de horario.

### Cupo de mascotas

La validación está en `MascotaService.createMascota` y usa `countByDuenoId`, que genera un `SELECT COUNT(*) FROM mascotas WHERE dueno_id = ?` sin cargar las mascotas en memoria. Si el dueño ya tiene 5, lanzo `CupoMascotasExcedidoException` y el handler responde 422, compartiendo el manejo con la falta de stock porque en los dos casos los datos son válidos pero rompen una regla de negocio. Como criterio de "mascota activa" tomé todas las mascotas registradas del dueño. Es coherente con el modelo actual: el DELETE de mascotas es físico, así que una mascota que ya no está en la clínica se borra, y una mascota con turnos no se puede borrar porque la FK de `turnos` lo impide. La limitación de este criterio es que una mascota que falleció pero tiene historial de turnos sigue ocupando un lugar del cupo para siempre, porque no hay forma de darla de baja sin perder ese historial. La solución sería una baja lógica con un campo `activa` en `Mascota`, pero la consigna pedía no modificar el modelo, así que la dejo documentada como mejora. Otra limitación es que el conteo y el alta son dos pasos, por lo que dos altas simultáneas con 4 mascotas cargadas podrían terminar en 6; se resolvería bloqueando la fila del dueño durante la validación.

### Decisión más difícil

Lo más difícil fue el descuento de stock, porque la forma obvia de hacerlo tiene un error que no se ve probando de a un pedido. La primera idea era buscar el medicamento, preguntar si el stock es mayor a 0 y guardarlo con una unidad menos. Eso funciona siempre que los pedidos lleguen de a uno, pero entre la lectura y el guardado otro pedido puede leer el mismo stock y los dos lo descuentan. Lo resolví moviendo la condición adentro del `UPDATE`, para que la base haga la validación y el descuento en una sola operación atómica, y usando la cantidad de filas modificadas para saber si había stock. También tuve que pensar el orden de las validaciones, para que un pedido duplicado o con IDs inexistentes falle antes de tocar el stock. Lo verifiqué enviando dos pedidos simultáneos por la última unidad y confirmando que solo uno se concreta y que el stock nunca queda negativo.
