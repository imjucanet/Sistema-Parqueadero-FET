package com.fet.parqueadero.adapter.out.kafka;

import com.fet.parqueadero.application.port.out.MovimientoEvento;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Adaptador de salida que publica los eventos de movimiento en el tópico Kafka
 * {@code parqueadero.movimientos}.
 *
 * <p>El evento se envía únicamente {@code después del commit} de la transacción
 * que persistió la operación: si la transacción se revierte, el evento nunca se
 * publica (PostgreSQL es la fuente de verdad operacional).</p>
 */
@Component
public class KafkaProducerAdapter {

    public static final String TOPICO_MOVIMIENTOS = "parqueadero.movimientos";

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerAdapter.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaProducerAdapter(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void publicarMovimiento(MovimientoEvento evento) {
        try {
            String payload = objectMapper.writeValueAsString(evento);
            kafkaTemplate.send(TOPICO_MOVIMIENTOS, evento.eventId(), payload)
                    .whenComplete((resultado, error) -> {
                        if (error != null) {
                            log.warn(
                                    "No fue posible publicar el evento {} en Kafka: {}",
                                    evento.eventId(),
                                    error.getMessage());
                        }
                    });
        } catch (JsonProcessingException ex) {
            log.error("No fue posible serializar el evento {} para Kafka.", evento.eventId(), ex);
        }
    }
}