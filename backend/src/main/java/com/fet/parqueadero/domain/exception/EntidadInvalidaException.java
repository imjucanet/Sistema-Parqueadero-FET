package com.fet.parqueadero.domain.exception;

/**
 * Indica una solicitud que no puede ejecutarse por un dato de negocio inválido
 * (por ejemplo, referenciar un recurso inexistente). Se traduce en HTTP 400.
 */
public class EntidadInvalidaException extends ErrorNegocioException {

    public EntidadInvalidaException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}