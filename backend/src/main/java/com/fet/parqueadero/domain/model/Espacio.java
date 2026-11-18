package com.fet.parqueadero.domain.model;

import java.util.UUID;

/**
 * Entidad de dominio que representa un espacio de parqueo.
 *
 * @param id     identificador del espacio (UUID)
 * @param numero identificación alfanumérica del espacio
 * @param estado estado actual del espacio
 */
public record Espacio(
        UUID id,
        String numero,
        EstadoEspacio estado) {
}