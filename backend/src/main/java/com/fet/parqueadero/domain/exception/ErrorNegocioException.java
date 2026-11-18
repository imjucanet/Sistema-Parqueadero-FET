package com.fet.parqueadero.domain.exception;

/**
 * Base de las excepciones de negocio del dominio.
 *
 * <p>Cada excepción transporta un {@code codigo} estable que se refleja
 * en el campo {@code codigo} de {@code ErrorResponse} del contrato OpenAPI.</p>
 */
public abstract class ErrorNegocioException extends RuntimeException {

    private final String codigo;

    protected ErrorNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    /** Código estable del error para la respuesta {@code ErrorResponse}. */
    public String getCodigo() {
        return codigo;
    }
}