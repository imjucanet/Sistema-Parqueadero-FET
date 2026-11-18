package com.fet.parqueadero.adapter.in.graphql.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entrada GraphQL para registrar un espacio de parqueo.
 *
 * <p>La longitud replica la de la tabla {@code ESPACIO} y coincide con
 * {@code EspacioRequest} del contrato OpenAPI.</p>
 */
public record EspacioInput(
        @NotBlank @Size(max = 10) String numero) {
}
