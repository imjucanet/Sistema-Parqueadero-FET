package com.fet.parqueadero.domain.service;

import com.fet.parqueadero.application.port.out.PropietarioRepositoryPort;
import com.fet.parqueadero.domain.exception.RecursoDuplicadoException;
import com.fet.parqueadero.domain.exception.RecursoNoEncontradoException;
import com.fet.parqueadero.domain.model.Propietario;
import com.fet.parqueadero.domain.model.TipoUsuario;
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
class GestionPropietariosServiceTest {

    @Mock
    private PropietarioRepositoryPort propietarios;

    @InjectMocks
    private GestionPropietariosService service;

    @Test
    void registrarPropietario_guardaNuevoPropietario() {
        Propietario entrada = new Propietario(null, "Ana", "Gómez", null, "DOC-1", TipoUsuario.ESTUDIANTE, "3001234567", "ana@fet.edu.co");
        Propietario guardado = new Propietario(UUID.randomUUID(), "Ana", "Gómez", "Ana Gómez", "DOC-1", TipoUsuario.ESTUDIANTE, "3001234567", "ana@fet.edu.co");
        when(propietarios.findByNumeroDocumento("DOC-1")).thenReturn(Optional.empty());
        when(propietarios.save(any(Propietario.class))).thenReturn(guardado);

        Propietario resultado = service.registrarPropietario(entrada);

        ArgumentCaptor<Propietario> captor = ArgumentCaptor.forClass(Propietario.class);
        verify(propietarios).save(captor.capture());
        assertThat(captor.getValue().id()).isNull();
        assertThat(captor.getValue().nombreCompleto()).isEqualTo("Ana Gómez");
        assertThat(resultado).isEqualTo(guardado);
    }

    @Test
    void registrarPropietario_conDocumentoDuplicado_lanzaRecursoDuplicado() {
        Propietario existente = new Propietario(UUID.randomUUID(), "Ana", "Gómez", "Ana Gómez", "DOC-1", TipoUsuario.ESTUDIANTE, null, null);
        Propietario entrada = new Propietario(null, "Otra", "Persona", null, "DOC-1", TipoUsuario.DOCENTE, null, null);
        when(propietarios.findByNumeroDocumento("DOC-1")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.registrarPropietario(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("DOC-1");
    }

    @Test
    void obtenerPropietario_cuandoExiste_devuelvePropietario() {
        UUID id = UUID.randomUUID();
        Propietario esperado = new Propietario(id, "Luis", "Rojas", "Luis Rojas", "DOC-2", TipoUsuario.DOCENTE, "3110000000", "luis@fet.edu.co");
        when(propietarios.findById(id)).thenReturn(Optional.of(esperado));

        Propietario resultado = service.obtenerPropietario(id);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void obtenerPropietario_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(propietarios.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPropietario(id))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("No existe el propietario con id %s.".formatted(id));
    }

    @Test
    void actualizarPropietario_actualizaDatos() {
        UUID id = UUID.randomUUID();
        Propietario existente = new Propietario(id, "Viejo", "Apellido", "Viejo Apellido", "DOC-3", TipoUsuario.ESTUDIANTE, "3000000000", "viejo@fet.edu.co");
        Propietario nuevo = new Propietario(id, "Nuevo", "Apellido", null, "DOC-3", TipoUsuario.DOCENTE, "3011111111", "nuevo@fet.edu.co");
        Propietario guardado = new Propietario(id, "Nuevo", "Apellido", "Nuevo Apellido", "DOC-3", TipoUsuario.DOCENTE, "3011111111", "nuevo@fet.edu.co");
        when(propietarios.findById(id)).thenReturn(Optional.of(existente));
        when(propietarios.findByNumeroDocumento("DOC-3")).thenReturn(Optional.of(existente));
        when(propietarios.save(any(Propietario.class))).thenReturn(guardado);

        Propietario resultado = service.actualizarPropietario(id, nuevo);

        ArgumentCaptor<Propietario> captor = ArgumentCaptor.forClass(Propietario.class);
        verify(propietarios).save(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(id);
        assertThat(captor.getValue().nombreCompleto()).isEqualTo("Nuevo Apellido");
        assertThat(resultado).isEqualTo(guardado);
    }

    @Test
    void listarPropietarios_delegaAlRepositorio() {
        Page<Propietario> pagina = new PageImpl<>(List.of(
                new Propietario(UUID.randomUUID(), "Ana", "Pérez", "Ana Pérez", "DOC-4", TipoUsuario.ESTUDIANTE, null, null),
                new Propietario(UUID.randomUUID(), "Luis", "Gómez", "Luis Gómez", "DOC-5", TipoUsuario.DOCENTE, null, null)
        ));
        PageRequest pageable = PageRequest.of(0, 10);
        when(propietarios.findAll(pageable)).thenReturn(pagina);

        Page<Propietario> resultado = service.listarPropietarios(pageable);

        assertThat(resultado).isEqualTo(pagina);
        verify(propietarios).findAll(pageable);
    }
}
