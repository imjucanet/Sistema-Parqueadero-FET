package com.fet.auditoria.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fet.auditoria.event.MovimientoEvento;
import com.fet.auditoria.exception.EventoInvalidoException;
import com.fet.auditoria.service.AuditoriaEventoService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume el tópico {@code parqueadero.movimientos}: parsea y valida la
 * estructura del evento (contrato en {@code 03_Kafka/eventos.md}) y delega la
 * idempotencia y persistencia en {@link AuditoriaEventoService}.
 *
 * <p>Un mensaje que no cumple el contrato lanza {@link EventoInvalidoException},
 * que el {@code DefaultErrorHandler} enruta directo al dead-letter sin
 * reintentar ni bloquear los mensajes válidos siguientes.</p>
 */
@Component
public class MovimientoEventoListener {

    private static final Logger log = LoggerFactory.getLogger(MovimientoEventoListener.class);

    private final ObjectMapper objectMapper;
    private final AuditoriaEventoService auditoriaEventoService;

    public MovimientoEventoListener(ObjectMapper objectMapper, AuditoriaEventoService auditoriaEventoService) {
        this.objectMapper = objectMapper;
        this.auditoriaEventoService = auditoriaEventoService;
    }

    @KafkaListener(topics = "${auditoria.topico-movimientos}")
    public void escuchar(ConsumerRecord<String, String> record) {
        MovimientoEvento evento = parsearYValidar(record.value());
        log.debug("Evento recibido: topic={} partition={} offset={} eventId={} eventType={}",
                record.topic(), record.partition(), record.offset(), evento.eventId(), evento.eventType());
        auditoriaEventoService.procesar(evento);
    }

    private MovimientoEvento parsearYValidar(String payload) {
        MovimientoEvento evento;
        try {
            evento = objectMapper.readValue(payload, MovimientoEvento.class);
        } catch (JsonProcessingException ex) {
            throw new EventoInvalidoException("Mensaje no es un MovimientoEvento válido: " + ex.getOriginalMessage(), ex);
        }

        if (evento.eventId() == null || evento.eventId().isBlank()) {
            throw new EventoInvalidoException("El evento no tiene eventId.");
        }
        if (evento.eventType() == null) {
            throw new EventoInvalidoException("El evento " + evento.eventId() + " no tiene eventType.");
        }
        if (evento.timestamp() == null) {
            throw new EventoInvalidoException("El evento " + evento.eventId() + " no tiene timestamp.");
        }
        if (evento.movimientoId() == null || evento.vehiculoId() == null || evento.espacioId() == null) {
            throw new EventoInvalidoException("El evento " + evento.eventId() + " tiene identificadores obligatorios ausentes.");
        }
        if (isBlank(evento.placa()) || isBlank(evento.espacioNumero())) {
            throw new EventoInvalidoException("El evento " + evento.eventId() + " tiene placa/espacioNumero ausentes.");
        }
        return evento;
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }
}
