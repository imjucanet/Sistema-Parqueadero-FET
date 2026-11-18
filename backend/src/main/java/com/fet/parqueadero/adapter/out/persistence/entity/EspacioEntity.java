package com.fet.parqueadero.adapter.out.persistence.entity;

import com.fet.parqueadero.domain.model.EstadoEspacio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Entidad JPA que representa la tabla {@code ESPACIO}. */
@Entity
@Table(name = "ESPACIO")
public class EspacioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "NUMERO", nullable = false, unique = true, length = 10)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 30)
    private EstadoEspacio estado;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public EstadoEspacio getEstado() {
        return estado;
    }

    public void setEstado(EstadoEspacio estado) {
        this.estado = estado;
    }
}