package com.fet.parqueadero.domain.model;

import java.util.UUID;

/**
 * Entidad de dominio que representa un vehículo registrado.
 *
 * @param id           identificador asignado por la persistencia (UUID)
 * @param placa        placa del vehículo
 * @param tipo         tipo de vehículo
 * @param marca        marca del vehículo
 * @param modelo       modelo del vehículo (opcional)
 * @param estado       estado operacional
 * @param propietarioId identificador del propietario asociado
 */
public record Vehiculo(
        UUID id,
        String placa,
        TipoVehiculo tipo,
        String marca,
        String modelo,
        EstadoVehiculo estado,
        UUID propietarioId) {
}