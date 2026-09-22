# vet-system — Clínica Veterinaria "Patitas Felices"

Universidad de Palermo · Microservicios y APIs Escalables · 2026
Ing. Jorge Agustín Pereyra

Sistema de gestión de una clínica veterinaria, construido sprint a sprint desde un monolito MVC
(Fase 1) hasta una arquitectura de microservicios completa (Fase 2).

## Integrantes

- [Nombre Apellido 1] — TODO
- [Nombre Apellido 2] — TODO

## Sprint actual: Sprint 6 — Testing: JUnit 5 + Mockito + MockMvc

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

## Stack

- Java 21 · Spring Boot 4.1.0
- Spring Web, Spring Data JPA (Hibernate 7), Bean Validation, Lombok, DevTools
- MapStruct 1.5.5.Final (mappers entidad ↔ DTO generados en compilación)
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

## Próximo sprint (Sprint 6)

- Testing: JUnit 5 + Mockito para tests unitarios de Services
- MockMvc para tests de integración de Controllers
- Patrón Arrange-Act-Assert (AAA)
- Criterios de cobertura: qué vale la pena testear y qué no
