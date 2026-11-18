package com.fet.parqueadero.adapter.out.persistence.repository;

import com.fet.parqueadero.adapter.out.persistence.entity.EspacioEntity;
import com.fet.parqueadero.domain.model.EstadoEspacio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repositorio Spring Data de la entidad {@code ESPACIO}. */
public interface EspacioJpaRepository extends JpaRepository<EspacioEntity, UUID> {

    List<EspacioEntity> findAllByOrderByNumeroAsc();

    Page<EspacioEntity> findAll(Pageable pageable);

    Page<EspacioEntity> findByEstadoOrderByNumeroAsc(EstadoEspacio estado, Pageable pageable);

    Optional<EspacioEntity> findFirstByEstadoOrderByNumeroAsc(EstadoEspacio estado);

    Optional<EspacioEntity> findByNumero(String numero);
}
