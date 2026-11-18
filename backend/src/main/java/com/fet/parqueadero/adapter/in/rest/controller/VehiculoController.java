package com.fet.parqueadero.adapter.in.rest.controller;

import com.fet.parqueadero.adapter.in.rest.api.VehiculosApi;
import com.fet.parqueadero.adapter.in.rest.dto.ErrorResponse;
import com.fet.parqueadero.adapter.in.rest.dto.Vehiculo;
import com.fet.parqueadero.adapter.in.rest.dto.VehiculoLoteRequest;
import com.fet.parqueadero.adapter.in.rest.dto.VehiculoLoteResponse;
import com.fet.parqueadero.adapter.in.rest.dto.VehiculoLoteResultado;
import com.fet.parqueadero.adapter.in.rest.dto.VehiculoPage;
import com.fet.parqueadero.adapter.in.rest.dto.VehiculoRequest;
import com.fet.parqueadero.adapter.in.rest.mapper.VehiculoRestMapper;
import com.fet.parqueadero.application.port.in.GestionVehiculosUseCase;
import com.fet.parqueadero.domain.exception.ErrorNegocioException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST de vehículos (adaptador de entrada).
 */
@RestController
@Validated
public class VehiculoController implements VehiculosApi {

    private final GestionVehiculosUseCase useCase;

    public VehiculoController(GestionVehiculosUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public ResponseEntity<Vehiculo> registrarVehiculo(@Valid @RequestBody VehiculoRequest vehiculoRequest) {
        Vehiculo creado = VehiculoRestMapper.toDto(useCase.registrarVehiculo(VehiculoRestMapper.toDomain(vehiculoRequest)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Override
    public ResponseEntity<VehiculoLoteResponse> registrarVehiculosEnLote(@Valid @RequestBody VehiculoLoteRequest vehiculoLoteRequest) {
        List<VehiculoLoteResultado> resultados = vehiculoLoteRequest.getVehiculos().stream()
                .map(this::registrarUno)
                .toList();
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(new VehiculoLoteResponse().resultados(resultados));
    }

    private VehiculoLoteResultado registrarUno(VehiculoRequest vehiculoRequest) {
        try {
            Vehiculo creado = VehiculoRestMapper.toDto(useCase.registrarVehiculo(VehiculoRestMapper.toDomain(vehiculoRequest)));
            return new VehiculoLoteResultado().exito(true).vehiculo(creado);
        } catch (ErrorNegocioException ex) {
            return new VehiculoLoteResultado().exito(false).error(new ErrorResponse(ex.getCodigo(), ex.getMessage()));
        }
    }

    @Override
    public ResponseEntity<VehiculoPage> listarVehiculos(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        Page<Vehiculo> vehiculos = useCase.listarVehiculos(pageable).map(VehiculoRestMapper::toDto);
        return ResponseEntity.ok(VehiculoRestMapper.toPageDto(vehiculos));
    }

    @Override
    public ResponseEntity<Vehiculo> obtenerVehiculo(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(VehiculoRestMapper.toDto(useCase.obtenerVehiculo(id)));
    }

    @Override
    public ResponseEntity<Vehiculo> actualizarVehiculo(@PathVariable("id") UUID id, @Valid @RequestBody VehiculoRequest vehiculoRequest) {
        Vehiculo actualizado = VehiculoRestMapper.toDto(useCase.actualizarVehiculo(id, VehiculoRestMapper.toDomain(vehiculoRequest)));
        return ResponseEntity.ok(actualizado);
    }

    @Override
    public ResponseEntity<Map<String, Map<String, List<String>>>> listarMarcas() {
        Map<String, Map<String, List<String>>> catalogo = useCase.listarMarcas().entrySet().stream()
                .collect(Collectors.toMap(entrada -> entrada.getKey().name(), Map.Entry::getValue));
        return ResponseEntity.ok(catalogo);
    }
}