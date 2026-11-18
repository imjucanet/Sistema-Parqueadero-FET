package com.fet.parqueadero.domain.service;

import com.fet.parqueadero.application.port.out.EspacioRepositoryPort;
import com.fet.parqueadero.domain.exception.RecursoDuplicadoException;
import com.fet.parqueadero.domain.model.Espacio;
import com.fet.parqueadero.domain.model.EstadoEspacio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionEspaciosServiceTest {

    @Mock
    private EspacioRepositoryPort espacios;

    @InjectMocks
    private GestionEspaciosService service;

    @Test
    void registrarEspacio_guardaComoDisponible() {
        Espacio entrada = new Espacio(null, "e-21", null);
        Espacio guardado = new Espacio(UUID.randomUUID(), "E-21", EstadoEspacio.DISPONIBLE);
        when(espacios.findByNumero("E-21")).thenReturn(Optional.empty());
        when(espacios.save(any(Espacio.class))).thenReturn(guardado);

        Espacio resultado = service.registrarEspacio(entrada);

        ArgumentCaptor<Espacio> captor = ArgumentCaptor.forClass(Espacio.class);
        verify(espacios).save(captor.capture());
        assertThat(captor.getValue().id()).isNull();
        assertThat(captor.getValue().numero()).isEqualTo("E-21");
        assertThat(captor.getValue().estado()).isEqualTo(EstadoEspacio.DISPONIBLE);
        assertThat(resultado).isEqualTo(guardado);
    }

    @Test
    void registrarEspacio_conNumeroDuplicado_lanzaRecursoDuplicado() {
        Espacio existente = new Espacio(UUID.randomUUID(), "E-21", EstadoEspacio.DISPONIBLE);
        Espacio entrada = new Espacio(null, "E-21", null);
        when(espacios.findByNumero("E-21")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.registrarEspacio(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("E-21");
    }

    @Test
    void listarEspacios_delegaAlRepositorio() {
        Page<Espacio> pagina = new PageImpl<>(List.of(
                new Espacio(UUID.randomUUID(), "E-01", EstadoEspacio.DISPONIBLE),
                new Espacio(UUID.randomUUID(), "E-02", EstadoEspacio.OCUPADO)
        ));
        PageRequest pageable = PageRequest.of(0, 10);
        when(espacios.findAll(pageable)).thenReturn(pagina);

        Page<Espacio> resultado = service.listarEspacios(pageable);

        assertThat(resultado).isEqualTo(pagina);
        verify(espacios).findAll(pageable);
    }

    @Test
    void listarEspaciosDisponibles_delegaAlRepositorio() {
        Page<Espacio> pagina = new PageImpl<>(List.of(
                new Espacio(UUID.randomUUID(), "E-03", EstadoEspacio.DISPONIBLE)
        ));
        PageRequest pageable = PageRequest.of(1, 5);
        when(espacios.findAllDisponibles(pageable)).thenReturn(pagina);

        Page<Espacio> resultado = service.listarEspaciosDisponibles(pageable);

        assertThat(resultado).isEqualTo(pagina);
        verify(espacios).findAllDisponibles(pageable);
    }
}
