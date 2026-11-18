package com.fet.parqueadero.adapter.in.graphql.controller;

import com.fet.parqueadero.adapter.in.graphql.MovimientoEventPublisher;
import com.fet.parqueadero.adapter.in.graphql.Paginacion;
import com.fet.parqueadero.application.port.in.GestionEspaciosUseCase;
import com.fet.parqueadero.application.port.in.GestionMovimientosUseCase;
import com.fet.parqueadero.application.port.in.GestionVehiculosUseCase;
import com.fet.parqueadero.domain.model.Espacio;
import com.fet.parqueadero.domain.model.Movimiento;
import com.fet.parqueadero.domain.model.Vehiculo;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/** Controlador GraphQL de movimientos (adaptador de entrada). */
@Controller
public class MovimientoGraphQLController {

    private final GestionMovimientosUseCase movimientos;
    private final GestionVehiculosUseCase vehiculos;
    private final GestionEspaciosUseCase espacios;
    private final MovimientoEventPublisher eventPublisher;

    public MovimientoGraphQLController(
            GestionMovimientosUseCase movimientos,
            GestionVehiculosUseCase vehiculos,
            GestionEspaciosUseCase espacios,
            MovimientoEventPublisher eventPublisher) {
        this.movimientos = movimientos;
        this.vehiculos = vehiculos;
        this.espacios = espacios;
        this.eventPublisher = eventPublisher;
    }

    @QueryMapping
    public Movimiento movimiento(@Argument UUID id) {
        return movimientos.obtenerMovimiento(id);
    }

    @QueryMapping
    public List<Movimiento> movimientosActivos(@Argument Integer pagina, @Argument Integer tamano) {
        return movimientos.listarMovimientosActivos(Paginacion.de(pagina, tamano)).getContent();
    }

    @QueryMapping
    public List<Movimiento> historialMovimientos(@Argument Integer pagina, @Argument Integer tamano) {
        return movimientos.listarHistorialMovimientos(Paginacion.de(pagina, tamano)).getContent();
    }

    @MutationMapping
    public Movimiento registrarEntrada(@Argument UUID vehiculoId, @Argument UUID espacioId) {
        return movimientos.registrarEntrada(vehiculoId, espacioId);
    }

    @MutationMapping
    public Movimiento registrarSalida(@Argument UUID movimientoId) {
        return movimientos.registrarSalida(movimientoId);
    }

    @SubscriptionMapping
    public Flux<Movimiento> movimientoRegistrado() {
        return eventPublisher.stream()
                .publishOn(Schedulers.boundedElastic())
                .map(evento -> movimientos.obtenerMovimiento(evento.movimientoId()));
    }

    /** Resuelve {@code Movimiento.vehiculo} por lotes (evita el N+1). */
    @BatchMapping(typeName = "Movimiento", field = "vehiculo")
    public Map<Movimiento, Vehiculo> vehiculoDe(List<Movimiento> lote) {
        Map<UUID, Vehiculo> porId = vehiculos.obtenerVehiculos(
                lote.stream().map(Movimiento::vehiculoId).distinct().toList());
        return indexar(lote, movimiento -> porId.get(movimiento.vehiculoId()));
    }

    /** Resuelve {@code Movimiento.espacio} por lotes (evita el N+1). */
    @BatchMapping(typeName = "Movimiento", field = "espacio")
    public Map<Movimiento, Espacio> espacioDe(List<Movimiento> lote) {
        Map<UUID, Espacio> porId = espacios.obtenerEspacios(
                lote.stream().map(Movimiento::espacioId).distinct().toList());
        return indexar(lote, movimiento -> porId.get(movimiento.espacioId()));
    }

    /**
     * {@code tiempoPermanenciaMinutos} es {@code Long} en el dominio y {@code Int}
     * en el esquema; el estrechamiento se hace aquí de forma explícita.
     */
    @SchemaMapping(typeName = "Movimiento", field = "tiempoPermanenciaMinutos")
    public Integer tiempoPermanenciaMinutos(Movimiento movimiento) {
        return movimiento.tiempoPermanenciaMinutos() == null
                ? null
                : movimiento.tiempoPermanenciaMinutos().intValue();
    }

    private static <T> Map<Movimiento, T> indexar(List<Movimiento> lote, Function<Movimiento, T> resolver) {
        return lote.stream().collect(Collectors.toMap(
                Function.identity(),
                resolver,
                (primero, segundo) -> primero,
                LinkedHashMap::new));
    }
}
