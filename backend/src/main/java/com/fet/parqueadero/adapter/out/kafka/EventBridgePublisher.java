package com.fet.parqueadero.adapter.out.kafka;

import com.fet.parqueadero.application.port.out.EventPublisherPort;
import com.fet.parqueadero.application.port.out.MovimientoEvento;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Implementación del puerto {@link EventPublisherPort} que traduce el evento de
 * dominio en un evento de aplicación de Spring.
 *
 * <p>El publicación definitiva en Kafka queda delegada a un listener con
 * {@code @TransactionalEventListener(AFTER_COMMIT)}, garantizando que el evento
 * solo se publica después de que la operación fue validada y persistida.</p>
 */
@Component
public class EventBridgePublisher implements EventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventBridgePublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publicarMovimiento(MovimientoEvento evento) {
        applicationEventPublisher.publishEvent(evento);
    }
}