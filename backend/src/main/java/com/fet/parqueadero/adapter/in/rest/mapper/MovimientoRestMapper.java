package com.fet.parqueadero.adapter.in.rest.mapper;

import com.fet.parqueadero.adapter.in.rest.dto.Movimiento;
import com.fet.parqueadero.adapter.in.rest.dto.MovimientoPage;
import org.springframework.data.domain.Page;

/** Mapea entre el DTO REST y el modelo de dominio de movimientos. */
public final class MovimientoRestMapper {

    private MovimientoRestMapper() {
    }

    public static Movimiento toDto(com.fet.parqueadero.domain.model.Movimiento dominio) {
        return new Movimiento()
                .id(dominio.id())
                .vehiculoId(dominio.vehiculoId())
                .espacioId(dominio.espacioId())
                .fechaEntrada(dominio.fechaEntrada())
                .fechaSalida(dominio.fechaSalida())
                .tiempoPermanenciaMinutos(dominio.tiempoPermanenciaMinutos())
                .estado(Movimiento.EstadoEnum.fromValue(dominio.estado().name()));
    }

    public static MovimientoPage toPageDto(Page<Movimiento> pagina) {
        return new MovimientoPage()
                .content(pagina.getContent())
                .page(pagina.getNumber())
                .size(pagina.getSize())
                .totalElements(pagina.getTotalElements())
                .totalPages(pagina.getTotalPages())
                .first(pagina.isFirst())
                .last(pagina.isLast())
                .hasNext(pagina.hasNext())
                .hasPrevious(pagina.hasPrevious());
    }
}