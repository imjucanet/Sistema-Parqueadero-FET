package com.fet.parqueadero.adapter.in.graphql.dto;

import com.fet.parqueadero.domain.model.TipoVehiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Entrada GraphQL para registrar/actualizar un vehículo.
 *
 * <p>Las longitudes replican las de la tabla {@code VEHICULO}.</p>
 */
public record VehiculoInput(
        @NotBlank @Size(max = 20) String placa,
        @NotNull TipoVehiculo tipo,
        @NotBlank @Size(max = 80) String marca,
        @Size(max = 80) String modelo,
        @NotNull UUID propietarioId) {
}
