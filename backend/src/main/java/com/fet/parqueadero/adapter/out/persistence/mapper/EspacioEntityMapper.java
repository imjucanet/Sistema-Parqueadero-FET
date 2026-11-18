package com.fet.parqueadero.adapter.out.persistence.mapper;

import com.fet.parqueadero.adapter.out.persistence.entity.EspacioEntity;
import com.fet.parqueadero.domain.model.Espacio;

/** Convierte entre la entidad {@code ESPACIO} y el modelo de dominio. */
public final class EspacioEntityMapper {

    private EspacioEntityMapper() {
    }

    public static EspacioEntity toEntity(Espacio dominio) {
        EspacioEntity entidad = new EspacioEntity();
        entidad.setId(dominio.id());
        entidad.setNumero(dominio.numero());
        entidad.setEstado(dominio.estado());
        return entidad;
    }

    public static Espacio toDomain(EspacioEntity entidad) {
        return new Espacio(
                entidad.getId(),
                entidad.getNumero(),
                entidad.getEstado());
    }
}