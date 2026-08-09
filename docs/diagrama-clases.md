# Diagrama de clases — Sprint 1

Modelo de dominio de la Clínica Veterinaria "Patitas Felices". Reemplazar/complementar con la
foto del diagrama hecho en papel en clase (subir la imagen a esta misma carpeta `/docs`).

```
┌─────────────────┐        1        N ┌─────────────────┐
│      Dueño       │ ──────────────── │     Mascota      │
├──────────────────┤                  ├──────────────────┤
│ id               │                  │ id               │
│ nombre           │                  │ nombre           │
│ apellido         │                  │ especie          │
│ dni (unique)     │                  │ raza             │
│ telefono         │                  │ fechaNacimiento  │
│ email            │                  │ dueño (FK)       │
└──────────────────┘                  └────────┬─────────┘
                                                 │ 1
                                                 │
                                                 │ N
                                        ┌──────────────────┐
                                        │       Turno       │
                                        ├────────────────────┤
                                        │ id                 │
                                        │ fecha              │
                                        │ hora               │
                                        │ motivo             │
                                        │ estado (enum)      │
                                        │ observaciones      │
                                        │ mascota (FK)       │
                                        │ veterinario (FK)   │
                                        └──────────┬─────────┘
                                                    │ N
                                                    │
                                                    │ 1
                                        ┌──────────────────┐
                                        │   Veterinario     │
                                        ├────────────────────┤
                                        │ id                 │
                                        │ nombre             │
                                        │ apellido           │
                                        │ matricula (unique) │
                                        │ especialidad       │
                                        └────────────────────┘
```

## Relaciones

- **Dueño (1) — (N) Mascota**: un dueño puede tener muchas mascotas (`@OneToMany` en `Dueno`,
  `@ManyToOne` en `Mascota`, FK `dueno_id`).
- **Mascota (1) — (N) Turno**: una mascota puede tener muchos turnos (FK `mascota_id`).
- **Veterinario (1) — (N) Turno**: un veterinario atiende muchos turnos (FK `veterinario_id`).
- **Turno** es la entidad que conecta Mascota y Veterinario — no hay relación directa entre ellas.

## Reglas de negocio (Sprint 1)

| Entidad | Regla |
|---|---|
| Dueño | DNI único, email obligatorio |
| Mascota | Debe tener un dueño registrado |
| Veterinario | Matrícula única |
| Turno | No puede haber dos turnos del mismo veterinario a la misma hora *(se valida en Sprint 2, capa Service)* |
