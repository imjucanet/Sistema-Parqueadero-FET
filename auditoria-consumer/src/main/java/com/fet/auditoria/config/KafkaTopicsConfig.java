package com.fet.auditoria.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara el tópico dead-letter al que {@code DeadLetterPublishingRecoverer}
 * envía los eventos que no se pudieron procesar (inválidos o con error
 * persistente tras los reintentos). Spring Kafka lo crea automáticamente al
 * arrancar si no existe.
 */
@Configuration
public class KafkaTopicsConfig {

    @Value("${auditoria.topico-movimientos}")
    private String topicoMovimientos;

    @Bean
    public NewTopic topicoMovimientosDeadLetter() {
        return TopicBuilder.name(topicoMovimientos + ".DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
