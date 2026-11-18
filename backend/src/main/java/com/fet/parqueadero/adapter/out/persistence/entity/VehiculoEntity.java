package com.fet.parqueadero.adapter.out.persistence.entity;

import com.fet.parqueadero.domain.model.EstadoVehiculo;
import com.fet.parqueadero.domain.model.TipoVehiculo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/** Entidad JPA que representa la tabla {@code VEHICULO}. */
@Entity
@Table(
        name = "VEHICULO",
        indexes = {
                @Index(name = "IDX_VEHICULO_PROPIETARIO", columnList = "PROPIETARIO_ID"),
                @Index(name = "IDX_VEHICULO_PLACA", columnList = "PLACA")
        })
public class VehiculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "PLACA", nullable = false, length = 20, unique = true)
    private String placa;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 20)
    private TipoVehiculo tipo;

    @Column(name = "MARCA", nullable = false, length = 80)
    private String marca;

    @Column(name = "MODELO", length = 80)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoVehiculo estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "PROPIETARIO_ID",
            nullable = false,
            foreignKey = @ForeignKey(name = "FK_VEHICULO_PROPIETARIO"))
    private PropietarioEntity propietario;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }

    public void setTipo(TipoVehiculo tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public EstadoVehiculo getEstado() {
        return estado;
    }

    public void setEstado(EstadoVehiculo estado) {
        this.estado = estado;
    }

    public PropietarioEntity getPropietario() {
        return propietario;
    }

    public void setPropietario(PropietarioEntity propietario) {
        this.propietario = propietario;
    }
}