package com.fet.parqueadero.adapter.in.rest;

import com.fet.parqueadero.adapter.in.rest.dto.ErrorResponse;
import com.fet.parqueadero.domain.exception.EntidadInvalidaException;
import com.fet.parqueadero.domain.exception.EspacioNoDisponibleException;
import com.fet.parqueadero.domain.exception.ErrorNegocioException;
import com.fet.parqueadero.domain.exception.MovimientoNoActivoException;
import com.fet.parqueadero.domain.exception.RecursoDuplicadoException;
import com.fet.parqueadero.domain.exception.RecursoNoEncontradoException;
import com.fet.parqueadero.domain.exception.VehiculoYaEnParqueaderoException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Manejador global de excepciones que traduce validaciones y errores de negocio
 * a la estructura {@link ErrorResponse} definida en el contrato OpenAPI.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String CODIGO_VALIDACION = "VALIDACION";
    private static final String CODIGO_SOLICITUD_INVALIDA = "SOLICITUD_INVALIDA";
    private static final String CODIGO_ERROR_INTERNO = "ERROR_INTERNO";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> manejarValidacionCuerpo(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("La solicitud contiene campos inválidos.");
        return responder(HttpStatus.BAD_REQUEST, CODIGO_VALIDACION, mensaje);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> manejarValidacionParametros(ConstraintViolationException ex) {
        String mensaje = ex.getConstraintViolations().stream()
                .findFirst()
                .map(violacion -> violacion.getMessage())
                .orElse("La solicitud contiene parámetros inválidos.");
        return responder(HttpStatus.BAD_REQUEST, CODIGO_VALIDACION, mensaje);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> manejarParametroFaltante(MissingServletRequestParameterException ex) {
        return responder(HttpStatus.BAD_REQUEST, CODIGO_SOLICITUD_INVALIDA,
                "El parámetro '%s' es obligatorio.".formatted(ex.getParameterName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> manejarCuerpoNoLegible(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, CODIGO_SOLICITUD_INVALIDA,
                "La solicitud no pudo interpretarse; revise el cuerpo y los tipos de datos enviados.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return responder(HttpStatus.BAD_REQUEST, CODIGO_VALIDACION,
                "Los parámetros de paginación son inválidos. Verifique que page >= 0 y size >= 1.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> manejarTipoDeParametro(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST, CODIGO_SOLICITUD_INVALIDA,
                "El parámetro '%s' tiene un tipo de dato inválido.".formatted(ex.getName()));
    }

    @ExceptionHandler(EntidadInvalidaException.class)
    ResponseEntity<ErrorResponse> manejarEntidadInvalida(EntidadInvalidaException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler({
            EspacioNoDisponibleException.class,
            VehiculoYaEnParqueaderoException.class,
            MovimientoNoActivoException.class,
            RecursoDuplicadoException.class})
    ResponseEntity<ErrorResponse> manejarConflicto(ErrorNegocioException ex) {
        return responder(HttpStatus.CONFLICT, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ErrorResponse> manejarRutaNoEncontrada(NoResourceFoundException ex) {
        return responder(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", "No existe el recurso solicitado.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> manejarErrorNoControlado(Exception ex) {
        log.error("Error no controlado.", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, CODIGO_ERROR_INTERNO,
                "Ocurrió un error interno en el servidor.");
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String codigo, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorResponse(codigo, mensaje));
    }
}