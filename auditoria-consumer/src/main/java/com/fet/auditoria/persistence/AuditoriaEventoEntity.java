package com.fet.auditoria.persistence;

import com.fet.auditoria.event.TipoEventoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Entidad JPA que representa la tabla {@code AUDITORIA_EVENTOS}. */
@Entity
@Table(
        name = "AUDITORIA_EVENTOS",
        indexes = {
                @Index(name = "IDX_AUDITORIA_MOVIMIENTO", columnList = "MOVIMIENTO_ID"),
                @Index(name = "IDX_AUDITORIA_EVENT_TYPE", columnList = "EVENT_TYPE")
        })
public class AuditoriaEventoEntity {

    @Id
    @Column(name = "EVENT_ID", length = 36, nullable = false)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "EVENT_TYPE", nullable = false, length = 30)
    private TipoEventoMovimiento eventType;

    @Column(name = "EVENT_TIMESTAMP", nullable = false)
    private OffsetDateTime eventTimestamp;

    @Column(name = "MOVIMIENTO_ID", nullable = false)
    private Long movimientoId;

    @Column(name = "VEHICULO_ID", nullable = false)
    private Long vehiculoId;

    @Column(name = "PLACA", nullable = false, length = 20)
    private String placa;

    @Column(name = "ESPACIO_ID", nullable = false)
    private Long espacioId;

    @Column(name = "ESPACIO_NUMERO", nullable = false, length = 10)
    private String espacioNumero;

    @Column(name = "TIEMPO_PERMANENCIA_MINUTOS")
    private Long tiempoPermanenciaMinutos;

    @Column(name = "FECHA_PROCESAMIENTO", nullable = false)
    private OffsetDateTime fechaProcesamiento;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public TipoEventoMovimiento getEventType() {
        return eventType;
    }

    public void setEventType(TipoEventoMovimiento eventType) {
        this.eventType = eventType;
    }

    public OffsetDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(OffsetDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public Long getMovimientoId() {
        return movimientoId;
    }

    public void setMovimientoId(Long movimientoId) {
        this.movimientoId = movimientoId;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public Long getEspacioId() {
        return espacioId;
    }

    public void setEspacioId(Long espacioId) {
        this.espacioId = espacioId;
    }

    public String getEspacioNumero() {
        return espacioNumero;
    }

    public void setEspacioNumero(String espacioNumero) {
        this.espacioNumero = espacioNumero;
    }

    public Long getTiempoPermanenciaMinutos() {
        return tiempoPermanenciaMinutos;
    }

    public void setTiempoPermanenciaMinutos(Long tiempoPermanenciaMinutos) {
        this.tiempoPermanenciaMinutos = tiempoPermanenciaMinutos;
    }

    public OffsetDateTime getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(OffsetDateTime fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }
}
