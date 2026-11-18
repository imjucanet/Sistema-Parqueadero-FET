package com.fet.parqueadero.adapter.out.persistence.entity;

import com.fet.parqueadero.domain.model.EstadoMovimiento;
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

import java.time.OffsetDateTime;
import java.util.UUID;

/** Entidad JPA que representa la tabla {@code MOVIMIENTO}. */
@Entity
@Table(
        name = "MOVIMIENTO",
        indexes = {
                @Index(name = "IDX_MOVIMIENTO_VEHICULO", columnList = "VEHICULO_ID"),
                @Index(name = "IDX_MOVIMIENTO_ESTADO", columnList = "ESTADO")
        })
public class MovimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "VEHICULO_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_MOVIMIENTO_VEHICULO"))
    private VehiculoEntity vehiculo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ESPACIO_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_MOVIMIENTO_ESPACIO"))
    private EspacioEntity espacio;

    @Column(name = "FECHA_ENTRADA", nullable = false)
    private OffsetDateTime fechaEntrada;

    @Column(name = "FECHA_SALIDA")
    private OffsetDateTime fechaSalida;

    @Column(name = "TIEMPO_PERMANENCIA_MINUTOS")
    private Long tiempoPermanenciaMinutos;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 30)
    private EstadoMovimiento estado;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public VehiculoEntity getVehiculo() {
        return vehiculo;
    }

    public void setVehiculo(VehiculoEntity vehiculo) {
        this.vehiculo = vehiculo;
    }

    public EspacioEntity getEspacio() {
        return espacio;
    }

    public void setEspacio(EspacioEntity espacio) {
        this.espacio = espacio;
    }

    public OffsetDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(OffsetDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public OffsetDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(OffsetDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public Long getTiempoPermanenciaMinutos() {
        return tiempoPermanenciaMinutos;
    }

    public void setTiempoPermanenciaMinutos(Long tiempoPermanenciaMinutos) {
        this.tiempoPermanenciaMinutos = tiempoPermanenciaMinutos;
    }

    public EstadoMovimiento getEstado() {
        return estado;
    }

    public void setEstado(EstadoMovimiento estado) {
        this.estado = estado;
    }
}