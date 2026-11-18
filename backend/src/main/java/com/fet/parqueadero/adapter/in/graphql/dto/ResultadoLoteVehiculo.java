package com.fet.parqueadero.adapter.in.graphql.dto;

import com.fet.parqueadero.domain.model.Vehiculo;

/**
 * Resultado individual del registro de vehículos en lote.
 *
 * <p>Equivale a {@code VehiculoLoteResultado} del contrato REST: el lote nunca
 * falla completo, cada elemento informa su propio éxito o su error de negocio.</p>
 */
public record ResultadoLoteVehiculo(boolean exito, Vehiculo vehiculo, ErrorNegocio error) {

    public static ResultadoLoteVehiculo exitoso(Vehiculo vehiculo) {
        return new ResultadoLoteVehiculo(true, vehiculo, null);
    }

    public static ResultadoLoteVehiculo fallido(String codigo, String mensaje) {
        return new ResultadoLoteVehiculo(false, null, new ErrorNegocio(codigo, mensaje));
    }
}
