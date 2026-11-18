package com.fet.parqueadero.adapter.in.rest.controller;

import com.fet.parqueadero.adapter.in.rest.api.EspaciosApi;
import com.fet.parqueadero.adapter.in.rest.dto.Espacio;
import com.fet.parqueadero.adapter.in.rest.dto.EspacioPage;
import com.fet.parqueadero.adapter.in.rest.dto.EspacioRequest;
import com.fet.parqueadero.adapter.in.rest.mapper.EspacioRestMapper;
import com.fet.parqueadero.application.port.in.GestionEspaciosUseCase;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Controlador REST de espacios (adaptador de entrada). */
@RestController
@Validated
public class EspacioController implements EspaciosApi {

    private final GestionEspaciosUseCase useCase;

    public EspacioController(GestionEspaciosUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public ResponseEntity<Espacio> registrarEspacio(@Valid @RequestBody EspacioRequest espacioRequest) {
        Espacio creado = EspacioRestMapper.toDto(useCase.registrarEspacio(EspacioRestMapper.toDomain(espacioRequest)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Override
    public ResponseEntity<EspacioPage> listarEspacios(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        Page<Espacio> espacios = useCase.listarEspacios(pageable).map(EspacioRestMapper::toDto);
        return ResponseEntity.ok(EspacioRestMapper.toPageDto(espacios));
    }

    @Override
    public ResponseEntity<EspacioPage> listarEspaciosDisponibles(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page == null ? 0 : page, size == null ? 20 : size);
        Page<Espacio> espacios = useCase.listarEspaciosDisponibles(pageable).map(EspacioRestMapper::toDto);
        return ResponseEntity.ok(EspacioRestMapper.toPageDto(espacios));
    }
}