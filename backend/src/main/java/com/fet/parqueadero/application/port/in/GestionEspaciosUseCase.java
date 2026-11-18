package com.fet.parqueadero.application.port.in;

import com.fet.parqueadero.domain.model.Espacio;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Casos de uso de gestión y consulta de espacios de parqueo. */
public interface GestionEspaciosUseCase {

    /** Registra un nuevo espacio como DISPONIBLE. Lanza {@code RecursoDuplicadoException} si el número ya existe. */
    Espacio registrarEspacio(Espacio espacio);

    /** Obtiene un espacio por su identificador. Lanza {@code RecursoNoEncontradoException} si no existe. */
    Espacio obtenerEspacio(UUID id);

    /**
     * Carga varios espacios en una sola consulta, indexados por id.
     * Pensado para los resolvers por lotes de GraphQL ({@code @BatchMapping}).
     */
    Map<UUID, Espacio> obtenerEspacios(Collection<UUID> ids);

    /** Lista todos los espacios registrados. */
    Page<Espacio> listarEspacios(Pageable pageable);

    /** Lista únicamente los espacios disponibles. */
    Page<Espacio> listarEspaciosDisponibles(Pageable pageable);
}