package com.fet.auditoria.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEventoEntity, String> {
}
