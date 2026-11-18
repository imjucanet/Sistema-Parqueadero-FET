package com.fet.parqueadero.application.port.in;

import com.fet.parqueadero.domain.model.Movimiento;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Casos de uso de gestión de movimientos de entrada y salida. */
public interface GestionMovimientosUseCase {

    /**
     * Registra la entrada de un vehículo: valida el vehículo, persiste el
     * movimiento y publica {@code VEHICULO_INGRESADO}.
     *
     * @param vehiculoId identificador del vehículo que ingresa
     * @param espacioId  espacio que el vehículo ya ocupó; si es {@code null} se
     *                   asigna automáticamente el primer espacio disponible
     */
    Movimiento registrarEntrada(UUID vehiculoId, UUID espacioId);

    /**
     * Registra la salida de un vehículo: finaliza el movimiento, calcula la
     * permanencia, libera el espacio y publica {@code VEHICULO_SALIDO}.
     */
    Movimiento registrarSalida(UUID movimientoId);

    /** Obtiene un movimiento por su identificador. Lanza {@code RecursoNoEncontradoException} si no existe. */
    Movimiento obtenerMovimiento(UUID id);

    /** Lista los movimientos actualmente activos (vehículos dentro). */
    Page<Movimiento> listarMovimientosActivos(Pageable pageable);

    /** Lista el historial completo de entradas y salidas. */
    Page<Movimiento> listarHistorialMovimientos(Pageable pageable);
}