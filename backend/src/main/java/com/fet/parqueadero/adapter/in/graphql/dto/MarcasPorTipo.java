package com.fet.parqueadero.adapter.in.graphql.dto;

import com.fet.parqueadero.domain.model.TipoVehiculo;
import java.util.List;
import java.util.Map;

/**
 * Catálogo de marcas de un tipo de vehículo.
 *
 * <p>GraphQL no tiene un tipo "mapa abierto" como el {@code CatalogoMarcas} del
 * contrato REST, así que el mismo contenido se expone como listas tipadas.</p>
 */
public record MarcasPorTipo(TipoVehiculo tipo, List<MarcaConModelos> marcas) {

    /** Convierte el catálogo del dominio (tipo -> marca -> modelos) a listas tipadas. */
    public static List<MarcasPorTipo> desde(Map<TipoVehiculo, Map<String, List<String>>> catalogo) {
        return catalogo.entrySet().stream()
                .map(porTipo -> new MarcasPorTipo(
                        porTipo.getKey(),
                        porTipo.getValue().entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(porMarca -> new MarcaConModelos(porMarca.getKey(), porMarca.getValue()))
                                .toList()))
                .sorted((a, b) -> a.tipo().compareTo(b.tipo()))
                .toList();
    }
}
