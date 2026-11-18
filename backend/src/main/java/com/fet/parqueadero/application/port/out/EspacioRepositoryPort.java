package com.fet.parqueadero.application.port.out;

import com.fet.parqueadero.domain.model.Espacio;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Puerto de salida para la persistencia de espacios de parqueo. */
public interface EspacioRepositoryPort {

    Page<Espacio> findAll(Pageable pageable);

    Page<Espacio> findAllDisponibles(Pageable pageable);

    Optional<Espacio> findFirstDisponible();

    Optional<Espacio> findById(UUID id);

    /** Carga varios espacios en una sola consulta (evita el N+1 de los resolvers GraphQL). */
    List<Espacio> findAllById(Collection<UUID> ids);

    /** Busca un espacio por su número (identificador de negocio único). */
    Optional<Espacio> findByNumero(String numero);

    Espacio save(Espacio espacio);
}
