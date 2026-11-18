package com.fet.auditoria.service;

import com.fet.auditoria.event.MovimientoEvento;
import com.fet.auditoria.event.TipoEventoMovimiento;
import com.fet.auditoria.persistence.AuditoriaEventoEntity;
import com.fet.auditoria.persistence.AuditoriaEventoRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.dao.DataIntegrityViolationException;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditoriaEventoServiceTest {

    private AuditoriaEventoRepository repositorio;
    private SimpleMeterRegistry meterRegistry;
    private AuditoriaEventoService servicio;

    @BeforeEach
    void setUp() {
        repositorio = mock(AuditoriaEventoRepository.class);
        meterRegistry = new SimpleMeterRegistry();
        servicio = new AuditoriaEventoService(repositorio, meterRegistry);
    }

    private MovimientoEvento eventoIngreso(String eventId) {
        return new MovimientoEvento(
                eventId,
                TipoEventoMovimiento.VEHICULO_INGRESADO,
                OffsetDateTime.now(),
                100L,
                25L,
                "ABC123",
                8L,
                "E-08",
                null);
    }

    @Test
    void eventoNuevo_sePersiste() {
        when(repositorio.existsById("evt-1")).thenReturn(false);

        servicio.procesar(eventoIngreso("evt-1"));

        ArgumentCaptor<AuditoriaEventoEntity> captor = ArgumentCaptor.forClass(AuditoriaEventoEntity.class);
        verify(repositorio).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo("evt-1");
        assertThat(captor.getValue().getPlaca()).isEqualTo("ABC123");
        assertThat(meterRegistry.get("auditoria.eventos.procesados").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("auditoria.eventos.duplicados").counter().count()).isEqualTo(0.0);
    }

    @Test
    void eventoYaExistente_seIgnoraSinError() {
        when(repositorio.existsById("evt-dup")).thenReturn(true);

        servicio.procesar(eventoIngreso("evt-dup"));

        verify(repositorio, never()).save(any());
        assertThat(meterRegistry.get("auditoria.eventos.duplicados").counter().count()).isEqualTo(1.0);
    }

    @Test
    void carreraEntreInstancias_seTrataComoDuplicado() {
        when(repositorio.existsById("evt-race")).thenReturn(false);
        when(repositorio.save(any())).thenThrow(new DataIntegrityViolationException("PK duplicada"));

        servicio.procesar(eventoIngreso("evt-race"));

        assertThat(meterRegistry.get("auditoria.eventos.duplicados").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("auditoria.eventos.procesados").counter().count()).isEqualTo(0.0);
    }
}
