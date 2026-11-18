package com.fet.parqueadero.domain.service;

import com.fet.parqueadero.application.port.out.EspacioRepositoryPort;
import com.fet.parqueadero.application.port.out.EventPublisherPort;
import com.fet.parqueadero.application.port.out.MovimientoEvento;
import com.fet.parqueadero.application.port.out.MovimientoRepositoryPort;
import com.fet.parqueadero.application.port.out.TipoEventoMovimiento;
import com.fet.parqueadero.application.port.out.VehiculoRepositoryPort;
import com.fet.parqueadero.domain.exception.EntidadInvalidaException;
import com.fet.parqueadero.domain.exception.EspacioNoDisponibleException;
import com.fet.parqueadero.domain.exception.MovimientoNoActivoException;
import com.fet.parqueadero.domain.exception.RecursoNoEncontradoException;
import com.fet.parqueadero.domain.exception.VehiculoYaEnParqueaderoException;
import com.fet.parqueadero.domain.model.Espacio;
import com.fet.parqueadero.domain.model.EstadoEspacio;
import com.fet.parqueadero.domain.model.EstadoMovimiento;
import com.fet.parqueadero.domain.model.EstadoVehiculo;
import com.fet.parqueadero.domain.model.Movimiento;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GestionMovimientosServiceTest {

    @Mock
    private VehiculoRepositoryPort vehiculos;

    @Mock
    private EspacioRepositoryPort espacios;

    @Mock
    private MovimientoRepositoryPort movimientos;

    @Mock
    private EventPublisherPort eventPublisher;

    @InjectMocks
    private GestionMovimientosService service;

    @Test
    void registrarEntrada_conVehiculoYEspacioValidos_guardaMovimientoYPublicaEvento() {
        UUID vehiculoId = UUID.randomUUID();
        UUID espacioId = UUID.randomUUID();
        UUID propietarioId = UUID.randomUUID();
        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, propietarioId);
        Espacio espacio = new Espacio(espacioId, "E-07", EstadoEspacio.DISPONIBLE);
        OffsetDateTime ahora = OffsetDateTime.now();
        Movimiento creado = new Movimiento(UUID.randomUUID(), vehiculoId, espacioId, ahora, null, null, EstadoMovimiento.EN_PARQUEADERO);

        when(vehiculos.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(movimientos.findActivoPorVehiculo(vehiculoId)).thenReturn(Optional.empty());
        when(espacios.findFirstDisponible()).thenReturn(Optional.of(espacio));
        when(movimientos.save(any(Movimiento.class))).thenReturn(creado);
        when(espacios.save(any(Espacio.class))).thenReturn(new Espacio(espacioId, "E-07", EstadoEspacio.OCUPADO));

        Movimiento resultado = service.registrarEntrada(vehiculoId, null);

        assertThat(resultado).isEqualTo(creado);
        verify(movimientos).save(any(Movimiento.class));
        verify(espacios).save(any(Espacio.class));
        ArgumentCaptor<MovimientoEvento> captor = ArgumentCaptor.forClass(MovimientoEvento.class);
        verify(eventPublisher).publicarMovimiento(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(TipoEventoMovimiento.VEHICULO_INGRESADO);
        assertThat(captor.getValue().vehiculoId()).isEqualTo(vehiculoId);
    }

    @Test
    void registrarEntrada_conVehiculoInexistente_lanzaEntidadInvalidaException() {
        UUID vehiculoId = UUID.randomUUID();
        when(vehiculos.findById(vehiculoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarEntrada(vehiculoId, null))
                .isInstanceOf(EntidadInvalidaException.class)
                .hasMessageContaining("No existe el vehículo con id %s.".formatted(vehiculoId));
    }

    @Test
    void registrarEntrada_conVehiculoYaEnParqueadero_lanzaVehiculoYaEnParqueaderoException() {
        UUID vehiculoId = UUID.randomUUID();
        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, UUID.randomUUID());
        Movimiento activo = new Movimiento(UUID.randomUUID(), vehiculoId, UUID.randomUUID(), OffsetDateTime.now(), null, null, EstadoMovimiento.EN_PARQUEADERO);
        when(vehiculos.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(movimientos.findActivoPorVehiculo(vehiculoId)).thenReturn(Optional.of(activo));

        assertThatThrownBy(() -> service.registrarEntrada(vehiculoId, null))
                .isInstanceOf(VehiculoYaEnParqueaderoException.class)
                .hasMessageContaining("ya se encuentra dentro del parqueadero");
    }

    @Test
    void registrarEntrada_sinEspaciosDisponibles_lanzaEspacioNoDisponibleException() {
        UUID vehiculoId = UUID.randomUUID();
        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, UUID.randomUUID());
        when(vehiculos.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(movimientos.findActivoPorVehiculo(vehiculoId)).thenReturn(Optional.empty());
        when(espacios.findFirstDisponible()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarEntrada(vehiculoId, null))
                .isInstanceOf(EspacioNoDisponibleException.class)
                .hasMessageContaining("No hay espacios disponibles");
    }

    @Test
    void registrarEntrada_conEspacioIdValido_usaEseEspacioEnVezDeAutoAsignar() {
        UUID vehiculoId = UUID.randomUUID();
        UUID espacioElegidoId = UUID.randomUUID();
        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, UUID.randomUUID());
        Espacio espacioElegido = new Espacio(espacioElegidoId, "E-15", EstadoEspacio.DISPONIBLE);
        OffsetDateTime ahora = OffsetDateTime.now();
        Movimiento creado = new Movimiento(UUID.randomUUID(), vehiculoId, espacioElegidoId, ahora, null, null, EstadoMovimiento.EN_PARQUEADERO);

        when(vehiculos.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(movimientos.findActivoPorVehiculo(vehiculoId)).thenReturn(Optional.empty());
        when(espacios.findById(espacioElegidoId)).thenReturn(Optional.of(espacioElegido));
        when(movimientos.save(any(Movimiento.class))).thenReturn(creado);
        when(espacios.save(any(Espacio.class))).thenReturn(new Espacio(espacioElegidoId, "E-15", EstadoEspacio.OCUPADO));

        Movimiento resultado = service.registrarEntrada(vehiculoId, espacioElegidoId);

        assertThat(resultado.espacioId()).isEqualTo(espacioElegidoId);
        verify(espacios, never()).findFirstDisponible();
    }

    @Test
    void registrarEntrada_conEspacioIdNoDisponible_lanzaEspacioNoDisponibleException() {
        UUID vehiculoId = UUID.randomUUID();
        UUID espacioElegidoId = UUID.randomUUID();
        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, UUID.randomUUID());
        Espacio espacioOcupado = new Espacio(espacioElegidoId, "E-15", EstadoEspacio.OCUPADO);
        when(vehiculos.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(movimientos.findActivoPorVehiculo(vehiculoId)).thenReturn(Optional.empty());
        when(espacios.findById(espacioElegidoId)).thenReturn(Optional.of(espacioOcupado));

        assertThatThrownBy(() -> service.registrarEntrada(vehiculoId, espacioElegidoId))
                .isInstanceOf(EspacioNoDisponibleException.class)
                .hasMessageContaining("E-15");
    }

    @Test
    void registrarEntrada_conEspacioIdInexistente_lanzaEntidadInvalidaException() {
        UUID vehiculoId = UUID.randomUUID();
        UUID espacioInexistenteId = UUID.randomUUID();
        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, UUID.randomUUID());
        when(vehiculos.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));
        when(movimientos.findActivoPorVehiculo(vehiculoId)).thenReturn(Optional.empty());
        when(espacios.findById(espacioInexistenteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarEntrada(vehiculoId, espacioInexistenteId))
                .isInstanceOf(EntidadInvalidaException.class)
                .hasMessageContaining("No existe el espacio con id %s.".formatted(espacioInexistenteId));
    }

    @Test
    void registrarSalida_conMovimientoActivo_finalizaYPublicaEvento() {
        UUID movimientoId = UUID.randomUUID();
        UUID vehiculoId = UUID.randomUUID();
        UUID espacioId = UUID.randomUUID();
        OffsetDateTime entrada = OffsetDateTime.now().minusHours(2);
        Movimiento activo = new Movimiento(movimientoId, vehiculoId, espacioId, entrada, null, null, EstadoMovimiento.EN_PARQUEADERO);
        Movimiento finalizado = new Movimiento(movimientoId, vehiculoId, espacioId, entrada, OffsetDateTime.now(), 120L, EstadoMovimiento.FINALIZADO);
        Espacio espacio = new Espacio(espacioId, "E-07", EstadoEspacio.OCUPADO);
        Vehiculo vehiculo = new Vehiculo(vehiculoId, "ABC123", TipoVehiculo.AUTOMOVIL, "Toyota", "2024", EstadoVehiculo.ACTIVO, UUID.randomUUID());

        when(movimientos.findById(movimientoId)).thenReturn(Optional.of(activo));
        when(movimientos.save(any(Movimiento.class))).thenReturn(finalizado);
        when(espacios.findById(espacioId)).thenReturn(Optional.of(espacio));
        when(espacios.save(any(Espacio.class))).thenReturn(new Espacio(espacioId, "E-07", EstadoEspacio.DISPONIBLE));
        when(vehiculos.findById(vehiculoId)).thenReturn(Optional.of(vehiculo));

        Movimiento resultado = service.registrarSalida(movimientoId);

        assertThat(resultado.estado()).isEqualTo(EstadoMovimiento.FINALIZADO);
        verify(eventPublisher).publicarMovimiento(any(MovimientoEvento.class));
        assertThat(resultado.tiempoPermanenciaMinutos()).isNotNull();
    }

    @Test
    void registrarSalida_conMovimientoNoActivo_lanzaMovimientoNoActivoException() {
        UUID movimientoId = UUID.randomUUID();
        Movimiento finalizado = new Movimiento(movimientoId, UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now(), OffsetDateTime.now(), 50L, EstadoMovimiento.FINALIZADO);
        when(movimientos.findById(movimientoId)).thenReturn(Optional.of(finalizado));

        assertThatThrownBy(() -> service.registrarSalida(movimientoId))
                .isInstanceOf(MovimientoNoActivoException.class)
                .hasMessageContaining("no se encuentra activo");
    }

    @Test
    void registrarSalida_conMovimientoInexistente_lanzaRecursoNoEncontrado() {
        UUID movimientoId = UUID.randomUUID();
        when(movimientos.findById(movimientoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarSalida(movimientoId))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("No existe el movimiento con id %s.".formatted(movimientoId));
    }

    @Test
    void listarMovimientosActivos_delegaAlRepositorio() {
        Page<Movimiento> pagina = new PageImpl<>(List.of(
                new Movimiento(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now(), null, null, EstadoMovimiento.EN_PARQUEADERO)
        ));
        PageRequest pageable = PageRequest.of(0, 10);
        when(movimientos.findAllActivos(pageable)).thenReturn(pagina);

        Page<Movimiento> resultado = service.listarMovimientosActivos(pageable);

        assertThat(resultado).isEqualTo(pagina);
        verify(movimientos).findAllActivos(pageable);
    }

    @Test
    void listarHistorialMovimientos_delegaAlRepositorio() {
        Page<Movimiento> pagina = new PageImpl<>(List.of(
                new Movimiento(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now().minusHours(1), OffsetDateTime.now(), 60L, EstadoMovimiento.FINALIZADO)
        ));
        PageRequest pageable = PageRequest.of(0, 10);
        when(movimientos.findAllHistorial(pageable)).thenReturn(pagina);

        Page<Movimiento> resultado = service.listarHistorialMovimientos(pageable);

        assertThat(resultado).isEqualTo(pagina);
        verify(movimientos).findAllHistorial(pageable);
    }
}
