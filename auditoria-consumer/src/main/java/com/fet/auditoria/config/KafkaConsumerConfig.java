package com.fet.auditoria.config;

import com.fet.auditoria.exception.EventoInvalidoException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Política de reintentos y recuperación de errores para el listener de
 * {@code parqueadero.movimientos}:
 *
 * <ul>
 *   <li>{@link EventoInvalidoException} (JSON malformado / campos ausentes /
 *       eventType desconocido) no es transitoria: se enruta directo al
 *       tópico dead-letter, sin reintentar.</li>
 *   <li>Cualquier otra excepción (p. ej. caída momentánea de PostgreSQL) se
 *       reintenta 3 veces con 1s de espera antes de ir al dead-letter.</li>
 * </ul>
 *
 * <p>En ambos casos el offset se confirma igual (el mensaje no bloquea la
 * partición) y queda registro en el tópico {@code parqueadero.movimientos.DLT}
 * y en los logs en nivel {@code ERROR}.</p>
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${auditoria.topico-movimientos}")
    private String topicoMovimientos;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate, MeterRegistry meterRegistry) {
        Counter eventosInvalidos = Counter.builder("auditoria.eventos.invalidos")
                .description("Eventos enviados al tópico dead-letter tras agotar reintentos o por ser inválidos")
                .register(meterRegistry);

        // Se fija explícitamente el destino (en vez de dejar el sufijo por defecto de la
        // librería, que puede variar entre versiones) para que coincida siempre con el
        // tópico parqueadero.movimientos.DLT declarado en KafkaTopicsConfig y documentado
        // en 03_Kafka/eventos.md.
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, exception) -> new TopicPartition(topicoMovimientos + ".DLT", record.partition()));

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.error(
                            "Evento irrecuperable en topic={} partition={} offset={}: {}. Se envía a {}.DLT",
                            record.topic(), record.partition(), record.offset(), exception.getMessage(), record.topic());
                    eventosInvalidos.increment();
                    recoverer.accept(record, exception);
                },
                new FixedBackOff(1000L, 3));

        errorHandler.addNotRetryableExceptions(EventoInvalidoException.class);
        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        return factory;
    }
}
