# vet-system — Clínica Veterinaria "Patitas Felices"

Universidad de Palermo · Microservicios y APIs Escalables · 2026
Ing. Jorge Agustín Pereyra

Sistema de gestión de una clínica veterinaria, construido sprint a sprint desde un monolito MVC
(Fase 1) hasta una arquitectura de microservicios completa (Fase 2).

## Integrantes

- [Nombre Apellido 1] — TODO
- [Nombre Apellido 2] — TODO

## Sprint actual: Sprint 2 — Arquitectura MVC + REST + CRUD Dueño

CRUD completo de Dueño expuesto como API REST, con las tres capas separadas
(Controller → Service → Repository) y códigos HTTP correctos en cada caso.

Sprint 1 (cerrado): proyecto Spring Boot creado, entidades JPA modeladas, persistencia
verificada contra MySQL (las 4 tablas se crean solas vía `ddl-auto=update`).

## Stack

- Java 21 · Spring Boot 4.1.0
- Spring Web, Spring Data JPA (Hibernate 7), Lombok, DevTools
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
├── controller/
│   └── DuenoController.java              ← capa HTTP (Sprint 2)
├── service/
│   └── DuenoService.java                 ← lógica de negocio (Sprint 2)
├── repository/
│   └── DuenoRepository.java              ← acceso a datos (Sprint 2)
├── exception/
│   ├── ResourceNotFoundException.java    ← → HTTP 404 (Sprint 2)
│   └── DuplicateResourceException.java   ← → HTTP 409 (Sprint 2)
└── model/
    ├── Dueno.java
    ├── Mascota.java
    ├── Veterinario.java
    ├── Turno.java
    └── EstadoTurno.java
```

**Regla de las capas:** el Controller no tiene lógica de negocio, el Service no habla
directamente con la base, el Repository no valida reglas de negocio.

## API REST — Dueño

| Método | Ruta                | Éxito              | Error                          |
|--------|---------------------|--------------------|--------------------------------|
| GET    | `/api/duenos`       | `200 OK` + lista   | —                              |
| GET    | `/api/duenos/{id}`  | `200 OK` + dueño   | `404` si el id no existe       |
| POST   | `/api/duenos`       | `201 Created`      | `409` si el DNI ya está usado  |
| PUT    | `/api/duenos/{id}`  | `200 OK` + dueño   | `404` si el id no existe       |
| DELETE | `/api/duenos/{id}`  | `204 No Content`   | `404` si el id no existe       |

El `PUT` **no actualiza el DNI**: es el identificador de negocio del dueño.

### Probar la API

Colección de Postman lista para importar:
[`docs/vet-system-sprint-02.postman_collection.json`](docs/vet-system-sprint-02.postman_collection.json)
— 8 requests con tests que verifican el código HTTP de cada uno. Importala en Postman y
usá **Run collection** con la tabla `duenos` vacía.

Salida real de las 6 pruebas del sprint:
[`docs/evidencia-sprint-02.txt`](docs/evidencia-sprint-02.txt).

### Limitación conocida (se resuelve en el Sprint 3)

`GET /api/duenos` devuelve JSON recursivo si algún dueño tiene mascotas asociadas:
Jackson serializa `Dueno → mascotas → Mascota → dueno → …` hasta cortar en
`Document nesting depth (1001) exceeded`. En el Sprint 2 no se nota porque todavía no hay
CRUD de Mascota, pero si insertás una mascota a mano en MySQL, se reproduce.
Se arregla en el Sprint 3 con `@JsonIgnore` / `@JsonManagedReference`, que es justamente
uno de los temas de ese sprint.

## Definition of Done — Sprint 2

- [x] `DuenoRepository` creado con `existsByDni()` y `findByEmail()`
- [x] `ResourceNotFoundException` creada en el paquete `exception/`
- [x] `DuenoService` con los 5 métodos y validación de DNI duplicado
- [x] `DuenoController` con los 5 endpoints REST y `ResponseEntity` correcto
- [x] `GET /api/duenos` retorna HTTP 200 con la lista de dueños
- [x] `GET /api/duenos/{id}` retorna HTTP 200 si existe y HTTP 404 si no
- [x] `POST /api/duenos` retorna HTTP 201 al crear y HTTP 409 si el DNI ya existe
- [x] `PUT /api/duenos/{id}` retorna HTTP 200 con los datos actualizados
- [x] `DELETE /api/duenos/{id}` retorna HTTP 204 y HTTP 404 si no existe
- [x] Colección Postman exportada y subida a `/docs`
- [ ] Rama `sprint-02` en GitHub con PR hacia `main` — *pendiente: push + PR*

## Definition of Done — Sprint 1

- [x] Proyecto Spring Boot 4.1.0 con todas las dependencias configuradas
- [x] `application.properties` conecta a MySQL
- [x] Las 4 entidades JPA (Dueño, Mascota, Veterinario, Turno) implementadas
- [x] Enum `EstadoTurno` definido
- [x] La aplicación compila y levanta sin errores (`./mvnw spring-boot:run`)
- [x] Las 4 tablas existen en MySQL, con sus 3 foreign keys — ver `docs/evidencia-sprint-01.txt`
- [x] Endpoint de verificación `/api/duenos` responde HTTP 200
- [x] Estructura de paquetes model/repository/service/controller lista
- [x] README con instrucciones de ejecución reales y verificadas
- [x] Repositorio GitHub creado con acceso del docente
- [ ] Screenshot de las tablas subido a `/docs` — *pendiente (la evidencia en texto ya está)*
- [ ] Diagrama de clases (foto del papel) subido a `/docs` — *ya hay versión en texto en `docs/diagrama-clases.md`*

## Próximo sprint (Sprint 3)

- CRUD completo de Mascota con la relación a Dueño
- Resolver el JSON circular (`@JsonIgnore` / `@JsonManagedReference`)
- Primer contacto con JPQL para consultas personalizadas
- `GET /api/duenos/{id}/mascotas` — endpoint anidado
