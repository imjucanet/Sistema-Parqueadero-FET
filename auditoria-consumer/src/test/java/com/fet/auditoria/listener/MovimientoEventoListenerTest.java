package com.fet.auditoria.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fet.auditoria.exception.EventoInvalidoException;
import com.fet.auditoria.service.AuditoriaEventoService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MovimientoEventoListenerTest {

    private MovimientoEventoListener listener;
    private AuditoriaEventoService servicio;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        servicio = mock(AuditoriaEventoService.class);
        listener = new MovimientoEventoListener(objectMapper, servicio);
    }

    private ConsumerRecord<String, String> record(String payload) {
        return new ConsumerRecord<>("parqueadero.movimientos", 0, 0L, "key", payload);
    }

    @Test
    void eventoValido_seDelegaAlServicio() {
        String payload = """
                {"eventId":"550e8400-e29b-41d4-a716-446655440000","eventType":"VEHICULO_INGRESADO",
                 "timestamp":"2026-08-24T08:30:00-05:00","movimientoId":100,"vehiculoId":25,
                 "placa":"ABC123","espacioId":8,"espacioNumero":"E-08","tiempoPermanenciaMinutos":null}
                """;

        listener.escuchar(record(payload));

        verify(servicio).procesar(argThatEventId("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    void jsonMalformado_lanzaEventoInvalido() {
        assertThatThrownBy(() -> listener.escuchar(record("{ esto no es json")))
                .isInstanceOf(EventoInvalidoException.class);
    }

    @Test
    void eventTypeDesconocido_lanzaEventoInvalido() {
        String payload = """
                {"eventId":"x","eventType":"ALGO_RARO","timestamp":"2026-08-24T08:30:00-05:00",
                 "movimientoId":1,"vehiculoId":1,"placa":"ABC123","espacioId":1,"espacioNumero":"E-01"}
                """;

        assertThatThrownBy(() -> listener.escuchar(record(payload)))
                .isInstanceOf(EventoInvalidoException.class);
    }

    @Test
    void campoObligatorioAusente_lanzaEventoInvalido() {
        String payload = """
                {"eventId":"y","eventType":"VEHICULO_INGRESADO","timestamp":"2026-08-24T08:30:00-05:00",
                 "vehiculoId":1,"placa":"ABC123","espacioId":1,"espacioNumero":"E-01"}
                """;

        assertThatThrownBy(() -> listener.escuchar(record(payload)))
                .isInstanceOf(EventoInvalidoException.class);
    }

    private com.fet.auditoria.event.MovimientoEvento argThatEventId(String eventId) {
        return org.mockito.ArgumentMatchers.argThat(evento -> evento.eventId().equals(eventId));
    }
}
