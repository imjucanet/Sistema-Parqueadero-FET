package com.fet.parqueadero.domain.exception;

import java.util.UUID;

/** Indica que un recurso solicitado no existe. Se traduce en HTTP 404. */
public class RecursoNoEncontradoException extends ErrorNegocioException {

    private static final String CODIGO = "RECURSO_NO_ENCONTRADO";

    public RecursoNoEncontradoException(String recurso, UUID id) {
        super(CODIGO, "No existe %s con id %s.".formatted(recurso, id));
    }
}