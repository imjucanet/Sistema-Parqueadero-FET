package com.fet.parqueadero.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Entidad de dominio que representa un movimiento de entrada/salida.
 *
 * @param id                        identificador del movimiento (UUID)
 * @param vehiculoId                identificador del vehículo involucrado
 * @param espacioId                 identificador del espacio asignado
 * @param fechaEntrada              instante de entrada al parqueadero
 * @param fechaSalida               instante de salida (null mientras esté activo)
 * @param tiempoPermanenciaMinutos  permanencia calculada en minutos
 * @param estado                    estado del movimiento
 */
public record Movimiento(
        UUID id,
        UUID vehiculoId,
        UUID espacioId,
        OffsetDateTime fechaEntrada,
        OffsetDateTime fechaSalida,
        Long tiempoPermanenciaMinutos,
        EstadoMovimiento estado) {
}