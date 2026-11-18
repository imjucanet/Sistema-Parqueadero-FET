package com.fet.parqueadero.application.port.in;

import com.fet.parqueadero.domain.model.TipoVehiculo;
import com.fet.parqueadero.domain.model.Vehiculo;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Casos de uso de gestión de vehículos. */
public interface GestionVehiculosUseCase {

    /** Registra un nuevo vehículo bajo un propietario existente. */
    Vehiculo registrarVehiculo(Vehiculo vehiculo);

    /** Obtiene un vehículo por su identificador. Lanza {@code RecursoNoEncontradoException} si no existe. */
    Vehiculo obtenerVehiculo(UUID id);

    /**
     * Carga varios vehículos en una sola consulta, indexados por id.
     * Pensado para los resolvers por lotes de GraphQL ({@code @BatchMapping}).
     */
    Map<UUID, Vehiculo> obtenerVehiculos(Collection<UUID> ids);

    /** Actualiza los datos de un vehículo existente. */
    Vehiculo actualizarVehiculo(UUID id, Vehiculo vehiculo);

    /** Lista todos los vehículos registrados. */
    Page<Vehiculo> listarVehiculos(Pageable pageable);

    /** Lista los vehículos de un propietario. */
    List<Vehiculo> listarVehiculosPorPropietario(UUID propietarioId);

    /**
     * Lista los vehículos de varios propietarios en una sola consulta,
     * agrupados por id de propietario (resolvers por lotes de GraphQL).
     */
    Map<UUID, List<Vehiculo>> listarVehiculosPorPropietarios(Collection<UUID> propietarioIds);

    /**
     * Catálogo estático completo de marcas y modelos conocidos, agrupado por
     * tipo de vehículo y luego por marca.
     */
    Map<TipoVehiculo, Map<String, List<String>>> listarMarcas();
}
