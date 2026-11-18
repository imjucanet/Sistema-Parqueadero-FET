package com.fet.parqueadero.adapter.in.graphql.controller;

import com.fet.parqueadero.adapter.in.graphql.Paginacion;
import com.fet.parqueadero.adapter.in.graphql.dto.MarcasPorTipo;
import com.fet.parqueadero.adapter.in.graphql.dto.ResultadoLoteVehiculo;
import com.fet.parqueadero.adapter.in.graphql.dto.VehiculoInput;
import com.fet.parqueadero.application.port.in.GestionPropietariosUseCase;
import com.fet.parqueadero.application.port.in.GestionVehiculosUseCase;
import com.fet.parqueadero.domain.exception.ErrorNegocioException;
import com.fet.parqueadero.domain.model.EstadoVehiculo;
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

/** Controlador GraphQL de vehículos (adaptador de entrada). */
@Controller
public class VehiculoGraphQLController {

    private final GestionVehiculosUseCase vehiculos;
    private final GestionPropietariosUseCase propietarios;

    public VehiculoGraphQLController(GestionVehiculosUseCase vehiculos, GestionPropietariosUseCase propietarios) {
        this.vehiculos = vehiculos;
        this.propietarios = propietarios;
    }

    @QueryMapping
    public Vehiculo vehiculo(@Argument UUID id) {
        return vehiculos.obtenerVehiculo(id);
    }

    @QueryMapping
    public List<Vehiculo> vehiculos(@Argument Integer pagina, @Argument Integer tamano) {
        return vehiculos.listarVehiculos(Paginacion.de(pagina, tamano)).getContent();
    }

    @QueryMapping
    public List<MarcasPorTipo> marcas() {
        return MarcasPorTipo.desde(vehiculos.listarMarcas());
    }

    @MutationMapping
    public Vehiculo registrarVehiculo(@Argument @Valid VehiculoInput input) {
        return vehiculos.registrarVehiculo(toDomain(null, input));
    }

    @MutationMapping
    public Vehiculo actualizarVehiculo(@Argument UUID id, @Argument @Valid VehiculoInput input) {
        return vehiculos.actualizarVehiculo(id, toDomain(id, input));
    }

    /**
     * Registra varios vehículos informando el resultado de cada uno.
     *
     * <p>Igual que {@code POST /api/vehiculos/lote}: un error de negocio en un
     * elemento (placa duplicada, propietario inexistente) no aborta el lote, se
     * reporta en su propio {@link ResultadoLoteVehiculo}.</p>
     */
    @MutationMapping
    public List<ResultadoLoteVehiculo> registrarVehiculosEnLote(
            @Argument List<@Valid VehiculoInput> inputs) {
        return inputs.stream().map(this::registrarUno).toList();
    }

    private ResultadoLoteVehiculo registrarUno(VehiculoInput input) {
        try {
            return ResultadoLoteVehiculo.exitoso(vehiculos.registrarVehiculo(toDomain(null, input)));
        } catch (ErrorNegocioException ex) {
            return ResultadoLoteVehiculo.fallido(ex.getCodigo(), ex.getMessage());
        }
    }

    /**
     * Resuelve {@code Vehiculo.propietario} para todos los vehículos de la
     * consulta con una sola consulta a la base de datos (evita el N+1).
     */
    @BatchMapping(typeName = "Vehiculo", field = "propietario")
    public Map<Vehiculo, Propietario> propietarioDe(List<Vehiculo> lote) {
        Map<UUID, Propietario> porId = propietarios.obtenerPropietarios(
                lote.stream().map(Vehiculo::propietarioId).distinct().toList());
        return lote.stream().collect(Collectors.toMap(
                Function.identity(),
                vehiculo -> porId.get(vehiculo.propietarioId()),
                (primero, segundo) -> primero,
                LinkedHashMap::new));
    }

    private static Vehiculo toDomain(UUID id, VehiculoInput input) {
        return new Vehiculo(
                id,
                input.placa(),
                input.tipo(),
                input.marca(),
                input.modelo(),
                EstadoVehiculo.ACTIVO,
                input.propietarioId());
    }
}
