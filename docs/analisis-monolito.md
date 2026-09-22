# Análisis del monolito — base para la migración a microservicios

vet-system · Cierre de la Fase 1 · Sprint 7

## Estado actual

Un solo proceso Spring Boot (un único `.jar`) con cuatro módulos lógicos — Dueños,
Mascotas, Veterinarios y Turnos — que comparten:

- **el proceso:** un solo `VetSystemApplication`, una sola JVM
- **la base:** una sola base MySQL `vet_system` con FKs cruzadas entre los cuatro módulos
- **el código:** un solo repositorio y un solo `pom.xml`
- **el deploy:** cualquier cambio implica recompilar y reiniciar todo

Para el volumen actual de la clínica este diseño es **correcto**: una sola unidad de deploy,
transacciones ACID locales, joins directos, debugging simple y 13 tests que corren en segundos.
El análisis no dice que el monolito esté mal. Dice **dónde deja de alcanzar** cuando el
sistema crece.

---

## 1. ¿Qué pasa si solo queremos escalar el módulo de Turnos por un pico de demanda?

**Problema — escalabilidad acoplada.** No se puede escalar una parte del proceso. La única
opción es escalar horizontalmente la **aplicación entera**: levantar N copias del `.jar`
completo detrás de un balanceador.

**En la clínica:** en campaña de vacunación, `POST /api/turnos` y `GET /api/turnos/agenda`
reciben 20 veces más tráfico, mientras que el ABM de veterinarios casi no se usa. Para
atender Turnos hay que replicar también Dueños, Mascotas y Veterinarios: cada réplica carga
memoria, pool de conexiones y contexto de Spring de módulos que no la necesitan. Además, todas
las réplicas golpean **la misma base MySQL**, así que el cuello de botella real no se mueve:
escalar la aplicación multiplica las conexiones contra una única base.

**Fase 2:** Turnos pasa a ser un microservicio propio que se escala solo (más instancias
registradas en **Eureka**, balanceadas por el **API Gateway**). Con base propia, la carga de
turnos deja de competir con el resto.

---

## 2. ¿Qué pasa si el módulo de Veterinarios falla por un bug? ¿Afecta a los demás?

**Problema — falla total (no hay aislamiento de fallos).** Todos los módulos comparten
proceso, heap, hilos de Tomcat y pool de conexiones (HikariCP, 10 por defecto).

**En la clínica:** un bug en `VeterinarioService` que genera un bucle o una consulta lenta
sin índice puede:
- ocupar los hilos de Tomcat → los requests de Dueños y Turnos quedan en cola;
- agotar el pool de conexiones → `GET /api/duenos` falla con timeout aunque su código esté bien;
- producir un `OutOfMemoryError` → **se cae la JVM y con ella las cuatro APIs**.

Un bug en el módulo menos usado deja a la clínica sin poder agendar turnos.

**Fase 2:** cada microservicio corre en su propio proceso. Si Veterinarios cae, Dueños y
Mascotas siguen funcionando. **Resilience4J** (circuit breaker) evita que Turnos, que llama a
Veterinarios, quede colgado esperando: corta el circuito y responde un fallback.

---

## 3. ¿Qué pasa si queremos MongoDB para el historial de turnos y MySQL para los dueños?

**Problema — base de datos y tecnología únicas.** Todas las entidades viven en el mismo
esquema relacional y están unidas por FKs (`turnos.mascota_id → mascotas`,
`mascotas.dueno_id → duenos`). Todo el código usa el mismo stack de persistencia (JPA/Hibernate).

**En la clínica:** el historial clínico de un turno (observaciones, estudios, adjuntos,
indicaciones que varían por especie) es semiestructurado y se lee mucho más de lo que se
escribe: encaja mejor en un documento de MongoDB que en columnas fijas de MySQL. Los dueños,
en cambio, son datos relacionales con unicidad fuerte (DNI) y sí conviene mantenerlos en
MySQL. En el monolito, mover Turnos a MongoDB rompe las FKs, las consultas JPA y las
transacciones que hoy abarcan ambas tablas. Ya vimos que esas FKs condicionan el diseño: son
la razón de que `DELETE /api/duenos/{id}` devuelva 409 cuando hay turnos.

**Fase 2:** *database per service*. Cada microservicio elige su motor (**MySQL** para Dueños,
**MongoDB** para el historial, **Redis** como caché). A cambio se pierden los joins y las
transacciones entre servicios: la integridad entre servicios se valida por API (**OpenFeign**)
y se acepta consistencia eventual.

---

## 4. ¿Qué pasa si dos equipos trabajan en paralelo sobre el mismo repositorio?

**Problema — equipos acoplados.** Un solo repositorio, un solo `pom.xml`, un solo
`GlobalExceptionHandler`, un solo `application.properties`, y módulos que se conocen entre sí
por código (`TurnoService` inyecta directamente `MascotaRepository` y `VeterinarioRepository`).

**En la clínica:** un equipo agrega recordatorios de turnos y otro rediseña Mascotas. Si el
segundo cambia un campo de `Mascota`, el `TurnoMapper` (que lee `mascota.nombre`) y los
tests del primer equipo se rompen. Los dos editan `pom.xml` y `GlobalExceptionHandler` →
conflictos de merge constantes. Nadie puede subir una versión de MapStruct o de Spring sin
coordinar con el resto. Y la suite de tests de todo el sistema bloquea cada merge.

**Fase 2:** cada microservicio es un repositorio (o módulo) con dueño claro. Los equipos se
coordinan por **contratos de API** (documentados con OpenAPI, como hicimos en este sprint),
no por código compartido. Mientras el contrato no cambie, cada equipo evoluciona a su ritmo.

---

## 5. ¿Qué pasa si queremos deployar solo una actualización del módulo de Mascotas?

**Problema — deploy monolítico.** La unidad de deploy es el `.jar` completo. Un cambio de una
línea en `MascotaService` implica recompilar, volver a testear y reiniciar **todo el sistema**.

**En la clínica:** corregir un texto de validación en Mascotas obliga a reiniciar la
aplicación. Durante el reinicio (en nuestra máquina, ~2 s de arranque; en producción con más
carga, bastante más) la clínica no puede agendar turnos ni consultar dueños. El riesgo de cada
deploy es el del sistema entero: si el `.jar` nuevo trae un error en otro módulo, el rollback
también es total. Esto empuja a deployar poco y en lotes grandes, lo que aumenta todavía más
el riesgo de cada deploy.

**Fase 2:** cada microservicio se construye, versiona y deploya por separado (contenedores con
**Docker Compose**). Actualizar Mascotas reinicia solo Mascotas; con varias instancias en
Eureka, ni siquiera hay corte.

---

## Resumen

| Pain point | Síntoma en vet-system | Respuesta en la Fase 2 |
|---|---|---|
| Escalabilidad acoplada | Escalar Turnos replica todo el `.jar` y la base sigue siendo una | MS de Turnos escalado solo, Eureka + Gateway |
| Falla total | Un bug en Veterinarios agota hilos/conexiones o tira la JVM | Procesos separados + Resilience4J |
| Base y tecnología únicas | FKs cruzadas impiden MongoDB para el historial | Database per service (MySQL, MongoDB, Redis) |
| Equipos acoplados | Mismo `pom.xml`, mismo handler, Services que se inyectan entre sí | Repos por servicio, contratos OpenAPI |
| Deploy monolítico | Cambiar Mascotas reinicia todo | Deploy independiente con Docker |

## El costo de migrar

Migrar no es gratis, y la justificación tiene que incluirlo:

- **Red en vez de llamadas en memoria:** latencia, timeouts y fallos parciales que hoy no existen.
- **Sin transacciones entre servicios:** crear un turno validando mascota y veterinario deja de
  ser una transacción ACID; hay que aceptar consistencia eventual o usar patrones como Saga.
- **Más infraestructura:** Eureka, Config Server, Gateway, un proceso y una base por servicio.
- **Observabilidad:** un error atraviesa varios servicios; hacen falta logs centralizados y trazas.
- **Seguridad distribuida:** la autenticación tiene que viajar entre servicios (JWT).

**Conclusión:** para la clínica de hoy el monolito es suficiente. La migración se justifica si
aparecen la carga desigual entre módulos, la necesidad de persistencia distinta por módulo o
varios equipos trabajando en paralelo. La estrategia será incremental con el **Strangler Fig
Pattern**: el Gateway enruta de a un módulo por vez hacia el nuevo microservicio, mientras el
monolito sigue atendiendo el resto, sin una reescritura "big bang".
