package com.fet.parqueadero.domain.model;

import java.util.UUID;

/**
 * Entidad de dominio que representa a un propietario de vehículos.
 *
 * @param id              identificador asignado por la persistencia (UUID)
 * @param primerNombre    primer nombre del propietario
 * @param primerApellido  primer apellido del propietario
 * @param nombreCompleto  concatenación de primerNombre y primerApellido, calculada por el backend
 * @param numeroDocumento número de documento de identidad, único por propietario
 * @param tipoUsuario     tipo de usuario institucional
 * @param telefono        número de teléfono (opcional)
 * @param correo          correo electrónico (opcional)
 */
public record Propietario(
        UUID id,
        String primerNombre,
        String primerApellido,
        String nombreCompleto,
        String numeroDocumento,
        TipoUsuario tipoUsuario,
        String telefono,
        String correo) {
}
