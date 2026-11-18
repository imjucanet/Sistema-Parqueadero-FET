# Kafka - Contrato inicial de eventos

## Tópico

`parqueadero.movimientos`

El tópico transporta los eventos operacionales generados por el backend.

## Productor

**Spring Boot Backend**

Flujo:

`Caso de uso -> EventPublisherPort -> KafkaProducerAdapter -> Kafka`

El backend publica el evento únicamente después de que la operación principal haya sido validada y persistida.

## Consumidor

**auditoria-consumer**

Responsabilidad única del MVP:

- consumir eventos;
- validar su estructura;
- evitar duplicidad mediante `eventId`;
- persistir el resultado en `AUDITORIA_EVENTOS`.

No modifica `MOVIMIENTO`, no calcula permanencia y no gestiona espacios.

## Tipos de evento

### VEHICULO_INGRESADO

Se genera después de:

1. validar el vehículo;
2. encontrar un espacio disponible;
3. registrar el movimiento;
4. ocupar el espacio.

### VEHICULO_SALIDO

Se genera después de:

1. localizar el movimiento activo;
2. registrar la salida;
3. calcular la permanencia;
4. liberar el espacio.

## Estructura común

```json
{
  "eventId": "UUID",
  "eventType": "VEHICULO_INGRESADO",
  "timestamp": "2026-08-24T08:30:00-05:00",
  "movimientoId": 100,
  "vehiculoId": 25,
  "placa": "ABC123",
  "espacioId": 8,
  "espacioNumero": "E-08",
  "tiempoPermanenciaMinutos": null
}
```

Para `VEHICULO_SALIDO`, `tiempoPermanenciaMinutos` contiene el valor calculado.

## Idempotencia

`eventId` es único (`VARCHAR(36)`, clave primaria de `AUDITORIA_EVENTOS`). El consumidor verifica si el evento ya fue procesado (`existsById`) antes de insertar; si la inserción choca igualmente con la clave primaria (carrera entre instancias del mismo `group.id`), la violación se captura y se trata también como duplicado. Un duplicado se registra en el log y en un contador, pero no se considera un error.

## Fuente de verdad

PostgreSQL mantiene los datos operacionales. Kafka comunica hechos ocurridos y la tabla `AUDITORIA_EVENTOS` almacena información derivada.

## Implementación del consumidor

El consumidor vive en `../auditoria-consumer` (servicio independiente, detalle en su `README.md`).

### Manejo de errores

| Situación | Comportamiento |
|---|---|
| JSON malformado, campo obligatorio ausente o `eventType` desconocido | No reintentable: se envía directo al tópico dead-letter `parqueadero.movimientos.DLT` |
| Error transitorio (ej. PostgreSQL no disponible momentáneamente) | 3 reintentos con 1s de espera fija; si persiste, también va al dead-letter |

En ambos casos el offset avanza: un mensaje problemático no bloquea la partición ni detiene el procesamiento de los eventos válidos siguientes. Cada caso queda registrado en el log (`INFO` para éxito/duplicado, `ERROR` para lo que termina en el dead-letter) y expuesto como contador Micrometer (`auditoria.eventos.procesados`, `auditoria.eventos.duplicados`, `auditoria.eventos.invalidos`) vía `/actuator/metrics`.
