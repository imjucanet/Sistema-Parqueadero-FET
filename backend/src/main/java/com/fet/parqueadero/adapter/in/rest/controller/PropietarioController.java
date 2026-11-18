package com.fet.parqueadero.adapter.in.rest.controller;

import com.fet.parqueadero.adapter.in.rest.api.PropietariosApi;
import com.fet.parqueadero.adapter.in.rest.dto.Propietario;
import com.fet.parqueadero.adapter.in.rest.dto.PropietarioPage;
import com.fet.parqueadero.adapter.in.rest.dto.PropietarioRequest;
import com.fet.parqueadero.adapter.in.rest.mapper.PropietarioRestMapper;
import com.fet.parqueadero.application.port.in.GestionPropietariosUseCase;
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

/** Controlador REST de propietarios (adaptador de entrada). */
@RestController
@Validated
public class PropietarioController implements PropietariosApi {

    private final GestionPropietariosUseCase useCase;

    public PropietarioController(GestionPropietariosUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public ResponseEntity<Propietario> registrarPropietario(@Valid @RequestBody PropietarioRequest propietarioRequest) {
        Propietario creado = PropietarioRestMapper.toDto(
                useCase.registrarPropietario(PropietarioRestMapper.toDomain(propietarioRequest)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Override
    public ResponseEntity<PropietarioPage> listarPropietarios(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        Page<Propietario> propietarios = useCase.listarPropietarios(pageable).map(PropietarioRestMapper::toDto);
        return ResponseEntity.ok(PropietarioRestMapper.toPageDto(propietarios));
    }

    @Override
    public ResponseEntity<Propietario> obtenerPropietario(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(PropietarioRestMapper.toDto(useCase.obtenerPropietario(id)));
    }

    @Override
    public ResponseEntity<Propietario> actualizarPropietario(
            @PathVariable("id") UUID id,
            @Valid @RequestBody PropietarioRequest propietarioRequest) {
        Propietario actualizado = PropietarioRestMapper.toDto(
                useCase.actualizarPropietario(id, PropietarioRestMapper.toDomain(propietarioRequest)));
        return ResponseEntity.ok(actualizado);
    }
}