package com.fet.parqueadero.application.port.out;

import com.fet.parqueadero.domain.model.Vehiculo;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Puerto de salida para la persistencia de vehículos. */
public interface VehiculoRepositoryPort {

    Vehiculo save(Vehiculo vehiculo);

    Optional<Vehiculo> findById(UUID id);

    /** Carga varios vehículos en una sola consulta (evita el N+1 de los resolvers GraphQL). */
    List<Vehiculo> findAllById(Collection<UUID> ids);

    /** Busca un vehículo por su placa (identificador de negocio único). */
    Optional<Vehiculo> findByPlaca(String placa);

    Page<Vehiculo> findAll(Pageable pageable);

    /** Lista todos los vehículos de un propietario. */
    List<Vehiculo> findByPropietarioId(UUID propietarioId);

    /** Lista los vehículos de varios propietarios en una sola consulta. */
    List<Vehiculo> findByPropietarioIds(Collection<UUID> propietarioIds);
}
