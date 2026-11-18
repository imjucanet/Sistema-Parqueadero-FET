package com.fet.parqueadero.domain.service;

import com.fet.parqueadero.application.port.in.GestionVehiculosUseCase;
import com.fet.parqueadero.application.port.out.PropietarioRepositoryPort;
import com.fet.parqueadero.application.port.out.VehiculoRepositoryPort;
import com.fet.parqueadero.domain.exception.EntidadInvalidaException;
import com.fet.parqueadero.domain.exception.RecursoDuplicadoException;
import com.fet.parqueadero.domain.exception.RecursoNoEncontradoException;
import com.fet.parqueadero.domain.model.CatalogoMarcas;
import com.fet.parqueadero.domain.model.EstadoVehiculo;
import com.fet.parqueadero.domain.model.TipoVehiculo;
import com.fet.parqueadero.domain.model.Vehiculo;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de los casos de uso de gestión de vehículos.
 *
 * <p>Cada operación es transaccional: la verificación de placa duplicada y el
 * guardado deben ocurrir en la misma transacción.</p>
 */
@Service
public class GestionVehiculosService implements GestionVehiculosUseCase {

    private final VehiculoRepositoryPort vehiculos;
    private final PropietarioRepositoryPort propietarios;

    public GestionVehiculosService(VehiculoRepositoryPort vehiculos, PropietarioRepositoryPort propietarios) {
        this.vehiculos = vehiculos;
        this.propietarios = propietarios;
    }

    @Override
    @Transactional
    public Vehiculo registrarVehiculo(Vehiculo vehiculo) {
        verificarPropietario(vehiculo.propietarioId());
        String placa = normalizarPlaca(vehiculo.placa());
        verificarPlacaNoDuplicada(placa, null);
        Vehiculo nuevo = new Vehiculo(null, placa, vehiculo.tipo(), vehiculo.marca(), vehiculo.modelo(), EstadoVehiculo.ACTIVO, vehiculo.propietarioId());
        return vehiculos.save(nuevo);
    }

    @Override
    @Transactional(readOnly = true)
    public Vehiculo obtenerVehiculo(UUID id) {
        return buscar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Vehiculo> obtenerVehiculos(Collection<UUID> ids) {
        return vehiculos.findAllById(ids).stream()
                .collect(Collectors.toMap(Vehiculo::id, Function.identity()));
    }

    @Override
    @Transactional
    public Vehiculo actualizarVehiculo(UUID id, Vehiculo vehiculo) {
        verificarPropietario(vehiculo.propietarioId());
        Vehiculo existente = buscar(id);
        String placa = normalizarPlaca(vehiculo.placa());
        verificarPlacaNoDuplicada(placa, id);
        Vehiculo actualizado = new Vehiculo(id, placa, vehiculo.tipo(), vehiculo.marca(), vehiculo.modelo(), existente.estado(), vehiculo.propietarioId());
        return vehiculos.save(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Vehiculo> listarVehiculos(Pageable pageable) {
        return vehiculos.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehiculo> listarVehiculosPorPropietario(UUID propietarioId) {
        return vehiculos.findByPropietarioId(propietarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, List<Vehiculo>> listarVehiculosPorPropietarios(Collection<UUID> propietarioIds) {
        return vehiculos.findByPropietarioIds(propietarioIds).stream()
                .collect(Collectors.groupingBy(Vehiculo::propietarioId));
    }

    @Override
    public Map<TipoVehiculo, Map<String, List<String>>> listarMarcas() {
        return CatalogoMarcas.todas();
    }

    private Vehiculo buscar(UUID id) {
        return vehiculos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("el vehículo", id));
    }

    private void verificarPropietario(UUID propietarioId) {
        if (!propietarios.existsById(propietarioId)) {
            throw new EntidadInvalidaException("PROPIETARIO_NO_ENCONTRADO", "No existe el propietario con id %s.".formatted(propietarioId));
        }
    }

    private void verificarPlacaNoDuplicada(String placa, UUID idActual) {
        vehiculos.findByPlaca(placa)
                .filter(existente -> !existente.id().equals(idActual))
                .ifPresent(existente -> {
                    throw new RecursoDuplicadoException(
                            "VEHICULO_YA_EXISTE",
                            "Ya existe un vehículo registrado con la placa %s.".formatted(placa));
                });
    }

    private static String normalizarPlaca(String placa) {
        return placa == null ? null : placa.trim().toUpperCase();
    }
}
