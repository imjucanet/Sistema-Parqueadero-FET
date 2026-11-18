package com.fet.parqueadero;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureHttpGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.WebSocketGraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Pruebas de integración del esquema GraphQL ({@code 02_GraphQL/schema.graphql})
 * contra los mismos casos de uso que expone la API REST.
 *
 * <p>Usa H2 en memoria (perfil {@code test}), igual que
 * {@link ParqueaderoApiIntegrationTest}. Se ejecuta sobre un puerto HTTP real
 * (necesario para la subscription vía WebSocket), por lo que cada prueba usa
 * datos propios en vez de depender de rollback transaccional.</p>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureHttpGraphQlTester
@ActiveProfiles("test")
class ParqueaderoGraphQLIntegrationTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @LocalServerPort
    private int puerto;

    // ------------------------------------------------------------------
    // Consultas anidadas y ciclo de vida de un movimiento
    // ------------------------------------------------------------------

    @Test
    void registrarPropietarioYVehiculo_consultaAnidadaDevuelveRelacion() {
        String propietarioId = crearPropietario("Ana", "Gómez", "DOC-ANA-001", "ESTUDIANTE");
        String vehiculoId = crearVehiculo("ABC123", "AUTOMOVIL", "Toyota", propietarioId);

        graphQlTester.document("""
                query($id: ID!) {
                  propietario(id: $id) {
                    numeroDocumento
                    nombreCompleto
                    vehiculos { id placa }
                  }
                }
                """)
                .variable("id", propietarioId)
                .execute()
                .path("propietario.numeroDocumento").entity(String.class).isEqualTo("DOC-ANA-001")
                .path("propietario.nombreCompleto").entity(String.class).isEqualTo("Ana Gómez")
                .path("propietario.vehiculos[0].id").entity(String.class).isEqualTo(vehiculoId)
                .path("propietario.vehiculos[0].placa").entity(String.class).isEqualTo("ABC123");
    }

    @Test
    void entradaYSalida_actualizanEspacioYPermanencia() {
        String espacioId = crearEspacio("S-01");
        String propietarioId = crearPropietario("Luis", "Rojas", "DOC-LUIS-001", "DOCENTE");
        String vehiculoId = crearVehiculo("XYZ789", "MOTOCICLETA", "Yamaha", propietarioId);

        String movimientoId = registrarEntrada(vehiculoId, espacioId);

        graphQlTester.document("""
                mutation($movimientoId: ID!) {
                  registrarSalida(movimientoId: $movimientoId) {
                    estado
                    tiempoPermanenciaMinutos
                    espacio { estado }
                  }
                }
                """)
                .variable("movimientoId", movimientoId)
                .execute()
                .path("registrarSalida.estado").entity(String.class).isEqualTo("FINALIZADO")
                .path("registrarSalida.espacio.estado").entity(String.class).isEqualTo("DISPONIBLE")
                .path("registrarSalida.tiempoPermanenciaMinutos").entity(Integer.class)
                .satisfies(minutos -> assertThat(minutos).isGreaterThanOrEqualTo(0));
    }

    @Test
    void movimiento_porId_devuelveElMovimientoConSusRelaciones() {
        String espacioId = crearEspacio("S-02");
        String propietarioId = crearPropietario("Eva", "Niño", "DOC-EVA-001", "ADMINISTRATIVO");
        String vehiculoId = crearVehiculo("MOV001", "AUTOMOVIL", "Mazda", propietarioId);
        String movimientoId = registrarEntrada(vehiculoId, espacioId);

        graphQlTester.document("""
                query($id: ID!) {
                  movimiento(id: $id) {
                    id
                    estado
                    vehiculo { placa }
                    espacio { numero }
                  }
                }
                """)
                .variable("id", movimientoId)
                .execute()
                .path("movimiento.id").entity(String.class).isEqualTo(movimientoId)
                .path("movimiento.estado").entity(String.class).isEqualTo("EN_PARQUEADERO")
                .path("movimiento.vehiculo.placa").entity(String.class).isEqualTo("MOV001")
                .path("movimiento.espacio.numero").entity(String.class).isEqualTo("S-02");
    }

    /** El scalar DateTime debe entregar un ISO-8601 que OffsetDateTime pueda releer. */
    @Test
    void fechaEntrada_seSerializaComoDateTimeIso8601() {
        String espacioId = crearEspacio("S-03");
        String propietarioId = crearPropietario("Iván", "Mora", "DOC-IVAN-001", "VISITANTE");
        String vehiculoId = crearVehiculo("DTM001", "AUTOMOVIL", "Kia", propietarioId);
        String movimientoId = registrarEntrada(vehiculoId, espacioId);

        String fechaEntrada = graphQlTester.document("""
                query($id: ID!) { movimiento(id: $id) { fechaEntrada fechaSalida } }
                """)
                .variable("id", movimientoId)
                .execute()
                .path("movimiento.fechaSalida").valueIsNull()
                .path("movimiento.fechaEntrada").entity(String.class).get();

        assertThat(OffsetDateTime.parse(fechaEntrada)).isNotNull();
    }

    // ------------------------------------------------------------------
    // Operaciones que antes solo existían en REST
    // ------------------------------------------------------------------

    @Test
    void registrarEspacio_creaElEspacioComoDisponible() {
        graphQlTester.document("""
                mutation { registrarEspacio(input: { numero: "S-10" }) { numero estado } }
                """)
                .execute()
                .path("registrarEspacio.numero").entity(String.class).isEqualTo("S-10")
                .path("registrarEspacio.estado").entity(String.class).isEqualTo("DISPONIBLE");
    }

    @Test
    void registrarEspacio_conNumeroDuplicado_devuelveCodigoDeNegocio() {
        crearEspacio("S-11");

        graphQlTester.document("""
                mutation { registrarEspacio(input: { numero: "S-11" }) { id } }
                """)
                .execute()
                .errors()
                .satisfy(errores -> assertThat(errores)
                        .singleElement()
                        .satisfies(error -> assertThat(error.getExtensions())
                                .containsEntry("codigo", "ESPACIO_YA_EXISTE")));
    }

    @Test
    void marcas_devuelveElCatalogoAgrupadoPorTipo() {
        graphQlTester.document("{ marcas { tipo marcas { marca modelos } } }")
                .execute()
                .path("marcas").entityList(Object.class)
                .satisfies(tipos -> assertThat(tipos).hasSizeGreaterThanOrEqualTo(2))
                .path("marcas[0].tipo").entity(String.class).isEqualTo("AUTOMOVIL")
                .path("marcas[0].marcas[0].marca").entity(String.class)
                .satisfies(marca -> assertThat(marca).isNotBlank())
                .path("marcas[0].marcas[0].modelos").entityList(String.class)
                .satisfies(modelos -> assertThat(modelos).isNotEmpty());
    }

    @Test
    void registrarVehiculosEnLote_informaExitoYErrorPorElemento() {
        String propietarioId = crearPropietario("Sara", "Luna", "DOC-SARA-001", "ESTUDIANTE");
        crearVehiculo("LOTE01", "AUTOMOVIL", "Ford", propietarioId);

        graphQlTester.document("""
                mutation($propietarioId: ID!, $inexistente: ID!) {
                  registrarVehiculosEnLote(inputs: [
                    { placa: "LOTE02", tipo: AUTOMOVIL, marca: "Honda", propietarioId: $propietarioId },
                    { placa: "LOTE01", tipo: AUTOMOVIL, marca: "Honda", propietarioId: $propietarioId },
                    { placa: "LOTE03", tipo: AUTOMOVIL, marca: "Honda", propietarioId: $inexistente }
                  ]) {
                    exito
                    vehiculo { placa }
                    error { codigo }
                  }
                }
                """)
                .variable("propietarioId", propietarioId)
                .variable("inexistente", UUID.randomUUID().toString())
                .execute()
                .path("registrarVehiculosEnLote[0].exito").entity(Boolean.class).isEqualTo(true)
                .path("registrarVehiculosEnLote[0].vehiculo.placa").entity(String.class).isEqualTo("LOTE02")
                .path("registrarVehiculosEnLote[1].exito").entity(Boolean.class).isEqualTo(false)
                .path("registrarVehiculosEnLote[1].error.codigo").entity(String.class).isEqualTo("VEHICULO_YA_EXISTE")
                .path("registrarVehiculosEnLote[2].exito").entity(Boolean.class).isEqualTo(false)
                .path("registrarVehiculosEnLote[2].error.codigo").entity(String.class)
                .isEqualTo("PROPIETARIO_NO_ENCONTRADO");
    }

    // ------------------------------------------------------------------
    // Validación de entradas
    // ------------------------------------------------------------------

    /**
     * Antes de validar los inputs, un documento más largo que VARCHAR(30) llegaba
     * hasta el INSERT y se reportaba como error interno.
     */
    @Test
    void registrarPropietario_conDocumentoDemasiadoLargo_devuelveValidacion() {
        graphQlTester.document("""
                mutation($documento: String!) {
                  registrarPropietario(input: {
                    primerNombre: "Zoe"
                    primerApellido: "Vega"
                    numeroDocumento: $documento
                    tipoUsuario: ESTUDIANTE
                  }) { id }
                }
                """)
                .variable("documento", "DOC-" + UUID.randomUUID())
                .execute()
                .errors()
                .satisfy(errores -> assertThat(errores)
                        .singleElement()
                        .satisfies(error -> {
                            assertThat(error.getExtensions()).containsEntry("codigo", "VALIDACION");
                            assertThat(error.getMessage()).contains("numeroDocumento");
                        }));
    }

    @Test
    void registrarVehiculo_conPlacaEnBlanco_devuelveValidacion() {
        String propietarioId = crearPropietario("Noa", "Pino", "DOC-NOA-001", "DOCENTE");

        graphQlTester.document("""
                mutation($propietarioId: ID!) {
                  registrarVehiculo(input: {
                    placa: "   "
                    tipo: AUTOMOVIL
                    marca: "Nissan"
                    propietarioId: $propietarioId
                  }) { id }
                }
                """)
                .variable("propietarioId", propietarioId)
                .execute()
                .errors()
                .satisfy(errores -> assertThat(errores)
                        .anySatisfy(error -> assertThat(error.getExtensions())
                                .containsEntry("codigo", "VALIDACION")));
    }

    // ------------------------------------------------------------------
    // Paginación
    // ------------------------------------------------------------------

    @Test
    void espacios_conPaginacion_limitaElResultado() {
        graphQlTester.document("{ espacios(pagina: 0, tamano: 2) { id } }")
                .execute()
                .path("espacios").entityList(Object.class).hasSize(2);

        graphQlTester.document("{ espacios { id } }")
                .execute()
                .path("espacios").entityList(Object.class)
                .satisfies(todos -> assertThat(todos).hasSizeGreaterThan(2));
    }

    // ------------------------------------------------------------------
    // Subscription
    // ------------------------------------------------------------------

    @Test
    void subscriptionMovimientoRegistrado_emiteLaEntradaTrasConfirmarse() {
        String espacioId = crearEspacio("S-20");
        String propietarioId = crearPropietario("Hugo", "Cano", "DOC-HUGO-001", "ESTUDIANTE");
        String vehiculoId = crearVehiculo("SUB001", "AUTOMOVIL", "Renault", propietarioId);

        WebSocketGraphQlTester wsTester = WebSocketGraphQlTester
                .builder("ws://localhost:%d/graphql".formatted(puerto), new ReactorNettyWebSocketClient())
                .build();

        Flux<String> placas = wsTester.document("""
                subscription { movimientoRegistrado { estado vehiculo { placa } } }
                """)
                .executeSubscription()
                .toFlux("movimientoRegistrado.vehiculo.placa", String.class);

        StepVerifier.create(placas)
                // El sink es directBestEffort: solo entrega a quien ya está escuchando,
                // así que la entrada se dispara después de establecer la subscription.
                .thenAwait(Duration.ofSeconds(1))
                .then(() -> registrarEntrada(vehiculoId, espacioId))
                .expectNext("SUB001")
                .thenCancel()
                .verify(Duration.ofSeconds(20));
    }

    // ------------------------------------------------------------------
    // Ayudantes
    // ------------------------------------------------------------------

    private String crearPropietario(String nombre, String apellido, String documento, String tipoUsuario) {
        return graphQlTester.document("""
                mutation($nombre: String!, $apellido: String!, $documento: String!, $tipo: TipoUsuario!) {
                  registrarPropietario(input: {
                    primerNombre: $nombre
                    primerApellido: $apellido
                    numeroDocumento: $documento
                    tipoUsuario: $tipo
                  }) { id }
                }
                """)
                .variable("nombre", nombre)
                .variable("apellido", apellido)
                .variable("documento", documento)
                .variable("tipo", tipoUsuario)
                .execute()
                .path("registrarPropietario.id").entity(String.class).get();
    }

    private String crearVehiculo(String placa, String tipo, String marca, String propietarioId) {
        return graphQlTester.document("""
                mutation($placa: String!, $tipo: TipoVehiculo!, $marca: String!, $propietarioId: ID!) {
                  registrarVehiculo(input: {
                    placa: $placa
                    tipo: $tipo
                    marca: $marca
                    propietarioId: $propietarioId
                  }) { id }
                }
                """)
                .variable("placa", placa)
                .variable("tipo", tipo)
                .variable("marca", marca)
                .variable("propietarioId", propietarioId)
                .execute()
                .path("registrarVehiculo.id").entity(String.class).get();
    }

    private String crearEspacio(String numero) {
        return graphQlTester.document("""
                mutation($numero: String!) { registrarEspacio(input: { numero: $numero }) { id } }
                """)
                .variable("numero", numero)
                .execute()
                .path("registrarEspacio.id").entity(String.class).get();
    }

    private String registrarEntrada(String vehiculoId, String espacioId) {
        return graphQlTester.document("""
                mutation($vehiculoId: ID!, $espacioId: ID) {
                  registrarEntrada(vehiculoId: $vehiculoId, espacioId: $espacioId) { id }
                }
                """)
                .variable("vehiculoId", vehiculoId)
                .variable("espacioId", espacioId)
                .execute()
                .path("registrarEntrada.id").entity(String.class).get();
    }
}
