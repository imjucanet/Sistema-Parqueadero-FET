package com.fet.parqueadero.adapter.out.persistence.repository;

import com.fet.parqueadero.adapter.out.persistence.entity.MovimientoEntity;
import com.fet.parqueadero.domain.model.EstadoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Repositorio Spring Data de la entidad {@code MOVIMIENTO}. */
public interface MovimientoJpaRepository extends JpaRepository<MovimientoEntity, UUID> {

    Optional<MovimientoEntity> findFirstByVehiculoIdAndEstadoOrderByFechaEntradaDesc(
            UUID vehiculoId, EstadoMovimiento estado);

    Page<MovimientoEntity> findAllByEstadoOrderByFechaEntradaDesc(EstadoMovimiento estado, Pageable pageable);

    Page<MovimientoEntity> findAllByOrderByFechaEntradaDesc(Pageable pageable);
}
