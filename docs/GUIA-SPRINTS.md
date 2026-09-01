# Guía de estudio por sprint — vet-system

Universidad de Palermo · Microservicios y APIs Escalables · 2026
Clínica Veterinaria "Patitas Felices" · Fase 1: Monolito MVC

Esta guía es para **estudiar antes del oral**. Cada sprint tiene: qué se construyó, qué
concepto hay que poder defender, dónde está en el código, y las preguntas que probablemente
te hagan.

---

## Mapa general

| Sprint | Rama | Tema central | La idea que hay que llevarse |
|--------|------|--------------|------------------------------|
| 1 | `main` | Dominio + Setup | Las entidades JPA son el modelo; Hibernate crea el schema |
| 2 | `sprint-02` | MVC + REST + CRUD Dueño | Controller → Service → Repository; los códigos HTTP son un contrato |
| 3 | `sprint-03` | Relaciones + CRUD Mascota | Las relaciones bidireccionales generan JSON circular |
| 4 | `sprint-04` | DTOs + MapStruct + Turno/Veterinario | La entidad JPA no debe ser el contrato de la API |
| 5 | `sprint-05` | Validaciones + errores globales | 4xx es error del cliente, 5xx del servidor. Nunca confundirlos |

Cada rama sale de la anterior, porque cada sprint refactoriza lo del previo.
Para ver la evolución de un archivo: `git log -p --follow <archivo>`.

---

# Sprint 1 — Dominio + Setup

## Qué se construyó
Proyecto Spring Boot 4.1.0, 4 entidades JPA (`Dueno`, `Mascota`, `Veterinario`, `Turno`),
enum `EstadoTurno`, conexión a MySQL, y un endpoint mínimo de verificación.

## Lo que tenés que entender

**`@Entity` + `@Table`** — `@Entity` le dice a Hibernate "esta clase es una tabla".
`@Table(name="duenos")` fija el nombre; sin eso Hibernate usaría el nombre de la clase.

**`@GeneratedValue(strategy = GenerationType.IDENTITY)`** — delega la generación de la PK al
motor de base (`AUTO_INCREMENT` en MySQL). Alternativas: `SEQUENCE` (PostgreSQL), `AUTO`
(Hibernate decide), `TABLE` (tabla auxiliar, casi nunca).

**`ddl-auto=update`** — Hibernate compara las entidades con las tablas existentes y crea lo
que falta. Nunca borra. Sirve para desarrollo; en producción se usa `validate` + una
herramienta de migraciones (Flyway o Liquibase).

**Quién es dueño de una relación bidireccional** — el lado que tiene la FK física en su
tabla. Lleva `@ManyToOne` + `@JoinColumn`. El otro lado lleva `@OneToMany(mappedBy="campo")`
y es un espejo de solo lectura para Hibernate.

**`@Enumerated(EnumType.STRING)`** — sin esto, Hibernate guarda el enum como número ordinal
(`0`, `1`, `2`). Si reordenás los valores del enum, todos los registros existentes quedan
apuntando al valor equivocado. Con `STRING` guarda `"PENDIENTE"`. Casi siempre es la opción
correcta.

**`@ToString.Exclude` + `@EqualsAndHashCode.Exclude`** — `@Data` de Lombok genera `toString`
con **todos** los campos. Con la relación bidireccional `Dueno ↔ Mascota` eso produce
recursión infinita: `Dueno.toString()` → recorre `mascotas` → `Mascota.toString()` → recorre
`dueno` → `StackOverflowError`. Además `equals`/`hashCode` sobre campos LAZY **fuerzan su
carga**, anulando el `FetchType.LAZY` que pusiste a propósito.

## Dónde mirarlo
- `src/main/java/com/vetSystem/vet_system/model/` — las 4 entidades
- `src/main/resources/application.properties` — `ddl-auto`, conexión
- `docs/evidencia-sprint-01.txt` — salida real de `SHOW TABLES` y los `DESCRIBE`

## Preguntas probables
1. *¿Por qué `mappedBy` va en `Dueno` y no en `Mascota`?* Porque la FK `dueno_id` está en la
   tabla `mascotas`; `Mascota` es la dueña de la relación.
2. *¿Qué pasa si borro `@ToString.Exclude`?* `StackOverflowError` la primera vez que loguees
   una entidad.
3. *¿Por qué `LAZY` y no `EAGER`?* Para no traer las mascotas cada vez que cargás un dueño.
4. *¿Usarías `ddl-auto=update` en producción?* No: no versiona los cambios ni permite
   rollback. Va Flyway/Liquibase.

---

# Sprint 2 — MVC + REST + CRUD Dueño

## Qué se construyó
`DuenoRepository`, `DuenoService`, `DuenoController` con los 5 endpoints REST,
`ResourceNotFoundException` y `DuplicateResourceException`.

## Lo que tenés que entender

**La regla de oro de las capas:**
- **Controller** → no tiene lógica de negocio. Recibe, delega, responde.
- **Service** → no habla con la base directamente. Solo reglas de negocio.
- **Repository** → no tiene lógica de negocio. Solo consultas.

Si un Controller consulta la base, está mal. Si un Repository valida reglas, está mal.

**Query Derivation** — al extender `JpaRepository<Dueno, Long>` ya tenés `findAll`,
`findById`, `save`, `deleteById`. Además, Spring **lee el nombre del método** y genera el
SQL: `existsByDni(String)` → `SELECT COUNT(*) > 0 FROM duenos WHERE dni = ?`. No hay que
escribir la consulta ni la implementación: Spring genera un proxy en tiempo de ejecución.

**Códigos HTTP como contrato:**

| Código | Cuándo |
|--------|--------|
| 200 OK | GET y PUT exitosos que devuelven datos |
| 201 Created | POST que creó un recurso |
| 204 No Content | DELETE exitoso (no hay cuerpo que devolver) |
| 400 Bad Request | Datos inválidos del cliente |
| 404 Not Found | El recurso no existe |
| 409 Conflict | Conflicto de datos (DNI duplicado, turno superpuesto) |
| 500 Server Error | Fallo inesperado del servidor. Nunca a propósito |

**`ResponseEntity`** — sin él Spring siempre devuelve 200, lo cual es incorrecto para POST
(debe ser 201) y DELETE (204).

**Constructor injection vía `@RequiredArgsConstructor`** — mejor que `@Autowired` sobre el
campo porque: permite que el campo sea `final` (inmutable), deja las dependencias explícitas
en la firma del constructor, y hace la clase testeable con `new DuenoService(mockRepo)` sin
levantar el contexto de Spring.

**Por qué `updateDueno` recarga la entidad** — el objeto que llega del JSON no tiene `id` ni
`dni`. Si hicieras `save(duenoDelJson)` insertarías una fila nueva en vez de actualizar. Por
eso se carga la entidad de la base y se le copian solo los campos editables.

**Excepción tipada vs `RuntimeException`** — atrapar `RuntimeException` a secas y devolver
409 convierte **cualquier** fallo (base caída, NPE) en "409 Conflict". Un 409 debe significar
exactamente conflicto de datos.

## Dónde mirarlo
- `repository/DuenoRepository.java` — la interfaz que Spring implementa sola
- `service/DuenoService.java` — validación de DNI, recarga en el update
- `controller/DuenoController.java` — `ResponseEntity` por caso
- `docs/evidencia-sprint-02.txt` — las 6 pruebas con sus códigos

## Preguntas probables
1. *¿Por qué el Service no devuelve `ResponseEntity`?* Porque no sabe de HTTP. Así se puede
   reusar desde un job programado o un consumidor de mensajes.
2. *¿De dónde sale la implementación de `DuenoRepository`?* Spring Data genera un proxy
   dinámico en tiempo de ejecución.
3. *¿`existsById` o `findById`?* `existsById` si solo querés saber si existe (`COUNT`, más
   barato); `findById` si vas a usar la entidad.
4. *¿Por qué 204 y no 200 en el DELETE?* Salió bien pero no hay cuerpo que devolver.

---

# Sprint 3 — Relaciones + CRUD Mascota

## Qué se construyó
`MascotaRepository`, `MascotaService`, `MascotaController`, el endpoint anidado
`GET /api/duenos/{id}/mascotas`, y la solución al JSON circular con anotaciones de Jackson.

## Lo que tenés que entender

**El JSON circular — el concepto central del sprint.** Jackson serializa `Dueno` → encuentra
`mascotas` → serializa cada `Mascota` → encuentra `dueno` → serializa `Dueno` → … hasta
explotar.

**En este repo está reproducido de verdad.** Antes del fix, `GET /api/duenos` con una mascota
cargada devolvía **37.982 bytes** de JSON anidado y Jackson cortaba con
`Document nesting depth (1001) exceeded`. Después del fix: **105 bytes**. Está en
`docs/evidencia-sprint-03.txt`.

**Ojo con no confundirlo con el problema del Sprint 1.** Son dos mecanismos distintos con la
misma causa raíz:

| | Sprint 1 | Sprint 3 |
|---|---|---|
| Quién recorre | Lombok (`toString`/`equals`) | Jackson (serialización JSON) |
| Cuándo explota | Al loguear o comparar una entidad | Al devolver la entidad por HTTP |
| Fix | `@ToString.Exclude` | `@JsonBackReference` |

**Las tres estrategias contra el JSON circular:**

| Estrategia | Cómo funciona | Cuándo |
|---|---|---|
| `@JsonManagedReference` / `@JsonBackReference` | El lado *managed* se serializa, el *back* se omite | Relación simple con un sentido claro |
| `@JsonIgnore` | El campo nunca se serializa | Cuando nunca lo necesitás en la respuesta |
| **DTOs** | No hay anotaciones Jackson en la entidad | La solución profesional → Sprint 4 |

Regla para recordar cuál va dónde:
- `@JsonManagedReference` → el que **TIENE la lista** (`Dueno` tiene `List<Mascota>`)
- `@JsonBackReference` → el que **ES el elemento** (`Mascota` tiene `Dueno`)

**Endpoint anidado** — `GET /api/duenos/5/mascotas` expresa la pertenencia en la URL misma.
Vive en `DuenoController` porque el recurso raíz de la ruta es el dueño.

**Por qué `getMascotasByDueno` valida que el dueño exista** — sin esa validación,
"el dueño no existe" y "el dueño existe pero no tiene mascotas" devolverían lo mismo (lista
vacía) y el cliente no podría distinguirlos. Con la validación: 404 vs 200 con `[]`.

**Setear el lado propietario es obligatorio** — `mascota.setDueno(dueno)` en el Service no es
decorativo. Agregar la mascota a `dueno.getMascotas()` **no** setea la FK: Hibernate la lee
de `Mascota.dueno`. Si no lo seteás, salta
`PropertyValueException: not-null property references a null or transient value`.

## Dónde mirarlo
- `git show sprint-03:src/main/java/com/vetSystem/vet_system/model/Dueno.java` — con las
  anotaciones Jackson (en `sprint-04` ya no están)
- `service/MascotaService.java` — el `setDueno` y la validación de existencia
- `docs/evidencia-sprint-03.txt` — el antes/después de bytes

## Preguntas probables
1. *¿Por qué ocurre el JSON circular?* Por la relación bidireccional: cada lado referencia al
   otro y Jackson los recorre en bucle.
2. *¿`@JsonIgnore` o `@JsonManagedReference`?* `@JsonIgnore` es más simple pero elimina el
   campo siempre; `@JsonManagedReference` conserva un lado.
3. *¿Qué pasa si te olvidás `mascota.setDueno(dueno)`?* `PropertyValueException`, porque la FK
   `dueno_id` es `NOT NULL` y sale null.
4. *¿Por qué el `duenoId` va en la query string y no en el body?* Porque una mascota no puede
   existir sin dueño: la pertenencia es parte de la ruta, no un dato editable.

---

# Sprint 4 — DTOs + MapStruct + Turno y Veterinario

## Qué se construyó
5 DTOs, 4 mappers de MapStruct, CRUD de `Veterinario`, CRUD de `Turno` con validación de
superposición, y el refactor que **elimina las anotaciones Jackson** del Sprint 3.

## Lo que tenés que entender

**Por qué las anotaciones de Jackson en la entidad estaban mal.** Es el
**Principio de Responsabilidad Única (SRP)**: una clase debe tener una sola razón para
cambiar.
- `Dueno.java` cambia cuando cambia el **modelo de datos**.
- `DuenoDTO.java` cambia cuando cambia **lo que le mostramos al cliente**.

Son dos razones distintas → dos clases distintas. Una entidad JPA no debería saber que existe
JSON, HTTP, ni ninguna tecnología de transporte.

**Con DTOs el JSON circular ni siquiera puede ocurrir**: `DuenoDTO` no tiene lista de
mascotas, así que no hay ciclo que cortar. El problema desaparece por diseño, no por parche.

**Request DTO vs Response DTO** — `TurnoRequestDTO` no tiene `estado`: un turno nuevo siempre
nace `PENDIENTE`. Si el cliente pudiera mandarlo, podría crear un turno ya `FINALIZADO`.
`TurnoResponseDTO` sí lo tiene, y además trae `mascotaNombre` y `veterinarioNombre` para que
el cliente arme la agenda sin pedir cada entidad por separado.

**MapStruct** — genera el código de conversión **en tiempo de compilación**. No es reflexión
ni magia: mirá `target/generated-sources/annotations/` después de compilar y vas a encontrar
`MascotaMapperImpl.java` con los `setX(getY())` escritos.

`componentModel = "spring"` hace que la implementación sea un `@Component` inyectable.

`@Mapping(source = "dueno.id", target = "duenoId")` navega la relación con notación de punto
y aplana el campo.

**Orden de los annotation processors** — en `pom.xml`, Lombok va **antes** que MapStruct en
`annotationProcessorPaths`. Si se invierte, MapStruct no encuentra los getters/setters que
genera `@Data` y produce mappers vacíos.

**`TurnoMapper` solo tiene `toDTO`** — la dirección inversa la hace el Service a mano, porque
no es un mapeo de campos: hay que resolver `mascotaId` y `veterinarioId` contra la base y
validar que existan.

**El orden de las validaciones en `createTurno` importa** — primero que existan mascota y
veterinario (404), después la superposición (409). Al revés, un turno con `veterinarioId`
inexistente daría 409 en vez del 404 correcto.

**`@PatchMapping` y no PUT para el estado** — PUT reemplaza el recurso completo, PATCH
actualiza una parte. Cambiar solo el estado de un turno es PATCH.

## Dónde mirarlo
- `dto/` y `mapper/` — los paquetes nuevos
- `target/generated-sources/annotations/…/MascotaMapperImpl.java` — el código generado
- `pom.xml` — el orden Lombok → MapStruct
- `service/TurnoService.java` — el orden de las validaciones
- `docs/evidencia-sprint-04.txt`

## Preguntas probables
1. *¿Por qué no exponer la entidad JPA directamente?* SRP; además el cliente podría mandar
   campos internos (como el `id`) y un cambio en la base rompería la API.
2. *¿MapStruct usa reflexión?* No, genera código Java en compilación. Mostrá
   `target/generated-sources/`.
3. *¿Por qué `TurnoRequestDTO` no tiene `estado`?* Para que el cliente no pueda crear un turno
   ya finalizado.
4. *¿Por qué Lombok antes que MapStruct?* Para que los getters generados por `@Data` existan
   cuando MapStruct escribe el mapper.

---

# Sprint 5 — Validaciones + manejo global de errores

## Qué se construyó
Bean Validation en los 4 DTOs de entrada, `@Valid` en todos los controllers, `ErrorResponse`,
`GlobalExceptionHandler`, `TurnoSuperpuestoException`, y la eliminación de **todos** los
try-catch de los controllers.

## Lo que tenés que entender

**La regla del sprint:** si el error es del **cliente** → 4xx. Si es del **servidor** → 5xx.
Confundirlos es un error de diseño, no de implementación.

**Bean Validation necesita su propia dependencia.** El doc de la materia dice que viene con
`spring-boot-starter-web` — **eso era cierto hasta Spring Boot 2**. Desde Spring Boot 3 hay
que agregar `spring-boot-starter-validation` o las anotaciones ni compilan
(`cannot find symbol: class NotBlank`). Está en el `pom.xml` con el comentario.

**Fast fail** — con `@Valid`, si el DTO no cumple, el método del Controller **nunca se
ejecuta** y el Service nunca se llama. La base no se toca. El flujo:

```
1. Cliente envía POST /api/duenos {"nombre":"", "email":"no-es-email"}
2. Spring deserializa el JSON en DuenoDTO
3. Spring corre las validaciones (@NotBlank, @Email)
4. Fallan → lanza MethodArgumentNotValidException
5. createDueno() NUNCA se ejecuta
6. @RestControllerAdvice intercepta la excepción
7. GlobalExceptionHandler arma el ErrorResponse
8. Cliente recibe 400 + JSON con el detalle
```

**`@NotBlank` vs `@NotNull` vs `@NotEmpty`:**
- `@NotNull` → solo rechaza `null`. `""` y `" "` pasan.
- `@NotEmpty` → rechaza `null` y `""`. `" "` pasa.
- `@NotBlank` → rechaza `null`, `""` y `"   "`. Para Strings es casi siempre el correcto.

**`@PastOrPresent` vs `@Past`** — una mascota puede haber nacido hoy, así que va
`@PastOrPresent`. Mismo criterio con `@FutureOrPresent` para la fecha de un turno: se puede
agendar para hoy, no para ayer.

**`@ControllerAdvice` centraliza** — antes el mismo `catch (ResourceNotFoundException)` estaba
repetido en 4 controllers. Cambiar el formato del error significaba tocar 4 lugares. Ahora es
uno solo. Verificalo: `grep -c "try {" src/main/java/…/controller/*.java` → 0 en los cuatro.

**Por qué el handler genérico no devuelve `ex.getMessage()`** — podría filtrar detalles
internos (nombres de tablas, rutas de clases, fragmentos de SQL) al cliente. El mensaje real
va al log del servidor; al cliente se le manda un texto neutro.

**Los tres casos que en la primera pasada daban 500 y eran del cliente:**

| Caso | Antes | Ahora | Por qué |
|---|---|---|---|
| `DELETE /api/duenos/1` con turnos agendados | 500 | **409** | `Dueno.mascotas` tiene `cascade=ALL` → intenta borrar las mascotas → `turnos.mascota_id` lo bloquea. La petición es válida pero choca con el estado de los datos: eso es 409 |
| `GET /api/inexistente` | 500 | **404** | La ruta no existe |
| `DELETE /api/turnos/1` | 500 | **405** | No hay endpoint DELETE de turnos: un turno se cancela con `PATCH ?estado=CANCELADO`, no se borra, para no perder el historial clínico |

El primero es la respuesta a la pregunta abierta que dejó el doc del Sprint 3
(*"¿Qué pasa cuando un Dueño que tiene Mascotas se intenta eliminar?"*). No se resolvió con
un `cascade` hasta `turnos` a propósito: borrar un dueño no debería borrar en silencio el
historial clínico de sus mascotas.

## Dónde mirarlo
- `exception/GlobalExceptionHandler.java` — los 9 handlers, cada uno con su comentario
- `exception/ErrorResponse.java` — la estructura única
- `dto/` — las anotaciones y por qué cada una
- `pom.xml` — `spring-boot-starter-validation` con la nota
- `docs/evidencia-sprint-05.txt` — 8 casos del doc + 8 extra, ninguno da 500

## Preguntas probables
1. *¿Qué pasa si sacás `@Valid`?* Las anotaciones del DTO no tienen ningún efecto: el body
   inválido llega al Service y probablemente reviente contra la constraint de la base con un
   500.
2. *¿Por qué `@ControllerAdvice` y no try-catch?* Duplicación: el mismo catch en 4 lugares. Un
   solo punto de cambio.
3. *¿Por qué excepciones tipadas?* Para que el handler pueda distinguir un error de negocio
   de un fallo inesperado del sistema.
4. *¿`@NotNull` o `@NotBlank` para un String?* `@NotBlank`, porque `" "` es un String no nulo
   pero igualmente inválido.
5. *¿Qué código devolvés al borrar un recurso referenciado por otros?* 409 Conflict.

---

## Decisiones YAGNI tomadas

Los documentos de la materia traen código que nadie llama. Se omitió a propósito:

| Del doc | Por qué se omitió |
|---|---|
| `countByEspecie()` en `MascotaRepository` | El doc dice "útil para reportes futuros" — no hay reportes |
| `findByMascotaIdOrderByFechaDescHoraDesc()` | No hay endpoint de historial |
| `findByMatricula()` en `VeterinarioRepository` | Sin caller; el DoD solo pide `existsByMatricula()` |
| `DELETE /api/turnos/{id}` | Un turno se cancela con PATCH, no se borra |

`existsByNombreAndDuenoId()` sí se implementó **y se usa**: rechaza con 409 dos mascotas con
el mismo nombre para el mismo dueño. El DoD lo exige, y dejarlo sin llamar sería código
muerto.

---

## Cómo navegar la historia

```bash
git log --oneline                      # un commit por sprint
git show sprint-03:<ruta-del-archivo>  # ver un archivo como quedó en ese sprint
git diff sprint-03 sprint-04           # qué cambió el refactor a DTOs
git diff sprint-04 sprint-05 -- src/main/java/com/vetSystem/vet_system/controller/
```

Ese último comando es el más útil para el oral: muestra exactamente cómo desaparecieron los
try-catch de los controllers.
