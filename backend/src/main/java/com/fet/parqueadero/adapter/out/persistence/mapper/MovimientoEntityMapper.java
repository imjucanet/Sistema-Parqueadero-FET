package com.fet.parqueadero.adapter.out.persistence.mapper;

import com.fet.parqueadero.adapter.out.persistence.entity.MovimientoEntity;
import com.fet.parqueadero.domain.model.Movimiento;

/** Convierte entre la entidad {@code MOVIMIENTO} y el modelo de dominio. */
public final class MovimientoEntityMapper {

    private MovimientoEntityMapper() {
    }

    public static MovimientoEntity toEntity(Movimiento dominio) {
        MovimientoEntity entidad = new MovimientoEntity();
        entidad.setId(dominio.id());
        entidad.setFechaEntrada(dominio.fechaEntrada());
        entidad.setFechaSalida(dominio.fechaSalida());
        entidad.setTiempoPermanenciaMinutos(dominio.tiempoPermanenciaMinutos());
        entidad.setEstado(dominio.estado());
        return entidad;
    }

    public static Movimiento toDomain(MovimientoEntity entidad) {
        return new Movimiento(
                entidad.getId(),
                entidad.getVehiculo().getId(),
                entidad.getEspacio().getId(),
                entidad.getFechaEntrada(),
                entidad.getFechaSalida(),
                entidad.getTiempoPermanenciaMinutos(),
                entidad.getEstado());
    }
}