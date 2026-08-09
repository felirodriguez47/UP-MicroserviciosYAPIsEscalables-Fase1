# vet-system — Clínica Veterinaria "Patitas Felices"

Universidad de Palermo · Microservicios y APIs Escalables · 2026
Ing. Jorge Agustín Pereyra

Sistema de gestión de una clínica veterinaria, construido sprint a sprint desde un monolito MVC
(Fase 1) hasta una arquitectura de microservicios completa (Fase 2).

## Integrantes

- [Nombre Apellido 1] — TODO
- [Nombre Apellido 2] — TODO

## Sprint actual: Sprint 1 — Kickoff + Dominio + Setup

Proyecto Spring Boot creado, entidades JPA modeladas, persistencia verificada contra MySQL
(las 4 tablas se crean solas vía `ddl-auto=update`) y endpoint de verificación respondiendo.

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

   Respuesta esperada: `vet-system OK - Sprint 1. CRUD de duenos: Sprint 2.`

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
├── model/          ← entidades JPA (Sprint 1)
├── repository/     ← interfaces JPA (Sprint 2)
├── service/        ← lógica de negocio (Sprint 2)
└── controller/     ← endpoints REST (Sprint 1: solo el de verificación)
```

## Endpoints

| Método | Ruta          | Descripción                                     | Sprint |
|--------|---------------|-------------------------------------------------|--------|
| GET    | `/api/duenos` | Verificación de que el contexto Spring levanta   | 1      |

El CRUD completo de Dueño se implementa en el Sprint 2.

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
- [ ] Repositorio GitHub creado con la rama `sprint-01` y acceso del docente — *pendiente*
- [ ] Screenshot de las tablas subido a `/docs` — *pendiente (la evidencia en texto ya está)*
- [ ] Diagrama de clases (foto del papel) subido a `/docs` — *ya hay versión en texto en `docs/diagrama-clases.md`*

## Próximo sprint (Sprint 2)

- Patrón Repository para cada entidad
- Capa Service con lógica de negocio
- CRUD completo de Dueño expuesto como REST
- Pruebas con Postman (GET, POST, PUT, DELETE)
