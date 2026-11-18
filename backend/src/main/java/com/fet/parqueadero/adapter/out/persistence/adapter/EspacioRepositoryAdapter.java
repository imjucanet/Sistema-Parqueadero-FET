package com.fet.parqueadero.adapter.out.persistence.adapter;

import com.fet.parqueadero.adapter.out.persistence.entity.EspacioEntity;
import com.fet.parqueadero.adapter.out.persistence.mapper.EspacioEntityMapper;
import com.fet.parqueadero.adapter.out.persistence.repository.EspacioJpaRepository;
import com.fet.parqueadero.application.port.out.EspacioRepositoryPort;
import com.fet.parqueadero.domain.model.EstadoEspacio;
import com.fet.parqueadero.domain.model.Espacio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador de persistencia de espacios sobre {@code ESPACIO}. */
@Repository
public class EspacioRepositoryAdapter implements EspacioRepositoryPort {

    private final EspacioJpaRepository jpaRepository;

    public EspacioRepositoryAdapter(EspacioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Page<Espacio> findAll(Pageable pageable) {
        Page<EspacioEntity> page = jpaRepository.findAll(pageable);
        return page.map(EspacioEntityMapper::toDomain);
    }

    @Override
    public Page<Espacio> findAllDisponibles(Pageable pageable) {
        Page<EspacioEntity> page = jpaRepository.findByEstadoOrderByNumeroAsc(EstadoEspacio.DISPONIBLE, pageable);
        return page.map(EspacioEntityMapper::toDomain);
    }

    @Override
    public Optional<Espacio> findFirstDisponible() {
        return jpaRepository.findFirstByEstadoOrderByNumeroAsc(EstadoEspacio.DISPONIBLE)
                .map(EspacioEntityMapper::toDomain);
    }

    @Override
    public Optional<Espacio> findById(UUID id) {
        return jpaRepository.findById(id).map(EspacioEntityMapper::toDomain);
    }

    @Override
    public List<Espacio> findAllById(Collection<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(EspacioEntityMapper::toDomain).toList();
    }

    @Override
    public Optional<Espacio> findByNumero(String numero) {
        return jpaRepository.findByNumero(numero).map(EspacioEntityMapper::toDomain);
    }

    @Override
    public Espacio save(Espacio espacio) {
        return EspacioEntityMapper.toDomain(jpaRepository.save(EspacioEntityMapper.toEntity(espacio)));
    }
}