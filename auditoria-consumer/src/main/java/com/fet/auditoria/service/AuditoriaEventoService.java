package com.fet.auditoria.service;

import com.fet.auditoria.event.MovimientoEvento;
import com.fet.auditoria.persistence.AuditoriaEventoEntity;
import com.fet.auditoria.persistence.AuditoriaEventoRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Procesa un {@link MovimientoEvento} ya validado: comprueba idempotencia por
 * {@code eventId} y persiste en {@code AUDITORIA_EVENTOS}.
 *
 * <p>Un evento duplicado no es un error: se registra y se descarta en
 * silencio, ya que Kafka garantiza entrega "at-least-once" y el mismo evento
 * puede llegar más de una vez (reintentos del productor, rebalanceos, etc).</p>
 */
@Service
public class AuditoriaEventoService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaEventoService.class);

    private final AuditoriaEventoRepository repositorio;
    private final Counter eventosProcesados;
    private final Counter eventosDuplicados;

    public AuditoriaEventoService(AuditoriaEventoRepository repositorio, MeterRegistry meterRegistry) {
        this.repositorio = repositorio;
        this.eventosProcesados = Counter.builder("auditoria.eventos.procesados")
                .description("Eventos de movimiento persistidos en AUDITORIA_EVENTOS")
                .register(meterRegistry);
        this.eventosDuplicados = Counter.builder("auditoria.eventos.duplicados")
                .description("Eventos descartados por ya existir (mismo eventId)")
                .register(meterRegistry);
    }

    @Transactional
    public void procesar(MovimientoEvento evento) {
        if (repositorio.existsById(evento.eventId())) {
            registrarDuplicado(evento);
            return;
        }

        try {
            repositorio.save(aEntidad(evento));
            eventosProcesados.increment();
            log.info("Evento {} ({}) persistido en AUDITORIA_EVENTOS. movimientoId={}, placa={}",
                    evento.eventId(), evento.eventType(), evento.movimientoId(), evento.placa());
        } catch (DataIntegrityViolationException ex) {
            // Carrera entre instancias del mismo consumer-group: otra ya insertó este eventId.
            registrarDuplicado(evento);
        }
    }

    private void registrarDuplicado(MovimientoEvento evento) {
        eventosDuplicados.increment();
        log.info("Evento duplicado ignorado: eventId={} eventType={}", evento.eventId(), evento.eventType());
    }

    private AuditoriaEventoEntity aEntidad(MovimientoEvento evento) {
        AuditoriaEventoEntity entidad = new AuditoriaEventoEntity();
        entidad.setEventId(evento.eventId());
        entidad.setEventType(evento.eventType());
        entidad.setEventTimestamp(evento.timestamp());
        entidad.setMovimientoId(evento.movimientoId());
        entidad.setVehiculoId(evento.vehiculoId());
        entidad.setPlaca(evento.placa());
        entidad.setEspacioId(evento.espacioId());
        entidad.setEspacioNumero(evento.espacioNumero());
        entidad.setTiempoPermanenciaMinutos(evento.tiempoPermanenciaMinutos());
        entidad.setFechaProcesamiento(OffsetDateTime.now());
        return entidad;
    }
}
