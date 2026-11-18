package com.fet.parqueadero.adapter.in.graphql;

import com.fet.parqueadero.application.port.out.MovimientoEvento;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * Alimenta la subscription GraphQL {@code movimientoRegistrado}.
 *
 * <p>Reutiliza el mismo evento de aplicación ({@link MovimientoEvento}) que ya
 * dispara la publicación en Kafka: se escucha con
 * {@code @TransactionalEventListener(AFTER_COMMIT)} para garantizar que solo
 * se emite tras confirmar la operación en PostgreSQL, igual que el productor
 * Kafka existente.</p>
 *
 * <p>El sink es {@code directBestEffort}: una subscription entrega hechos en
 * vivo, así que un movimiento ocurrido mientras nadie escuchaba se descarta en
 * lugar de acumularse. Con {@code onBackpressureBuffer} el primer cliente que
 * se conectaba recibía de golpe todo el historial pendiente, que no es lo que
 * un observador en tiempo real espera; para consultar lo ya ocurrido está
 * {@code historialMovimientos}.</p>
 */
@Component
public class MovimientoEventPublisher {

    private final Sinks.Many<MovimientoEvento> sink = Sinks.many().multicast().directBestEffort();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMovimiento(MovimientoEvento evento) {
        sink.tryEmitNext(evento);
    }

    public Flux<MovimientoEvento> stream() {
        return sink.asFlux();
    }
}
