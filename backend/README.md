# Backend Sistema Parqueadero — APIs REST y GraphQL del Parqueadero FET

Sistema de gestión de parqueadero institucional de la 
Fundación Escuela Tecnológica de Neiva "Jesús Oviedo Pérez".

Implementa, con arquitectura hexagonal, **dos adaptadores de entrada sobre los
mismos casos de uso**:

- **REST**, definida en el contrato OpenAPI (`src/main/resources/api/openapi.yaml`).
- **GraphQL**, definida en el esquema (`src/main/resources/graphql/schema.graphqls`),
  con queries, mutations y una subscription en tiempo real.

Ninguna regla de negocio vive en los adaptadores: ambos delegan en los mismos
`Gestion*UseCase`.

## Tecnologías Utilizadas

- Java 21
- Spring Boot 3.5 + Spring Web, Spring Data JPA y Spring for Kafka
- Spring for GraphQL (+ WebSocket para subscriptions) y graphql-java-extended-scalars
- PostgreSQL (fuente de verdad operacional)
- Flyway (versionado y migración del esquema de base de datos)
- Apache Kafka (tópico `parqueadero.movimientos`)
- openapi-generator (generación de interfaces REST y DTOs a partir del contrato)
- Bean Validation (Jakarta Validation) + manejo global de errores
- Springdoc / Swagger UI


## Estructura del Proyecto

```text
backend/
├── src/main/resources/api/openapi.yaml   # contrato único de la API REST
├── src/main/java/com/fet/parqueadero/
│   ├── domain/                            # núcleo de dominio (modelo y reglas)
│   │   ├── model/                         # records y enums de dominio 
│   │   ├── service/                       # implementación de casos de uso
│   │   └── exception/                     # excepciones de negocio
│   ├── application/port/                  # contratos hexagonales
│   │   ├── in/                            # casos de uso (Gestion*UseCase)
│   │   └── out/                           # repositorios y publicación de eventos
│   ├── adapter/in/rest/                   # API REST
│   │   ├── api/  y  dto/                  # GENERADOS por openapi-generator
│   │   ├── controller/                    # controladores que implementan las interfaces
│   │   ├── mapper/                        # conversión DTO <-> dominio
│   │   └── GlobalExceptionHandler.java    # errores uniformes (ErrorResponse)
│   ├── adapter/in/graphql/                # API GraphQL
│   │   ├── controller/                    # @QueryMapping / @MutationMapping / @BatchMapping
│   │   ├── dto/                           # inputs validados y tipos de salida
│   │   ├── GraphQLExceptionResolver.java  # errores con el mismo `codigo` que REST
│   │   ├── GraphQlScalarsConfig.java      # scalar DateTime
│   │   ├── MovimientoEventPublisher.java  # alimenta la subscription
│   │   └── Paginacion.java                # argumentos pagina/tamano -> Pageable
│   ├── adapter/out/persistence/           # JPA: entities, mappers, repos, adapters
│   ├── adapter/out/kafka/                 # publicador de eventos VEHICULO_*
│   └── config/                            # inicializador de espacios
├── src/main/resources/graphql/            # esquema GraphQL (schema.graphqls)
├── src/main/resources/db/migration/       # scripts Flyway (Vn__descripcion.sql)
└── src/main/resources/application.yml
```

## Migraciones de base de datos

El esquema de PostgreSQL se versiona con Flyway (`src/main/resources/db/migration`).
Hibernate ya **no** crea ni modifica tablas (`ddl-auto: validate`): al arrancar,
Flyway aplica las migraciones pendientes y Hibernate solo valida que las
entidades coincidan con el esquema resultante.

Para agregar un cambio de esquema, crear un archivo nuevo
`Vn__descripcion.sql` (siguiente número de versión) en ese directorio; nunca
editar una migración ya aplicada. En pruebas se usa H2 con
`ddl-auto: create-drop` y Flyway deshabilitado (`application-test.yml`), por
lo que no requieren PostgreSQL.

## Código generado

Los DTOs y las interfaces de los controladores se generan desde
`src/main/resources/api/openapi.yaml` con el plugin `org.openapi.generator`:

```bash
./gradlew openApiGenerate        # genera en build/generated/openapi
./gradlew build                  # genera y compila
./gradlew bootRun                # genera, compila y ejecuta en :8080
```

Los DTOs/interfaces no se editan manualmente, si cambia el contrato, se regenera
este código manteniendo el resto de la implementación.

## Invariantes de arquitectura que cumple

1. Los controladores no contienen reglas de negocio: delegan en los casos de uso
   (`application/port/in`). Esto vale igual para REST y para GraphQL, que son dos
   adaptadores de entrada hermanos sobre el mismo núcleo.
2. Las capas de entrada (DTOs REST, inputs GraphQL) están desacopladas del dominio
   y de la persistencia (entidades).
3. El dominio no conoce ningún framework de transporte: `domain/model` son records
   y enums sin anotaciones de Spring, JPA ni GraphQL.
4. Los eventos `VEHICULO_INGRESADO` / `VEHICULO_SALIDO` se publican **después**
   de validar y persistir la operación (`@TransactionalEventListener(AFTER_COMMIT)`).
   La subscription GraphQL se engancha a ese mismo evento, así que tampoco puede
   emitir un movimiento que no esté confirmado en la base de datos.
5. PostgreSQL es la fuente de verdad; Kafka solo comunica hechos ocurridos.

## Ejecución local

Requisitos: Docker Compose (PostgreSQL y Kafka en `../04_Docker/docker-compose.yml`).

```bash
# 1. Infraestructura (PostgreSQL + Kafka + tópico)
docker compose -f ../04_Docker/docker-compose.yml up -d postgres kafka kafka-init

# 2. Backend
./gradlew bootRun
```

El backend arranca por defecto en `http://localhost:8080`:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API docs JSON: `http://localhost:8080/v3/api-docs`
- GraphiQL (explorador GraphQL): `http://localhost:8080/graphiql`
- Endpoint GraphQL: `http://localhost:8080/graphql` (y `ws://localhost:8080/graphql`
  para la subscription)

Al arrancar se siembran espacios base (`parqueadero.espacios-iniciales`, por
defecto 20 de `E-01` a `E-20`) para tener datos listos la primera vez; después se
pueden agregar más con `POST /api/espacios` o la mutation `registrarEspacio`.

## Endpoints (contrato)

| Método | Ruta                        | Operación                  |
|--------|-----------------------------|----------------------------|
| POST   | `/api/propietarios`         | registrarPropietario       |
| GET    | `/api/propietarios`         | listarPropietarios         |
| GET    | `/api/propietarios/{id}`    | obtenerPropietario         |
| PUT    | `/api/propietarios/{id}`    | actualizarPropietario      |
| POST   | `/api/vehiculos`            | registrarVehiculo          |
| GET    | `/api/vehiculos`            | listarVehiculos            |
| GET    | `/api/vehiculos/{id}`       | obtenerVehiculo            |
| PUT    | `/api/vehiculos/{id}`       | actualizarVehiculo         |
| GET    | `/api/espacios`             | listarEspacios             |
| GET    | `/api/espacios/disponibles` | listarEspaciosDisponibles  |
| POST   | `/api/movimientos/entrada`  | registrarEntrada           |
| POST   | `/api/movimientos/{id}/salida` | registrarSalida         |
| GET    | `/api/movimientos/activos`  | listarMovimientosActivos   |
| GET    | `/api/movimientos/historial`| listarHistorialMovimientos |

## API GraphQL

Esquema: `src/main/resources/graphql/schema.graphqls` (copia entregable en
`../02_GraphQL/schema.graphql`). Explorador interactivo en `/graphiql`.

### Operaciones

| Tipo | Operación | Equivalente REST |
|------|-----------|------------------|
| Query | `propietario(id)` / `propietarios(pagina, tamano)` | `GET /api/propietarios[/{id}]` |
| Query | `vehiculo(id)` / `vehiculos(pagina, tamano)` | `GET /api/vehiculos[/{id}]` |
| Query | `marcas` | `GET /api/vehiculos/marcas` |
| Query | `espacio(id)` / `espacios(pagina, tamano)` | `GET /api/espacios` |
| Query | `espaciosDisponibles(pagina, tamano)` | `GET /api/espacios/disponibles` |
| Query | `movimiento(id)` | — |
| Query | `movimientosActivos(pagina, tamano)` | `GET /api/movimientos/activos` |
| Query | `historialMovimientos(pagina, tamano)` | `GET /api/movimientos/historial` |
| Mutation | `registrarPropietario` / `actualizarPropietario` | `POST`/`PUT /api/propietarios` |
| Mutation | `registrarVehiculo` / `actualizarVehiculo` | `POST`/`PUT /api/vehiculos` |
| Mutation | `registrarVehiculosEnLote` | `POST /api/vehiculos/lote` |
| Mutation | `registrarEspacio` | `POST /api/espacios` |
| Mutation | `registrarEntrada` / `registrarSalida` | `POST /api/movimientos/...` |
| Subscription | `movimientoRegistrado` | — (equivale al tópico Kafka) |

`pagina` y `tamano` son opcionales: si se omiten ambos se devuelven todos los
registros; si se envía solo `pagina`, `tamano` vale 20.

### Decisiones de diseño

- **Un solo núcleo de negocio.** Los controladores GraphQL no replican reglas:
  llaman a los mismos `Gestion*UseCase` que usan los controladores REST.
- **Errores con el mismo vocabulario.** `GraphQLExceptionResolver` traduce las
  excepciones de dominio a errores GraphQL con `extensions.codigo`, usando los
  mismos códigos de la tabla de abajo (`VEHICULO_YA_EXISTE`, `ESPACIO_NO_DISPONIBLE`, …).
- **Entradas validadas.** Los inputs llevan Bean Validation con las longitudes de
  las tablas, así un dato demasiado largo responde `BAD_REQUEST` con código
  `VALIDACION` en lugar de llegar al INSERT y convertirse en `ERROR_INTERNO`.
- **Sin N+1.** `Propietario.vehiculos`, `Vehiculo.propietario`, `Movimiento.vehiculo`
  y `Movimiento.espacio` se resuelven con `@BatchMapping`: una consulta por campo
  y por petición, no una por elemento.
- **`DateTime` real.** Las fechas usan un scalar ISO-8601, no `String`.
- **Subscription consistente con Kafka.** `MovimientoEventPublisher` escucha el
  mismo evento de aplicación que el productor Kafka, con
  `@TransactionalEventListener(AFTER_COMMIT)`: solo emite después de confirmar en
  PostgreSQL. El sink es `directBestEffort` (entrega hechos en vivo; para lo ya
  ocurrido está `historialMovimientos`).

### Ejemplos

Consulta anidada (una sola petición, en vez de tres llamadas REST):

```graphql
query {
  propietarios(pagina: 0, tamano: 10) {
    nombreCompleto
    vehiculos { placa marca }
  }
}
```

Subscription en tiempo real:

```graphql
subscription {
  movimientoRegistrado {
    estado
    fechaEntrada
    vehiculo { placa }
    espacio { numero }
  }
}
```

## Contratos sincronizados

Los contratos que carga la aplicación viven en `src/main/resources`; las copias de
`../01_OpenAPI/` y `../02_GraphQL/` son los entregables del taller. Dos tareas de
Gradle las mantienen iguales y corren solas con `processResources` (por tanto en
`build`, `test` y `bootRun`):

```bash
./gradlew sincronizarEsquemaGraphQL   # -> ../02_GraphQL/schema.graphql
./gradlew sincronizarContratoOpenAPI  # -> ../01_OpenAPI/openapi.yaml
```

Se omiten si esas carpetas no existen, como dentro del build de Docker, cuyo
contexto es solo `backend/`.

## Limitación conocida: rutas con acentos en Windows

`./gradlew test` **falla con `ClassNotFoundException` en todas las clases de
prueba** si la ruta del proyecto contiene caracteres no ASCII (por ejemplo la `Ó`
de "PROGRAMACIÓN"). No es un problema del código: Gradle escribe el classpath del
worker de pruebas en un *argfile* en UTF-8, pero el lanzador de Java en Windows lo
interpreta con la codepage ANSI del sistema; la ruta se corrompe, la entrada
`build/classes/java/test` se cae del classpath y ninguna clase de prueba se
encuentra.

La compilación y `bootRun` no se ven afectados. Para ejecutar las pruebas, clonar
el repositorio en una ruta sin acentos (por ejemplo `D:\dev\parqueadero`).

## Codigos HTTP   

La API REST utiliza códigos HTTP de acuerdo con el resultado de cada operación.
Las respuestas de error mantienen una estructura uniforme mediante el DTO
`ErrorResponse` compuesto por los campos (`codigo`, `mensaje`).
:

| Código                        | HTTP | Escenario                                   |
|-------------------------------|------|---------------------------------------------|
| `VALIDACION`                  | 400  | Bean Validation fallida                     |
| `SOLICITUD_INVALIDA`          | 400  | Cuerpo no legible / tipo inválido           |
| `PROPIETARIO_NO_ENCONTRADO`   | 400  | Vehículo apunta a propietario inexistente   |
| `VEHICULO_NO_ENCONTRADO`      | 400  | Entrada con vehículo inexistente            |
| `RECURSO_NO_ENCONTRADO`       | 404  | `GET/PUT /{id}` o salida de movimiento inexistente |
| `ESPACIO_NO_DISPONIBLE`       | 409  | No hay espacios disponibles                 |
| `VEHICULO_YA_EN_PARQUEADERO`  | 409  | El vehículo ya está dentro                  |
| `MOVIMIENTO_NO_ACTIVO`        | 409  | Salida de un movimiento no activo           |
| `ERROR_INTERNO`               | 500  | Error no controlado                         |

## Docker

```bash
docker build -t fet-backend .
```

El `Dockerfile` es el referenciado por `../04_Docker/docker-compose.yml`
(servicio `backend`). Corre `./gradlew bootJar` en una imagen `gradle` y ejecuta
el artefacto en una imagen JRE.