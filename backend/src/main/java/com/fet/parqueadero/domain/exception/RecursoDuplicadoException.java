package com.fet.parqueadero.domain.exception;

/**
 * Indica que ya existe un recurso con el mismo identificador de negocio
 * (documento, placa, número de espacio, etc.). Se traduce en HTTP 409.
 */
public class RecursoDuplicadoException extends ErrorNegocioException {

    public RecursoDuplicadoException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
