package com.fet.auditoria.integration;

import com.fet.auditoria.persistence.AuditoriaEventoRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Prueba de extremo a extremo del consumidor contra un broker Kafka embebido:
 * publica eventos "reales" (mismo formato que produce el backend) y verifica
 * persistencia, idempotencia y enrutamiento al dead-letter.
 */
@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"parqueadero.movimientos", "parqueadero.movimientos.DLT"})
@DirtiesContext
class AuditoriaConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private AuditoriaEventoRepository repositorio;

    private KafkaTemplate<String, String> testTemplate;

    @Test
    void eventoValido_sePersisteYReenviarloNoLoDuplica() {
        String eventId = UUID.randomUUID().toString();
        String payload = payloadIngreso(eventId);

        testTemplate().send("parqueadero.movimientos", eventId, payload);

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(repositorio.existsById(eventId)).isTrue());

        testTemplate().send("parqueadero.movimientos", eventId, payload);

        await().pollDelay(Duration.ofSeconds(2))
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> assertThat(repositorio.count()).isEqualTo(1));
    }

    @Test
    void eventoMalformado_vaAlDeadLetterYNoBloqueaLosSiguientes() {
        try (KafkaConsumer<String, String> dltConsumer = crearConsumidorDlt()) {
            testTemplate().send("parqueadero.movimientos", "key-invalida", "{ json corrupto");

            ConsumerRecords<String, String> registros = pollHasta(dltConsumer, Duration.ofSeconds(15));
            assertThat(registros.count()).isGreaterThanOrEqualTo(1);
        }

        String eventId = UUID.randomUUID().toString();
        testTemplate().send("parqueadero.movimientos", eventId, payloadIngreso(eventId));

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertThat(repositorio.existsById(eventId)).isTrue());
    }

    private KafkaTemplate<String, String> testTemplate() {
        if (testTemplate == null) {
            Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
            ProducerFactory<String, String> productorFactory =
                    new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new StringSerializer());
            testTemplate = new KafkaTemplate<>(productorFactory);
        }
        return testTemplate;
    }

    private KafkaConsumer<String, String> crearConsumidorDlt() {
        Map<String, Object> props = KafkaTestUtils.consumerProps("dlt-test-group", "true", embeddedKafkaBroker);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
        consumer.subscribe(Collections.singletonList("parqueadero.movimientos.DLT"));
        return consumer;
    }

    private ConsumerRecords<String, String> pollHasta(KafkaConsumer<String, String> consumer, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            if (!records.isEmpty()) {
                return records;
            }
        }
        return ConsumerRecords.empty();
    }

    private String payloadIngreso(String eventId) {
        return """
                {"eventId":"%s","eventType":"VEHICULO_INGRESADO","timestamp":"2026-08-24T08:30:00-05:00",
                 "movimientoId":100,"vehiculoId":25,"placa":"ABC123","espacioId":8,"espacioNumero":"E-08",
                 "tiempoPermanenciaMinutos":null}
                """.formatted(eventId);
    }
}
