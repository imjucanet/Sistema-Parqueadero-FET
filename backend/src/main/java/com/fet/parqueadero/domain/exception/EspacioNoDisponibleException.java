package com.fet.parqueadero.domain.exception;

/** Indica que no hay un espacio disponible para registrar la entrada. Se traduce en HTTP 409. */
public class EspacioNoDisponibleException extends ErrorNegocioException {

    private static final String CODIGO = "ESPACIO_NO_DISPONIBLE";

    public EspacioNoDisponibleException() {
        super(CODIGO, "No hay espacios disponibles.");
    }

    public EspacioNoDisponibleException(String mensaje) {
        super(CODIGO, mensaje);
    }
}