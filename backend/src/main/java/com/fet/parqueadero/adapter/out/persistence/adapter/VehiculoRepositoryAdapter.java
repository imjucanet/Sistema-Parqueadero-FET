package com.fet.parqueadero.adapter.out.persistence.adapter;

import com.fet.parqueadero.adapter.out.persistence.entity.VehiculoEntity;
import com.fet.parqueadero.adapter.out.persistence.mapper.VehiculoEntityMapper;
import com.fet.parqueadero.adapter.out.persistence.repository.PropietarioJpaRepository;
import com.fet.parqueadero.adapter.out.persistence.repository.VehiculoJpaRepository;
import com.fet.parqueadero.application.port.out.VehiculoRepositoryPort;
import com.fet.parqueadero.domain.model.Vehiculo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador de persistencia de vehículos sobre {@code VEHICULO}. */
@Repository
public class VehiculoRepositoryAdapter implements VehiculoRepositoryPort {

    private final VehiculoJpaRepository jpaRepository;
    private final PropietarioJpaRepository propietarioJpaRepository;

    public VehiculoRepositoryAdapter(
            VehiculoJpaRepository jpaRepository,
            PropietarioJpaRepository propietarioJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.propietarioJpaRepository = propietarioJpaRepository;
    }

    @Override
    public Vehiculo save(Vehiculo vehiculo) {
        VehiculoEntity entidad = VehiculoEntityMapper.toEntity(vehiculo);
        entidad.setPropietario(propietarioJpaRepository.getReferenceById(vehiculo.propietarioId()));
        return VehiculoEntityMapper.toDomain(jpaRepository.save(entidad));
    }

    @Override
    public Optional<Vehiculo> findById(UUID id) {
        return jpaRepository.findById(id).map(VehiculoEntityMapper::toDomain);
    }

    @Override
    public List<Vehiculo> findAllById(Collection<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(VehiculoEntityMapper::toDomain).toList();
    }

    @Override
    public Optional<Vehiculo> findByPlaca(String placa) {
        return jpaRepository.findByPlaca(placa).map(VehiculoEntityMapper::toDomain);
    }

    @Override
    public Page<Vehiculo> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(VehiculoEntityMapper::toDomain);
    }

    @Override
    public List<Vehiculo> findByPropietarioId(UUID propietarioId) {
        return jpaRepository.findByPropietarioId(propietarioId).stream()
                .map(VehiculoEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Vehiculo> findByPropietarioIds(Collection<UUID> propietarioIds) {
        return jpaRepository.findByPropietarioIdIn(propietarioIds).stream()
                .map(VehiculoEntityMapper::toDomain)
                .toList();
    }
}