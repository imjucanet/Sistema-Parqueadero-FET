package com.fet.parqueadero.application.port.out;

import com.fet.parqueadero.domain.model.Propietario;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Puerto de salida para la persistencia de propietarios. */
public interface PropietarioRepositoryPort {

    Propietario save(Propietario propietario);

    Optional<Propietario> findById(UUID id);

    /** Carga varios propietarios en una sola consulta (evita el N+1 de los resolvers GraphQL). */
    List<Propietario> findAllById(Collection<UUID> ids);

    boolean existsById(UUID id);

    /** Busca un propietario por su número de documento (identificador de negocio único). */
    Optional<Propietario> findByNumeroDocumento(String numeroDocumento);

    Page<Propietario> findAll(Pageable pageable);
}
