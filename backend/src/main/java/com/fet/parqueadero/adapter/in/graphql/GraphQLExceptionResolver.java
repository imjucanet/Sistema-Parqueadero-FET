package com.fet.parqueadero.adapter.in.graphql;

import com.fet.parqueadero.domain.exception.ErrorNegocioException;
import com.fet.parqueadero.domain.exception.RecursoNoEncontradoException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

/**
 * Traduce las excepciones de negocio del dominio y los fallos de validación a
 * errores GraphQL, exponiendo el mismo {@code codigo} estable que ya usa
 * {@code ErrorResponse} en el contrato REST.
 */
@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    private static final String CODIGO_VALIDACION = "VALIDACION";

    @Override
    protected List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment environment) {
        if (ex instanceof ConstraintViolationException violaciones) {
            return violaciones.getConstraintViolations().stream()
                    .map(violacion -> error(environment, ErrorType.BAD_REQUEST, CODIGO_VALIDACION, mensajeDe(violacion)))
                    .toList();
        }
        if (ex instanceof ErrorNegocioException negocio) {
            ErrorType tipo = negocio instanceof RecursoNoEncontradoException
                    ? ErrorType.NOT_FOUND
                    : ErrorType.BAD_REQUEST;
            return List.of(error(environment, tipo, negocio.getCodigo(), negocio.getMessage()));
        }
        return null;
    }

    /**
     * Nombra el campo que falló junto al mensaje, porque en GraphQL el
     * {@code path} del error apunta a la operación y no al campo del input.
     */
    private static String mensajeDe(ConstraintViolation<?> violacion) {
        String propiedad = violacion.getPropertyPath().toString();
        return propiedad.isEmpty()
                ? violacion.getMessage()
                : "%s: %s".formatted(propiedad, violacion.getMessage());
    }

    private static GraphQLError error(
            DataFetchingEnvironment environment, ErrorType tipo, String codigo, String mensaje) {
        return GraphqlErrorBuilder.newError(environment)
                .errorType(tipo)
                .message(mensaje)
                .extensions(Map.of("codigo", codigo))
                .build();
    }
}
