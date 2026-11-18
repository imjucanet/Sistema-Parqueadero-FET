# Sistema de Gestión de Parqueadero Institucional — FET

Proyecto de la asignatura **Programación Avanzada** de la Fundación Escuela
Tecnológica de Neiva "Jesús Oviedo Pérez" (Grupo 1 — Equipo 1).

Sistema distribuido para la gestión del parqueadero institucional: registro de
propietarios y vehículos, control de espacios, y registro de entradas y salidas,
con propagación asíncrona de los hechos operacionales hacia un servicio
independiente de auditoría.

El núcleo de negocio es **uno solo** y se expone por dos adaptadores de entrada
hermanos (REST y GraphQL) bajo **arquitectura hexagonal**: ninguna regla de
negocio vive en los adaptadores.

## Arquitectura

```
                  ┌──────────────────────────┐
   Navegador ───► │  frontend (React + Vite) │
                  └───────┬──────────┬───────┘
                   REST   │          │  GraphQL (HTTP + WebSocket)
                          ▼          ▼
            ┌─────────────────────────────────────────┐
            │              backend                    │
            │  adapter/in/rest     adapter/in/graphql │
            │              ╲        ╱                 │
            │        application/port/in (casos de uso)│
            │                   │                     │
            │              domain (modelo y reglas)   │
            │                   │                     │
            │   adapter/out/persistence   adapter/out/kafka
            └───────┬───────────────────────────┬─────┘
                    ▼                           ▼
            ┌──────────────┐          ┌───────────────────────┐
            │  PostgreSQL  │          │ Kafka                 │
            │ (verdad      │          │ parqueadero.movimientos│
            │  operacional)│          └───────────┬───────────┘
            └──────┬───────┘                      │
                   │                              ▼
                   │              ┌──────────────────────────────┐
                   └──────────────┤  auditoria-consumer          │
                 AUDITORIA_EVENTOS│  valida, deduplica, audita   │
                                  └──────────────────────────────┘
```

## Estructura del repositorio

| Carpeta | Contenido |
|---------|-----------|
| `01_OpenAPI/` | Contrato entregable de la API REST (`openapi.yaml`, OpenAPI 3.0.3, 17 operaciones) |
| `02_GraphQL/` | Esquema entregable de la API GraphQL (`schema.graphql`, 11 queries, 8 mutations, 1 subscription) |
| `03_Kafka/` | Contrato de eventos del tópico `parqueadero.movimientos` y ejemplos de carga útil |
| `04_Docker/` | `docker-compose.yml`: PostgreSQL, Kafka, creación del tópico, Kafka UI, backend y consumidor |
| `05_Evidencias/` | Reportes JUnit de las pruebas, logs de verificación del frontend, entorno de ejecución y evidencias del flujo Kafka |
| `backend/` | Servicio Spring Boot: API REST, API GraphQL y productor de eventos ([README](backend/README.md)) |
| `auditoria-consumer/` | Servicio Spring Boot independiente: consumidor Kafka de auditoría ([README](auditoria-consumer/README.md)) |
| `frontend/` | Cliente React + Vite que consume ambas APIs ([README](frontend/README.md)) |

El informe final del proyecto es
`Informe_Final_Sistema_Parqueadero_FET.docx` (raíz del repositorio).

## Tecnologías

| Capa | Tecnología |
|------|-----------|
| Lenguaje backend | Java 21 |
| Framework | Spring Boot 3.5.16 (Web, Data JPA, Validation, GraphQL, WebSocket, Kafka) |
| Contrato REST | OpenAPI 3.0.3 + openapi-generator 7.14.0 (contract-first) |
| Contrato GraphQL | Spring for GraphQL + graphql-java-extended-scalars 24.0 |
| Persistencia | PostgreSQL 16, Flyway (`ddl-auto: validate`) |
| Mensajería | Apache Kafka 4.0.0 |
| Frontend | React 19, Vite 8, oxlint (sin dependencias de cliente GraphQL) |
| Infraestructura | Docker Compose |
| Build | Gradle 8.14.2 (wrapper incluido) |

## Puesta en marcha

### Opción A — Todo con Docker Compose

```bash
cd 04_Docker
docker compose up -d --build
```

Servicios expuestos:

| Servicio | URL |
|----------|-----|
| Backend (REST + GraphQL) | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| GraphiQL | http://localhost:8080/graphiql |
| Consumidor de auditoría | http://localhost:8081/actuator/health |
| Kafka UI | http://localhost:8090 |
| PostgreSQL | `localhost:5433` (base `parqueadero`) |

### Opción B — Infraestructura en Docker y servicios en local

```bash
# 1. Infraestructura
docker compose -f 04_Docker/docker-compose.yml up -d postgres kafka kafka-init kafka-ui

# 2. Backend  (puerto 8080)
cd backend && ./gradlew bootRun

# 3. Consumidor de auditoría  (puerto 8081)
cd auditoria-consumer && ./gradlew bootRun

# 4. Frontend  (puerto 5173)
cd frontend && npm install && npm run dev
```

> No se puede correr el backend local y su contenedor a la vez: ambos usan el
> puerto `8080`.

## Pruebas

```bash
cd backend            && ./gradlew test   # 68 casos
cd auditoria-consumer && ./gradlew test   #  9 casos
cd frontend           && npm run lint && npm run build
```

Resultado de la última ejecución (2026-11-18): **77 casos, 0 fallos, 0 errores**.
El detalle por caso está en `05_Evidencias/resumen_pruebas.txt` y los reportes
HTML de Gradle en `05_Evidencias/pruebas_*/reporte_html/index.html`.

> **Windows — rutas con acentos.** `./gradlew test` falla con
> `ClassNotFoundException` en todas las clases de prueba si la ruta del
> repositorio contiene caracteres no ASCII. Es un defecto de codificación del
> argfile del worker de pruebas de Gradle en Windows, no del código: clonar en
> una ruta ASCII (p. ej. `D:\dev\parqueadero`) y las pruebas pasan sin cambios.
> Detalle en `05_Evidencias/entorno_ejecucion.txt`.

## Decisiones de diseño destacadas

1. **Un solo núcleo de negocio, dos adaptadores de entrada.** Los controladores
   REST y GraphQL delegan en los mismos `Gestion*UseCase`; el dominio no conoce
   ningún framework de transporte.
2. **Contract-first.** Las interfaces y DTOs REST se generan desde
   `openapi.yaml`; no se editan a mano. Dos tareas de Gradle mantienen
   sincronizadas las copias entregables de `01_OpenAPI/` y `02_GraphQL/`.
3. **Esquema versionado.** Flyway es el único que crea o modifica tablas;
   Hibernate solo valida (`ddl-auto: validate`).
4. **Eventos solo después del commit.** Tanto el productor Kafka como la
   subscription GraphQL se enganchan a
   `@TransactionalEventListener(AFTER_COMMIT)`: no se puede anunciar un
   movimiento que no esté confirmado en PostgreSQL.
5. **Idempotencia en el consumidor.** `eventId` es clave primaria de
   `AUDITORIA_EVENTOS`; se verifica antes de insertar y la violación de clave
   por carrera entre instancias también se trata como duplicado, no como error.
6. **Sin N+1 en GraphQL.** Las relaciones se resuelven con `@BatchMapping`: una
   consulta por campo y por petición.
7. **Errores con un vocabulario único.** REST y GraphQL devuelven los mismos
   códigos de negocio (`VEHICULO_YA_EN_PARQUEADERO`, `ESPACIO_NO_DISPONIBLE`, …).

## Documentación detallada

- [`backend/README.md`](backend/README.md) — arquitectura, endpoints, operaciones GraphQL, códigos de error, migraciones
- [`auditoria-consumer/README.md`](auditoria-consumer/README.md) — flujo del consumidor, manejo de errores, dead-letter, métricas
- [`frontend/README.md`](frontend/README.md) — cliente REST y GraphQL, subscription sobre WebSocket
- [`03_Kafka/eventos.md`](03_Kafka/eventos.md) — contrato de eventos, idempotencia y fuente de verdad
