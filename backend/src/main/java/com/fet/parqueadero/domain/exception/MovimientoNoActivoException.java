package com.fet.parqueadero.domain.exception;

/** Indica que el movimiento no se encuentra activo y no admite registro de salida. Se traduce en HTTP 409. */
public class MovimientoNoActivoException extends ErrorNegocioException {

    private static final String CODIGO = "MOVIMIENTO_NO_ACTIVO";

    public MovimientoNoActivoException() {
        super(CODIGO, "El movimiento no se encuentra activo.");
    }
}