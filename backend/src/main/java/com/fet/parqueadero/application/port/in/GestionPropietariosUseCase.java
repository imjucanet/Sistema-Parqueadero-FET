package com.fet.parqueadero.application.port.in;

import com.fet.parqueadero.domain.model.Propietario;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Casos de uso de gestión de propietarios. */
public interface GestionPropietariosUseCase {

    /** Registra un nuevo propietario y lo persiste. */
    Propietario registrarPropietario(Propietario propietario);

    /** Obtiene un propietario por su identificador. Lanza {@code RecursoNoEncontradoException} si no existe. */
    Propietario obtenerPropietario(UUID id);

    /**
     * Carga varios propietarios en una sola consulta, indexados por id.
     * Pensado para los resolvers por lotes de GraphQL ({@code @BatchMapping}).
     */
    Map<UUID, Propietario> obtenerPropietarios(Collection<UUID> ids);

    /** Actualiza los datos de un propietario existente. */
    Propietario actualizarPropietario(UUID id, Propietario propietario);

    /** Lista todos los propietarios registrados. */
    Page<Propietario> listarPropietarios(Pageable pageable);
}
