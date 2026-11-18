package com.fet.parqueadero.adapter.out.persistence.adapter;

import com.fet.parqueadero.adapter.out.persistence.entity.MovimientoEntity;
import com.fet.parqueadero.adapter.out.persistence.mapper.MovimientoEntityMapper;
import com.fet.parqueadero.adapter.out.persistence.repository.EspacioJpaRepository;
import com.fet.parqueadero.adapter.out.persistence.repository.MovimientoJpaRepository;
import com.fet.parqueadero.adapter.out.persistence.repository.VehiculoJpaRepository;
import com.fet.parqueadero.application.port.out.MovimientoRepositoryPort;
import com.fet.parqueadero.domain.model.EstadoMovimiento;
import com.fet.parqueadero.domain.model.Movimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** Adaptador de persistencia de movimientos sobre {@code MOVIMIENTO}. */
@Repository
public class MovimientoRepositoryAdapter implements MovimientoRepositoryPort {

    private final MovimientoJpaRepository jpaRepository;
    private final VehiculoJpaRepository vehiculoJpaRepository;
    private final EspacioJpaRepository espacioJpaRepository;

    public MovimientoRepositoryAdapter(
            MovimientoJpaRepository jpaRepository,
            VehiculoJpaRepository vehiculoJpaRepository,
            EspacioJpaRepository espacioJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.vehiculoJpaRepository = vehiculoJpaRepository;
        this.espacioJpaRepository = espacioJpaRepository;
    }

    @Override
    public Movimiento save(Movimiento movimiento) {
        MovimientoEntity entidad = MovimientoEntityMapper.toEntity(movimiento);
        entidad.setVehiculo(vehiculoJpaRepository.getReferenceById(movimiento.vehiculoId()));
        entidad.setEspacio(espacioJpaRepository.getReferenceById(movimiento.espacioId()));
        return MovimientoEntityMapper.toDomain(jpaRepository.save(entidad));
    }

    @Override
    public Optional<Movimiento> findById(UUID id) {
        return jpaRepository.findById(id).map(MovimientoEntityMapper::toDomain);
    }

    @Override
    public Optional<Movimiento> findActivoPorVehiculo(UUID vehiculoId) {
        return jpaRepository
                .findFirstByVehiculoIdAndEstadoOrderByFechaEntradaDesc(vehiculoId, EstadoMovimiento.EN_PARQUEADERO)
                .map(MovimientoEntityMapper::toDomain);
    }

    @Override
    public Page<Movimiento> findAllActivos(Pageable pageable) {
        return jpaRepository.findAllByEstadoOrderByFechaEntradaDesc(EstadoMovimiento.EN_PARQUEADERO, pageable)
                .map(MovimientoEntityMapper::toDomain);
    }

    @Override
    public Page<Movimiento> findAllHistorial(Pageable pageable) {
        return jpaRepository.findAllByOrderByFechaEntradaDesc(pageable)
                .map(MovimientoEntityMapper::toDomain);
    }
}