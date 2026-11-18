package com.fet.parqueadero.adapter.out.persistence.mapper;

import com.fet.parqueadero.adapter.out.persistence.entity.VehiculoEntity;
import com.fet.parqueadero.domain.model.Vehiculo;

/** Convierte entre la entidad {@code VEHICULO} y el modelo de dominio. */
public final class VehiculoEntityMapper {

    private VehiculoEntityMapper() {
    }

    public static VehiculoEntity toEntity(Vehiculo dominio) {
        VehiculoEntity entidad = new VehiculoEntity();
        entidad.setId(dominio.id());
        entidad.setPlaca(dominio.placa());
        entidad.setTipo(dominio.tipo());
        entidad.setMarca(dominio.marca());
        entidad.setModelo(dominio.modelo());
        entidad.setEstado(dominio.estado());
        return entidad;
    }

    public static Vehiculo toDomain(VehiculoEntity entidad) {
        return new Vehiculo(
                entidad.getId(),
                entidad.getPlaca(),
                entidad.getTipo(),
                entidad.getMarca(),
                entidad.getModelo(),
                entidad.getEstado(),
                entidad.getPropietario().getId());
    }
}