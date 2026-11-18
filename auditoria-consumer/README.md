# Auditoria Consumer — Consumidor Kafka del Parqueadero FET

Servicio independiente que consume el tópico `parqueadero.movimientos`
publicado por el backend, valida y persiste cada evento en la tabla
`AUDITORIA_EVENTOS` de PostgreSQL, garantizando idempotencia por `eventId`.

Responsabilidad única (ver `../03_Kafka/eventos.md`): consumir, validar,
evitar duplicados y auditar. No modifica `MOVIMIENTO`, no calcula permanencia
ni gestiona espacios — esas operaciones ya ocurrieron en el backend antes de
publicar el evento.

## Tecnologías

- Java 21, Spring Boot 3.5, Spring for Kafka, Spring Data JPA
- PostgreSQL (misma base `parqueadero` que el backend) + Flyway
- Micrometer / Actuator (contadores de eventos procesados, duplicados e inválidos)

## Flujo

```
Kafka (parqueadero.movimientos)
        │
        ▼
MovimientoEventoListener   -- parsea JSON y valida estructura
        │
        ▼
AuditoriaEventoService      -- existsById(eventId)?
        │                        │
        │                 sí ──► duplicado: log + contador, no se persiste
        ▼ no
   INSERT AUDITORIA_EVENTOS   -- éxito: log + contador
```

### Manejo de errores

| Situación                                         | Comportamiento                                                        |
|----------------------------------------------------|------------------------------------------------------------------------|
| JSON malformado / campo obligatorio ausente / `eventType` desconocido | `EventoInvalidoException` (no reintentable) → va directo al tópico dead-letter `parqueadero.movimientos.DLT`, log `ERROR`, contador `auditoria.eventos.invalidos` |
| Error transitorio (ej. PostgreSQL momentáneamente caído) | Reintento automático: 3 intentos con 1s de espera (`FixedBackOff`); si persiste, va al dead-letter |
| Evento ya procesado (mismo `eventId`)               | Se ignora en silencio: log `INFO`, contador `auditoria.eventos.duplicados`, no se trata como error |

En todos los casos el offset avanza: un mensaje problemático nunca bloquea la
partición ni detiene el procesamiento de los eventos válidos siguientes.

### Idempotencia

`EVENT_ID` es la clave primaria de `AUDITORIA_EVENTOS`. Antes de insertar se
verifica su existencia (`existsById`); además, si dos instancias del mismo
`group.id` llegaran a procesar el mismo evento en paralelo, la violación de
la clave primaria (`DataIntegrityViolationException`) se captura y se trata
también como duplicado, no como error.

## Esquema `AUDITORIA_EVENTOS`

Ver migración Flyway `src/main/resources/db/migration/V1__auditoria_eventos.sql`.
Se usa una tabla de historial de Flyway propia
(`flyway_schema_history_auditoria`) para no interferir con las migraciones
del backend, aunque ambos servicios comparten la misma base de datos.

## Ejecución local

Requisitos: Docker Compose (PostgreSQL y Kafka en `../04_Docker/docker-compose.yml`).

```bash
# 1. Infraestructura
docker compose -f ../04_Docker/docker-compose.yml up -d postgres kafka kafka-init kafka-ui

# 2. Backend (productor real de eventos)
cd ../backend && ./gradlew bootRun

# 3. Este consumidor
cd ../auditoria-consumer && ./gradlew bootRun
```

El servicio arranca en `http://localhost:8081` (`/actuator/health`,
`/actuator/metrics/auditoria.eventos.procesados`,
`/actuator/metrics/auditoria.eventos.duplicados`,
`/actuator/metrics/auditoria.eventos.invalidos`).

## Demostración manual de idempotencia y errores

Con la infraestructura levantada, se puede publicar eventos a mano dentro del
contenedor de Kafka usando el productor de consola (usando como clave el
mismo `eventId` del JSON para conservar el orden por partición):

```bash
docker exec -it fet-kafka /opt/kafka/bin/kafka-console-producer.sh \
  --broker-list localhost:9092 \
  --topic parqueadero.movimientos \
  --property "parse.key=true" --property "key.separator=:"
```

Y luego pegar (usando el ejemplo de `../03_Kafka/ejemplos_eventos.json`):

```
550e8400-e29b-41d4-a716-446655440000:{"eventId":"550e8400-e29b-41d4-a716-446655440000","eventType":"VEHICULO_INGRESADO","timestamp":"2026-08-24T08:30:00-05:00","movimientoId":100,"vehiculoId":25,"placa":"ABC123","espacioId":8,"espacioNumero":"E-08","tiempoPermanenciaMinutos":null}
```

- Pegar la misma línea dos veces → en los logs del consumidor aparece una
  sola vez "persistido" y la segunda vez "evento duplicado ignorado"; en
  `AUDITORIA_EVENTOS` solo hay una fila.
- Pegar una línea con JSON corrupto (p. ej. `abc:{esto no es json}`) → log
  `ERROR` y el mensaje aparece en el tópico `parqueadero.movimientos.DLT`
  (visible en Kafka UI, `http://localhost:8090`); un evento válido enviado a
  continuación se sigue procesando con normalidad.

## Pruebas

```bash
./gradlew test
```

Incluye pruebas unitarias de validación e idempotencia, y una prueba de
integración extremo a extremo con `@EmbeddedKafka` que publica eventos reales
y verifica persistencia, deduplicación y enrutamiento al dead-letter.

## Docker

```bash
docker build -t fet-auditoria-consumer .
```

El `Dockerfile` es el referenciado por `../04_Docker/docker-compose.yml`
(servicio `auditoria-consumer`).
