package com.fet.parqueadero.domain.exception;

/** Indica que el vehículo ya se encuentra dentro del parqueadero. Se traduce en HTTP 409. */
public class VehiculoYaEnParqueaderoException extends ErrorNegocioException {

    private static final String CODIGO = "VEHICULO_YA_EN_PARQUEADERO";

    public VehiculoYaEnParqueaderoException() {
        super(CODIGO, "El vehículo ya se encuentra dentro del parqueadero.");
    }
}