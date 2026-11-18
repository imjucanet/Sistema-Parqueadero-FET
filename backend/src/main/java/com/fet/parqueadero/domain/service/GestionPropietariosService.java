package com.fet.parqueadero.domain.service;

import com.fet.parqueadero.application.port.in.GestionPropietariosUseCase;
import com.fet.parqueadero.application.port.out.PropietarioRepositoryPort;
import com.fet.parqueadero.domain.exception.RecursoDuplicadoException;
import com.fet.parqueadero.domain.exception.RecursoNoEncontradoException;
import com.fet.parqueadero.domain.model.Propietario;
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
 * Implementación de los casos de uso de gestión de propietarios.
 *
 * <p>Cada operación es transaccional: la verificación de duplicados y el
 * guardado deben ocurrir en la misma transacción, de lo contrario dos
 * solicitudes concurrentes pueden pasar ambas la verificación.</p>
 */
@Service
public class GestionPropietariosService implements GestionPropietariosUseCase {

    private final PropietarioRepositoryPort propietarios;

    public GestionPropietariosService(PropietarioRepositoryPort propietarios) {
        this.propietarios = propietarios;
    }

    @Override
    @Transactional
    public Propietario registrarPropietario(Propietario propietario) {
        String numeroDocumento = normalizar(propietario.numeroDocumento());
        verificarDocumentoNoDuplicado(numeroDocumento, null);
        Propietario nuevo = new Propietario(
                null,
                propietario.primerNombre(),
                propietario.primerApellido(),
                construirNombreCompleto(propietario.primerNombre(), propietario.primerApellido()),
                numeroDocumento,
                propietario.tipoUsuario(),
                propietario.telefono(),
                propietario.correo());
        return propietarios.save(nuevo);
    }

    @Override
    @Transactional(readOnly = true)
    public Propietario obtenerPropietario(UUID id) {
        return buscar(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, Propietario> obtenerPropietarios(Collection<UUID> ids) {
        return propietarios.findAllById(ids).stream()
                .collect(Collectors.toMap(Propietario::id, Function.identity()));
    }

    @Override
    @Transactional
    public Propietario actualizarPropietario(UUID id, Propietario propietario) {
        buscar(id);
        String numeroDocumento = normalizar(propietario.numeroDocumento());
        verificarDocumentoNoDuplicado(numeroDocumento, id);
        Propietario actualizado = new Propietario(
                id,
                propietario.primerNombre(),
                propietario.primerApellido(),
                construirNombreCompleto(propietario.primerNombre(), propietario.primerApellido()),
                numeroDocumento,
                propietario.tipoUsuario(),
                propietario.telefono(),
                propietario.correo());
        return propietarios.save(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Propietario> listarPropietarios(Pageable pageable) {
        return propietarios.findAll(pageable);
    }

    private Propietario buscar(UUID id) {
        return propietarios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el propietario", id));
    }

    private void verificarDocumentoNoDuplicado(String numeroDocumento, UUID idActual) {
        propietarios.findByNumeroDocumento(numeroDocumento)
                .filter(existente -> !existente.id().equals(idActual))
                .ifPresent(existente -> {
                    throw new RecursoDuplicadoException(
                            "PROPIETARIO_YA_EXISTE",
                            "Ya existe un propietario registrado con el número de documento %s.".formatted(numeroDocumento));
                });
    }

    private static String normalizar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private static String construirNombreCompleto(String primerNombre, String primerApellido) {
        return "%s %s".formatted(normalizar(primerNombre), normalizar(primerApellido)).trim();
    }
}
