package com.fet.parqueadero.adapter.in.graphql.dto;

import com.fet.parqueadero.domain.model.TipoUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Entrada GraphQL para registrar/actualizar un propietario.
 *
 * <p>Las longitudes replican las de la tabla {@code PROPIETARIO}
 * (ver {@code db/migration/V1__esquema_inicial.sql}). Sin ellas, un valor
 * demasiado largo llega hasta el INSERT y se convierte en un
 * {@code ERROR_INTERNO}; validándolo aquí el cliente recibe {@code BAD_REQUEST}
 * con el código {@code VALIDACION}.</p>
 */
public record PropietarioInput(
        @NotBlank @Size(max = 80) String primerNombre,
        @NotBlank @Size(max = 80) String primerApellido,
        @NotBlank @Size(max = 30) String numeroDocumento,
        @NotNull TipoUsuario tipoUsuario,
        @Size(max = 30) String telefono,
        @Email @Size(max = 120) String correo) {
}
