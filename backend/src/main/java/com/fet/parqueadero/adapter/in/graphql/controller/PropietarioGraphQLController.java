package com.fet.parqueadero.adapter.in.graphql.controller;

import com.fet.parqueadero.adapter.in.graphql.Paginacion;
import com.fet.parqueadero.adapter.in.graphql.dto.PropietarioInput;
import com.fet.parqueadero.application.port.in.GestionPropietariosUseCase;
import com.fet.parqueadero.application.port.in.GestionVehiculosUseCase;
import com.fet.parqueadero.domain.model.Propietario;
import com.fet.parqueadero.domain.model.Vehiculo;
import jakarta.validation.Valid;
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
import org.springframework.stereotype.Controller;

/** Controlador GraphQL de propietarios (adaptador de entrada). */
@Controller
public class PropietarioGraphQLController {

    private final GestionPropietariosUseCase propietarios;
    private final GestionVehiculosUseCase vehiculos;

    public PropietarioGraphQLController(GestionPropietariosUseCase propietarios, GestionVehiculosUseCase vehiculos) {
        this.propietarios = propietarios;
        this.vehiculos = vehiculos;
    }

    @QueryMapping
    public Propietario propietario(@Argument UUID id) {
        return propietarios.obtenerPropietario(id);
    }

    @QueryMapping
    public List<Propietario> propietarios(@Argument Integer pagina, @Argument Integer tamano) {
        return propietarios.listarPropietarios(Paginacion.de(pagina, tamano)).getContent();
    }

    @MutationMapping
    public Propietario registrarPropietario(@Argument @Valid PropietarioInput input) {
        return propietarios.registrarPropietario(toDomain(null, input));
    }

    @MutationMapping
    public Propietario actualizarPropietario(@Argument UUID id, @Argument @Valid PropietarioInput input) {
        return propietarios.actualizarPropietario(id, toDomain(id, input));
    }

    /**
     * Resuelve {@code Propietario.vehiculos} para todos los propietarios de la
     * consulta con una sola consulta a la base de datos.
     *
     * <p>Con {@code @SchemaMapping} este campo se resolvía una vez por
     * propietario (problema N+1): una consulta de 100 propietarios disparaba
     * 101 consultas.</p>
     */
    @BatchMapping(typeName = "Propietario", field = "vehiculos")
    public Map<Propietario, List<Vehiculo>> vehiculosDe(List<Propietario> lote) {
        Map<UUID, List<Vehiculo>> porPropietario = vehiculos.listarVehiculosPorPropietarios(
                lote.stream().map(Propietario::id).toList());
        return lote.stream().collect(Collectors.toMap(
                Function.identity(),
                propietario -> porPropietario.getOrDefault(propietario.id(), List.of()),
                (primero, segundo) -> primero,
                LinkedHashMap::new));
    }

    private static Propietario toDomain(UUID id, PropietarioInput input) {
        return new Propietario(
                id,
                input.primerNombre(),
                input.primerApellido(),
                null,
                input.numeroDocumento(),
                input.tipoUsuario(),
                input.telefono(),
                input.correo());
    }
}
