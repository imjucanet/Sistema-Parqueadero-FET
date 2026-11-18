package com.fet.parqueadero.adapter.out.persistence.repository;

import com.fet.parqueadero.adapter.out.persistence.entity.PropietarioEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de la entidad {@code PROPIETARIO}. */
public interface PropietarioJpaRepository extends JpaRepository<PropietarioEntity, UUID> {

    Optional<PropietarioEntity> findByNumeroDocumento(String numeroDocumento);
}
