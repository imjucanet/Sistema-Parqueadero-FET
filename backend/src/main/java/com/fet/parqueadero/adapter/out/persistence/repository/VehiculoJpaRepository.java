package com.fet.parqueadero.adapter.out.persistence.repository;

import com.fet.parqueadero.adapter.out.persistence.entity.VehiculoEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repositorio Spring Data de la entidad {@code VEHICULO}. */
public interface VehiculoJpaRepository extends JpaRepository<VehiculoEntity, UUID> {

    Optional<VehiculoEntity> findByPlaca(String placa);

    List<VehiculoEntity> findByPropietarioId(UUID propietarioId);

    List<VehiculoEntity> findByPropietarioIdIn(Collection<UUID> propietarioIds);
}
