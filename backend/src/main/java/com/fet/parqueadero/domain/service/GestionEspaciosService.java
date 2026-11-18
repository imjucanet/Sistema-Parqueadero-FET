package com.fet.parqueadero.domain.service;

import com.fet.parqueadero.application.port.in.GestionEspaciosUseCase;
import com.fet.parqueadero.application.port.out.EspacioRepositoryPort;
import com.fet.parqueadero.domain.exception.EntidadInvalidaException;
import com.fet.parqueadero.domain.exception.RecursoDuplicadoException;
import com.fet.parqueadero.domain.exception.RecursoNoEncontradoException;
import com.fet.parqueadero.domain.model.Espacio;
import com.fet.parqueadero.domain.model.EstadoEspacio;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación de los casos de uso de gestión y consulta de espacios.
 *
 * <p>Cada operación es transaccional: la verificación de número duplicado y el
 * guardado deben ocurrir en la misma transacción.</p>
 */
@Service
public class GestionEspaciosService implements GestionEspaciosUseCase {

    private final EspacioRepositoryPort espacios;

    public GestionEspaciosService(EspacioRepositoryPort espacios) {
        this.espacios = espacios;
    }

    @Override
    @Transactional
    public Espacio registrarEspacio(Espacio espacio) {
        String numero = normalizar(espacio.numero());
        if (numero == null || numero.isEmpty()) {
            throw new EntidadInvalidaException(
                    "VALIDACION", "El número del espacio no puede estar vacío.");
        }
        espacios.findByNumero(numero).ifPresent(existente -> {
            throw new RecursoDuplicadoException(
                    "ESPACIO_YA_EXISTE",
                    "Ya existe un espacio registrado con el número %s.".formatted(numero));
        });
        Espacio nuevo = new Espacio(null, numero, EstadoEspacio.DISPONIBLE);
        return espacios.save(nuevo);
    }

    @Override
    @Transactional(readOnly = true)
    public Espacio obtenerEspacio(UUID id) {
        return espacios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el espacio", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Espacio> obtenerEspacios(Collection<UUID> ids) {
        return espacios.findAllById(ids).stream()
                .collect(Collectors.toMap(Espacio::id, Function.identity()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Espacio> listarEspacios(Pageable pageable) {
        return espacios.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Espacio> listarEspaciosDisponibles(Pageable pageable) {
        return espacios.findAllDisponibles(pageable);
    }

    private static String normalizar(String valor) {
        return valor == null ? null : valor.trim().toUpperCase();
    }
}
