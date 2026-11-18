package com.fet.parqueadero.adapter.in.rest.mapper;

import com.fet.parqueadero.adapter.in.rest.dto.Propietario;
import com.fet.parqueadero.adapter.in.rest.dto.PropietarioPage;
import com.fet.parqueadero.adapter.in.rest.dto.PropietarioRequest;
import com.fet.parqueadero.domain.model.TipoUsuario;
import org.springframework.data.domain.Page;

/** Mapea entre los DTOs de la capa REST y el modelo de dominio de propietarios. */
public final class PropietarioRestMapper {

    private PropietarioRestMapper() {
    }

    public static Propietario toDto(com.fet.parqueadero.domain.model.Propietario dominio) {
        return new Propietario()
                .id(dominio.id())
                .primerNombre(dominio.primerNombre())
                .primerApellido(dominio.primerApellido())
                .nombreCompleto(dominio.nombreCompleto())
                .numeroDocumento(dominio.numeroDocumento())
                .tipoUsuario(Propietario.TipoUsuarioEnum.fromValue(dominio.tipoUsuario().name()))
                .telefono(dominio.telefono())
                .correo(dominio.correo());
    }

    public static com.fet.parqueadero.domain.model.Propietario toDomain(PropietarioRequest request) {
        return new com.fet.parqueadero.domain.model.Propietario(
                null,
                request.getPrimerNombre(),
                request.getPrimerApellido(),
                null,
                request.getNumeroDocumento(),
                toDomainTipoUsuario(request.getTipoUsuario()),
                request.getTelefono(),
                request.getCorreo());
    }

    private static TipoUsuario toDomainTipoUsuario(PropietarioRequest.TipoUsuarioEnum tipo) {
        return TipoUsuario.valueOf(tipo.getValue());
    }

    public static PropietarioPage toPageDto(Page<Propietario> pagina) {
        return new PropietarioPage()
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