package com.fet.parqueadero.adapter.in.rest.mapper;

import com.fet.parqueadero.adapter.in.rest.dto.Espacio;
import com.fet.parqueadero.adapter.in.rest.dto.EspacioPage;
import com.fet.parqueadero.adapter.in.rest.dto.EspacioRequest;
import org.springframework.data.domain.Page;

/** Mapea entre el DTO REST y el modelo de dominio de espacios. */
public final class EspacioRestMapper {

    private EspacioRestMapper() {
    }

    public static Espacio toDto(com.fet.parqueadero.domain.model.Espacio dominio) {
        return new Espacio()
                .id(dominio.id())
                .numero(dominio.numero())
                .estado(Espacio.EstadoEnum.fromValue(dominio.estado().name()));
    }

    public static com.fet.parqueadero.domain.model.Espacio toDomain(EspacioRequest request) {
        return new com.fet.parqueadero.domain.model.Espacio(null, request.getNumero(), null);
    }

    public static EspacioPage toPageDto(Page<Espacio> pagina) {
        return new EspacioPage()
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