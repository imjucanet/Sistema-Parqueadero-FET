package com.fet.parqueadero.domain.service;

import com.fet.parqueadero.application.port.out.PropietarioRepositoryPort;
import com.fet.parqueadero.application.port.out.VehiculoRepositoryPort;
import com.fet.parqueadero.domain.exception.EntidadInvalidaException;
import com.fet.parqueadero.domain.exception.RecursoDuplicadoException;
import com.fet.parqueadero.domain.exception.RecursoNoEncontradoException;
import com.fet.parqueadero.domain.model.EstadoVehiculo;
import com.fet.parqueadero.domain.model.TipoVehiculo;
import com.fet.parqueadero.domain.model.Vehiculo;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionVehiculosServiceTest {

    @Mock
    private VehiculoRepositoryPort vehiculos;

    @Mock
    private PropietarioRepositoryPort propietarios;

    @InjectMocks
    private GestionVehiculosService service;

    @Test
    void registrarVehiculo_conPropietarioValido_guardaVehiculo() {
        UUID propietarioId = UUID.randomUUID();
        Vehiculo entrada = new Vehiculo(null, "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, propietarioId);
        Vehiculo guardado = new Vehiculo(UUID.randomUUID(), "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, propietarioId);
        when(propietarios.existsById(propietarioId)).thenReturn(true);
        when(vehiculos.findByPlaca("ABC123")).thenReturn(Optional.empty());
        when(vehiculos.save(any(Vehiculo.class))).thenReturn(guardado);

        Vehiculo resultado = service.registrarVehiculo(entrada);

        ArgumentCaptor<Vehiculo> captor = ArgumentCaptor.forClass(Vehiculo.class);
        verify(vehiculos).save(captor.capture());
        assertThat(captor.getValue().id()).isNull();
        assertThat(captor.getValue().estado()).isEqualTo(EstadoVehiculo.ACTIVO);
        assertThat(resultado).isEqualTo(guardado);
    }

    @Test
    void registrarVehiculo_conPropietarioInvalido_lanzaEntidadInvalidaException() {
        UUID propietarioId = UUID.randomUUID();
        Vehiculo entrada = new Vehiculo(null, "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, propietarioId);
        when(propietarios.existsById(propietarioId)).thenReturn(false);

        assertThatThrownBy(() -> service.registrarVehiculo(entrada))
                .isInstanceOf(EntidadInvalidaException.class)
                .hasMessageContaining("No existe el propietario con id %s.".formatted(propietarioId));
    }

    @Test
    void registrarVehiculo_conPlacaDuplicada_lanzaRecursoDuplicado() {
        UUID propietarioId = UUID.randomUUID();
        Vehiculo existente = new Vehiculo(UUID.randomUUID(), "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, propietarioId);
        Vehiculo entrada = new Vehiculo(null, "abc123", TipoVehiculo.MOTOCICLETA, "Yamaha", "2023", EstadoVehiculo.ACTIVO, propietarioId);
        when(propietarios.existsById(propietarioId)).thenReturn(true);
        when(vehiculos.findByPlaca("ABC123")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> service.registrarVehiculo(entrada))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("ABC123");
    }

    @Test
    void obtenerVehiculo_cuandoExiste_devuelveVehiculo() {
        UUID id = UUID.randomUUID();
        Vehiculo esperado = new Vehiculo(id, "XYZ789", TipoVehiculo.MOTOCICLETA, "Honda", "2022", EstadoVehiculo.ACTIVO, UUID.randomUUID());
        when(vehiculos.findById(id)).thenReturn(Optional.of(esperado));

        Vehiculo resultado = service.obtenerVehiculo(id);

        assertThat(resultado).isEqualTo(esperado);
    }

    @Test
    void actualizarVehiculo_conservaEstadoYGuardaCambios() {
        UUID id = UUID.randomUUID();
        UUID propietarioId = UUID.randomUUID();
        Vehiculo existente = new Vehiculo(id, "AAA111", TipoVehiculo.AUTOMOVIL, "Mazda", "2020", EstadoVehiculo.ACTIVO, propietarioId);
        Vehiculo actualizado = new Vehiculo(id, "BBB222", TipoVehiculo.AUTOMOVIL, "Renault", "2025", EstadoVehiculo.ACTIVO, propietarioId);
        when(propietarios.existsById(propietarioId)).thenReturn(true);
        when(vehiculos.findById(id)).thenReturn(Optional.of(existente));
        when(vehiculos.findByPlaca("BBB222")).thenReturn(Optional.empty());
        when(vehiculos.save(any(Vehiculo.class))).thenReturn(actualizado);

        Vehiculo resultado = service.actualizarVehiculo(id, actualizado);

        ArgumentCaptor<Vehiculo> captor = ArgumentCaptor.forClass(Vehiculo.class);
        verify(vehiculos).save(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(id);
        assertThat(captor.getValue().estado()).isEqualTo(EstadoVehiculo.ACTIVO);
        assertThat(resultado).isEqualTo(actualizado);
    }

    @Test
    void obtenerVehiculo_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        UUID id = UUID.randomUUID();
        when(vehiculos.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerVehiculo(id))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("No existe el vehículo con id %s.".formatted(id));
    }

    @Test
    void listarMarcas_devuelveCatalogoCompletoConModelosPorMarcaYTipo() {
        Map<TipoVehiculo, Map<String, List<String>>> catalogo = service.listarMarcas();

        assertThat(catalogo.get(TipoVehiculo.AUTOMOVIL)).containsKeys("Chevrolet", "Toyota");
        assertThat(catalogo.get(TipoVehiculo.AUTOMOVIL).get("Toyota")).contains("Corolla", "Hilux");
        assertThat(catalogo.get(TipoVehiculo.MOTOCICLETA)).containsKeys("Yamaha", "AKT");
        assertThat(catalogo.get(TipoVehiculo.MOTOCICLETA).get("Yamaha")).contains("FZ");
        assertThat(catalogo.get(TipoVehiculo.OTRO)).isEmpty();
    }

    @Test
    void listarVehiculos_delegaAlRepositorio() {
        Page<Vehiculo> pagina = new PageImpl<>(List.of(
                new Vehiculo(UUID.randomUUID(), "P1", TipoVehiculo.AUTOMOVIL, "Toyota", "2023", EstadoVehiculo.ACTIVO, UUID.randomUUID()),
                new Vehiculo(UUID.randomUUID(), "P2", TipoVehiculo.MOTOCICLETA, "Honda", "2024", EstadoVehiculo.ACTIVO, UUID.randomUUID())
        ));
        PageRequest pageable = PageRequest.of(0, 5);
        when(vehiculos.findAll(pageable)).thenReturn(pagina);

        Page<Vehiculo> resultado = service.listarVehiculos(pageable);

        assertThat(resultado).isEqualTo(pagina);
        verify(vehiculos).findAll(pageable);
    }
}
