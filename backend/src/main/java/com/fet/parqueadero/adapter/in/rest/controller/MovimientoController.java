package com.fet.parqueadero.adapter.in.rest.controller;

import com.fet.parqueadero.adapter.in.rest.api.MovimientosApi;
import com.fet.parqueadero.adapter.in.rest.dto.EntradaRequest;
import com.fet.parqueadero.adapter.in.rest.dto.Movimiento;
import com.fet.parqueadero.adapter.in.rest.dto.MovimientoPage;
import com.fet.parqueadero.adapter.in.rest.mapper.MovimientoRestMapper;
import com.fet.parqueadero.application.port.in.GestionMovimientosUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST de movimientos (adaptador de entrada). */
@RestController
@Validated
public class MovimientoController implements MovimientosApi {

    private final GestionMovimientosUseCase useCase;

    public MovimientoController(GestionMovimientosUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public ResponseEntity<Movimiento> registrarEntrada(@Valid @RequestBody EntradaRequest entradaRequest) {
        Movimiento creado = MovimientoRestMapper.toDto(
                useCase.registrarEntrada(entradaRequest.getVehiculoId(), entradaRequest.getEspacioId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Override
    public ResponseEntity<Movimiento> registrarSalida(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(MovimientoRestMapper.toDto(useCase.registrarSalida(id)));
    }

    @Override
    public ResponseEntity<MovimientoPage> listarMovimientosActivos(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        Page<Movimiento> movimientos = useCase.listarMovimientosActivos(pageable).map(MovimientoRestMapper::toDto);
        return ResponseEntity.ok(MovimientoRestMapper.toPageDto(movimientos));
    }

    @Override
    public ResponseEntity<MovimientoPage> listarHistorialMovimientos(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        Page<Movimiento> movimientos = useCase.listarHistorialMovimientos(pageable).map(MovimientoRestMapper::toDto);
        return ResponseEntity.ok(MovimientoRestMapper.toPageDto(movimientos));
    }
}