package com.fet.parqueadero.application.port.out;

import com.fet.parqueadero.domain.model.Movimiento;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** Puerto de salida para la persistencia de movimientos. */
public interface MovimientoRepositoryPort {

    Movimiento save(Movimiento movimiento);

    Optional<Movimiento> findById(UUID id);

    /** Busca el movimiento activo (EN_PARQUEADERO) de un vehículo. */
    Optional<Movimiento> findActivoPorVehiculo(UUID vehiculoId);

    /** Lista los movimientos activos ordenados por fecha de entrada descendente. */
    Page<Movimiento> findAllActivos(Pageable pageable);

    /** Lista el historial de movimientos ordenado por fecha de entrada descendente. */
    Page<Movimiento> findAllHistorial(Pageable pageable);
}
