package com.fet.parqueadero.domain.service;

import com.fet.parqueadero.application.port.in.GestionMovimientosUseCase;
import com.fet.parqueadero.application.port.out.EspacioRepositoryPort;
import com.fet.parqueadero.application.port.out.EventPublisherPort;
import com.fet.parqueadero.application.port.out.MovimientoEvento;
import com.fet.parqueadero.application.port.out.MovimientoRepositoryPort;
import com.fet.parqueadero.application.port.out.TipoEventoMovimiento;
import com.fet.parqueadero.application.port.out.VehiculoRepositoryPort;
import com.fet.parqueadero.domain.exception.EntidadInvalidaException;
import com.fet.parqueadero.domain.exception.EspacioNoDisponibleException;
import com.fet.parqueadero.domain.exception.MovimientoNoActivoException;
import com.fet.parqueadero.domain.exception.RecursoNoEncontradoException;
import com.fet.parqueadero.domain.exception.VehiculoYaEnParqueaderoException;
import com.fet.parqueadero.domain.model.Espacio;
import com.fet.parqueadero.domain.model.EstadoEspacio;
import com.fet.parqueadero.domain.model.EstadoMovimiento;
import com.fet.parqueadero.domain.model.Movimiento;
import com.fet.parqueadero.domain.model.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Implementación de los casos de uso de movimientos.
 *
 * <p>La aplicación de la operación y la publicación del evento ocurren en la
 * misma transacción: la persistencia se confirma primero y el evento se
 * publica {@code después del commit} (invariante de arquitectura).</p>
 */
@Service
public class GestionMovimientosService implements GestionMovimientosUseCase {

    private final VehiculoRepositoryPort vehiculos;
    private final EspacioRepositoryPort espacios;
    private final MovimientoRepositoryPort movimientos;
    private final EventPublisherPort eventPublisher;

    public GestionMovimientosService(
            VehiculoRepositoryPort vehiculos,
            EspacioRepositoryPort espacios,
            MovimientoRepositoryPort movimientos,
            EventPublisherPort eventPublisher) {
        this.vehiculos = vehiculos;
        this.espacios = espacios;
        this.movimientos = movimientos;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Movimiento registrarEntrada(UUID vehiculoId, UUID espacioId) {
        Vehiculo vehiculo = vehiculos.findById(vehiculoId)
                .orElseThrow(() -> new EntidadInvalidaException(
                        "VEHICULO_NO_ENCONTRADO",
                        "No existe el vehículo con id %s.".formatted(vehiculoId)));

        movimientos.findActivoPorVehiculo(vehiculoId).ifPresent(ignored -> {
            throw new VehiculoYaEnParqueaderoException();
        });

        Espacio espacio = espacioId != null ? buscarEspacioElegido(espacioId) : asignarPrimerDisponible();

        OffsetDateTime ahora = OffsetDateTime.now();
        Movimiento movimiento = movimientos.save(new Movimiento(
                null,
                vehiculoId,
                espacio.id(),
                ahora,
                null,
                null,
                EstadoMovimiento.EN_PARQUEADERO));

        espacios.save(new Espacio(espacio.id(), espacio.numero(), EstadoEspacio.OCUPADO));

        eventPublisher.publicarMovimiento(new MovimientoEvento(
                UUID.randomUUID().toString(),
                TipoEventoMovimiento.VEHICULO_INGRESADO,
                ahora,
                movimiento.id(),
                vehiculo.id(),
                vehiculo.placa(),
                espacio.id(),
                espacio.numero(),
                null));

        return movimiento;
    }

    private Espacio asignarPrimerDisponible() {
        return espacios.findFirstDisponible().orElseThrow(EspacioNoDisponibleException::new);
    }

    private Espacio buscarEspacioElegido(UUID espacioId) {
        Espacio espacio = espacios.findById(espacioId)
                .orElseThrow(() -> new EntidadInvalidaException(
                        "ESPACIO_NO_ENCONTRADO",
                        "No existe el espacio con id %s.".formatted(espacioId)));
        if (espacio.estado() != EstadoEspacio.DISPONIBLE) {
            throw new EspacioNoDisponibleException(
                    "El espacio %s no está disponible.".formatted(espacio.numero()));
        }
        return espacio;
    }

    @Override
    @Transactional
    public Movimiento registrarSalida(UUID movimientoId) {
        Movimiento movimiento = movimientos.findById(movimientoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("el movimiento", movimientoId));

        if (movimiento.estado() != EstadoMovimiento.EN_PARQUEADERO) {
            throw new MovimientoNoActivoException();
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        long permanencia = ChronoUnit.MINUTES.between(movimiento.fechaEntrada(), ahora);

        Movimiento finalizado = movimientos.save(new Movimiento(
                movimiento.id(),
                movimiento.vehiculoId(),
                movimiento.espacioId(),
                movimiento.fechaEntrada(),
                ahora,
                permanencia,
                EstadoMovimiento.FINALIZADO));

        Espacio espacio = espacios.findById(movimiento.espacioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("el espacio", movimiento.espacioId()));
        espacios.save(new Espacio(espacio.id(), espacio.numero(), EstadoEspacio.DISPONIBLE));

        Vehiculo vehiculo = vehiculos.findById(movimiento.vehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("el vehículo", movimiento.vehiculoId()));

        eventPublisher.publicarMovimiento(new MovimientoEvento(
                UUID.randomUUID().toString(),
                TipoEventoMovimiento.VEHICULO_SALIDO,
                ahora,
                finalizado.id(),
                vehiculo.id(),
                vehiculo.placa(),
                espacio.id(),
                espacio.numero(),
                permanencia));

        return finalizado;
    }

    @Override
    @Transactional(readOnly = true)
    public Movimiento obtenerMovimiento(UUID id) {
        return movimientos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("el movimiento", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Movimiento> listarMovimientosActivos(Pageable pageable) {
        return movimientos.findAllActivos(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Movimiento> listarHistorialMovimientos(Pageable pageable) {
        return movimientos.findAllHistorial(pageable);
    }
}