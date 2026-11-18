package com.fet.parqueadero.adapter.out.persistence.adapter;

import com.fet.parqueadero.adapter.out.persistence.entity.PropietarioEntity;
import com.fet.parqueadero.adapter.out.persistence.mapper.PropietarioEntityMapper;
import com.fet.parqueadero.adapter.out.persistence.repository.PropietarioJpaRepository;
import com.fet.parqueadero.application.port.out.PropietarioRepositoryPort;
import com.fet.parqueadero.domain.model.Propietario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador de persistencia de propietarios sobre {@code PROPIETARIO}. */
@Repository
public class PropietarioRepositoryAdapter implements PropietarioRepositoryPort {

    private final PropietarioJpaRepository jpaRepository;

    public PropietarioRepositoryAdapter(PropietarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Propietario save(Propietario propietario) {
        PropietarioEntity entidad = PropietarioEntityMapper.toEntity(propietario);
        return PropietarioEntityMapper.toDomain(jpaRepository.save(entidad));
    }

    @Override
    public Optional<Propietario> findById(UUID id) {
        return jpaRepository.findById(id).map(PropietarioEntityMapper::toDomain);
    }

    @Override
    public List<Propietario> findAllById(Collection<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(PropietarioEntityMapper::toDomain).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public Optional<Propietario> findByNumeroDocumento(String numeroDocumento) {
        return jpaRepository.findByNumeroDocumento(numeroDocumento).map(PropietarioEntityMapper::toDomain);
    }

    @Override
    public Page<Propietario> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(PropietarioEntityMapper::toDomain);
    }
}