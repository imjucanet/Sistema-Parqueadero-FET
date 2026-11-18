package com.fet.auditoria.event;

import java.time.OffsetDateTime;

/**
 * Estructura del evento publicado por el backend en el tópico
 * {@code parqueadero.movimientos}, documentada en {@code 03_Kafka/eventos.md}.
 *
 * @param eventId                   identificador único del evento (UUID), usado para idempotencia
 * @param eventType                 tipo de evento
 * @param timestamp                 instante en que se generó el evento
 * @param movimientoId              identificador del movimiento persistido en el backend
 * @param vehiculoId                identificador del vehículo
 * @param placa                     placa del vehículo
 * @param espacioId                 identificador del espacio
 * @param espacioNumero             número del espacio
 * @param tiempoPermanenciaMinutos  permanencia calculada (null en VEHICULO_INGRESADO)
 */
public record MovimientoEvento(
        String eventId,
        TipoEventoMovimiento eventType,
        OffsetDateTime timestamp,
        Long movimientoId,
        Long vehiculoId,
        String placa,
        Long espacioId,
        String espacioNumero,
        Long tiempoPermanenciaMinutos) {
}
