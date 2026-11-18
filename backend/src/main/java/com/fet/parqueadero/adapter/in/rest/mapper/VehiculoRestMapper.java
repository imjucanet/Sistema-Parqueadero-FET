package com.fet.parqueadero.adapter.in.rest.mapper;

import com.fet.parqueadero.adapter.in.rest.dto.Vehiculo;
import com.fet.parqueadero.adapter.in.rest.dto.VehiculoPage;
import com.fet.parqueadero.adapter.in.rest.dto.VehiculoRequest;
import com.fet.parqueadero.domain.model.EstadoVehiculo;
import com.fet.parqueadero.domain.model.TipoVehiculo;
import org.springframework.data.domain.Page;

/** Mapea entre los DTOs de la capa REST y el modelo de dominio de vehículos. */
public final class VehiculoRestMapper {

    private VehiculoRestMapper() {
    }

    public static Vehiculo toDto(com.fet.parqueadero.domain.model.Vehiculo dominio) {
        return new Vehiculo()
                .id(dominio.id())
                .placa(dominio.placa())
                .tipo(Vehiculo.TipoEnum.fromValue(dominio.tipo().name()))
                .marca(dominio.marca())
                .modelo(dominio.modelo())
                .propietarioId(dominio.propietarioId())
                .estado(Vehiculo.EstadoEnum.fromValue(dominio.estado().name()));
    }

    public static com.fet.parqueadero.domain.model.Vehiculo toDomain(VehiculoRequest request) {
        return new com.fet.parqueadero.domain.model.Vehiculo(
                null,
                request.getPlaca(),
                toDomainTipo(request.getTipo()),
                request.getMarca(),
                request.getModelo(),
                EstadoVehiculo.ACTIVO,
                request.getPropietarioId());
    }

    private static TipoVehiculo toDomainTipo(VehiculoRequest.TipoEnum tipo) {
        return TipoVehiculo.valueOf(tipo.getValue());
    }

    public static VehiculoPage toPageDto(Page<Vehiculo> pagina) {
        return new VehiculoPage()
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