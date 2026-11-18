package com.fet.parqueadero.adapter.in.graphql.dto;

/**
 * Error de negocio expuesto dentro de un resultado GraphQL.
 *
 * <p>Transporta el mismo {@code codigo} estable que {@code ErrorResponse} en el
 * contrato REST, de modo que ambos adaptadores informen el mismo vocabulario de
 * errores.</p>
 */
public record ErrorNegocio(String codigo, String mensaje) {
}
