package com.fet.parqueadero.application.port.out;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Evento de movimiento publicado en el tópico {@code parqueadero.movimientos}.
 *
 * <p>La estructura coincide con el contrato definido en {@code 06_Kafka/eventos.md}.</p>
 *
 * @param eventId                   identificador único del evento (UUID)
 * @param eventType                 tipo de evento
 * @param timestamp                 instante en que se generó el evento
 * @param movimientoId              identificador del movimiento persistido
 * @param vehiculoId                identificador del vehículo
 * @param placa                     placa del vehículo
 * @param espacioId                 identificador del espacio
 * @param espacioNumero             número del espacio
 * @param tiempoPermanenciaMinutos  permanencia calculada (null en ingreso)
 */
public record MovimientoEvento(
        String eventId,
        TipoEventoMovimiento eventType,
        OffsetDateTime timestamp,
        UUID movimientoId,
        UUID vehiculoId,
        String placa,
        UUID espacioId,
        String espacioNumero,
        Long tiempoPermanenciaMinutos) {
}