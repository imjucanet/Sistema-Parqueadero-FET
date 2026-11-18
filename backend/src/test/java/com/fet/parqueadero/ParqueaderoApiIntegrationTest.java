package com.fet.parqueadero;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de la API REST contra el contrato OpenAPI.
 *
 * <p>Usa H2 en memoria (perfil {@code test}); los eventos Kafka se intentan
 * publicar sin broker y sus fallos solo se registran en el log.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ParqueaderoApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registrarPropietarioValido_devuelve201() throws Exception {
        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"primerNombre":"Juan","primerApellido":"Pérez","numeroDocumento":"DOC-JUAN-001","tipoUsuario":"ESTUDIANTE","telefono":"3001234567","correo":"juan@fet.edu.co"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.primerNombre").value("Juan"))
                .andExpect(jsonPath("$.primerApellido").value("Pérez"))
                .andExpect(jsonPath("$.nombreCompleto").value("Juan Pérez"))
                .andExpect(jsonPath("$.numeroDocumento").value("DOC-JUAN-001"))
                .andExpect(jsonPath("$.tipoUsuario").value("ESTUDIANTE"))
                .andExpect(jsonPath("$.correo").value("juan@fet.edu.co"));
    }

    @Test
    void registrarPropietarioInvalido_devuelve400Validacion() throws Exception {
        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoUsuario":"DESCONOCIDO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").isString())
                .andExpect(jsonPath("$.mensaje").isString());
    }

    @Test
    void registrarPropietarioDuplicado_devuelve409() throws Exception {
        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"primerNombre":"Ana","primerApellido":"Ramírez","numeroDocumento":"DOC-DUP-001","tipoUsuario":"ESTUDIANTE"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"primerNombre":"Ana","primerApellido":"Otra","numeroDocumento":"DOC-DUP-001","tipoUsuario":"DOCENTE"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PROPIETARIO_YA_EXISTE"));
    }

    @Test
    void obtenerPropietarioInexistente_devuelve404() throws Exception {
        mockMvc.perform(get("/api/propietarios/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void registrarVehiculoConPropietarioInexistente_devuelve400() throws Exception {
        UUID propietarioId = crearPropietario();

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehiculoJson("ABC123", "AUTOMOVIL", UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("PROPIETARIO_NO_ENCONTRADO"));

        assertThat(propietarioId).isNotNull();
    }

    @Test
    void registrarVehiculoValido_devuelve201Activo() throws Exception {
        UUID propietarioId = crearPropietario();

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehiculoJson("ABC123", "AUTOMOVIL", propietarioId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.placa").value("ABC123"))
                .andExpect(jsonPath("$.tipo").value("AUTOMOVIL"))
                .andExpect(jsonPath("$.propietarioId").value(propietarioId.toString()))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void registrarVehiculoDuplicado_devuelve409() throws Exception {
        UUID propietarioId = crearPropietario();

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehiculoJson("DUP123", "AUTOMOVIL", propietarioId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehiculoJson("dup123", "MOTOCICLETA", propietarioId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VEHICULO_YA_EXISTE"));
    }

    @Test
    void registrarVehiculosEnLote_mezclaExitosYErrores_devuelve207() throws Exception {
        UUID propietarioId = crearPropietario();

        mockMvc.perform(post("/api/vehiculos/lote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculos":[
                                    {"placa":"LOT001","tipo":"AUTOMOVIL","marca":"Mazda","propietarioId":"%s"},
                                    {"placa":"LOT002","tipo":"MOTOCICLETA","marca":"Yamaha","propietarioId":"%s"}
                                ]}
                                """.formatted(propietarioId, UUID.randomUUID())))
                .andExpect(status().isMultiStatus())
                .andExpect(jsonPath("$.resultados.length()").value(2))
                .andExpect(jsonPath("$.resultados[0].exito").value(true))
                .andExpect(jsonPath("$.resultados[0].vehiculo.placa").value("LOT001"))
                .andExpect(jsonPath("$.resultados[1].exito").value(false))
                .andExpect(jsonPath("$.resultados[1].error.codigo").value("PROPIETARIO_NO_ENCONTRADO"));
    }

    @Test
    void listarMarcas_devuelveCatalogoCompletoConModelosPorMarcaYTipo() throws Exception {
        mockMvc.perform(get("/api/vehiculos/marcas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.AUTOMOVIL.Toyota", Matchers.hasItem("Corolla")))
                .andExpect(jsonPath("$.MOTOCICLETA.Yamaha", Matchers.hasItem("FZ")))
                .andExpect(jsonPath("$.OTRO").isEmpty());
    }

    @Test
    void listarEspacios_seedInicial_disponiblesAlInicio() throws Exception {
        mockMvc.perform(get("/api/espacios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.content[0].numero").value("E-01"))
                .andExpect(jsonPath("$.content[0].estado").value("DISPONIBLE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    void registrarEspacio_devuelve201Disponible() throws Exception {
        mockMvc.perform(post("/api/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":"E-99"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.numero").value("E-99"))
                .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    void registrarEspacioConNumeroVacio_devuelve400() throws Exception {
        mockMvc.perform(post("/api/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").isString());
    }

    @Test
    void registrarEspacioConNumeroSoloEspacios_devuelve400() throws Exception {
        mockMvc.perform(post("/api/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":"   "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VALIDACION"));
    }

    @Test
    void registrarEspacioDuplicado_devuelve409() throws Exception {
        mockMvc.perform(post("/api/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":"E-50"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/espacios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numero":"e-50"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESPACIO_YA_EXISTE"));
    }

    @Test
    void entrada_devuelve201_ocupaEspacio_y_primeraSalida_devuelve200() throws Exception {
        UUID vehiculoId = crearVehiculo();

        MvcResult entrada = mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s"}
                                """.formatted(vehiculoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehiculoId").value(vehiculoId.toString()))
                .andExpect(jsonPath("$.espacioId").isString())
                .andExpect(jsonPath("$.fechaEntrada").isNotEmpty())
                .andExpect(jsonPath("$.estado").value("EN_PARQUEADERO"))
                .andReturn();

        JsonNode cuerpoEntrada = objectMapper.readTree(entrada.getResponse().getContentAsString());
        String espacioId = cuerpoEntrada.get("espacioId").asText();
        String movimientoId = cuerpoEntrada.get("id").asText();

        // El espacio ocupado ya no aparece en disponibles.
        mockMvc.perform(get("/api/espacios/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(espacioId)).doesNotExist());

        // El movimiento aparece en los activos.
        mockMvc.perform(get("/api/movimientos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(movimientoId)).exists());

        // Salida: finaliza, calcula permanencia y libera el espacio.
        mockMvc.perform(post("/api/movimientos/{id}/salida", movimientoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FINALIZADO"))
                .andExpect(jsonPath("$.fechaSalida").isNotEmpty())
                .andExpect(jsonPath("$.tiempoPermanenciaMinutos").isNumber());

        mockMvc.perform(get("/api/espacios/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(espacioId)).exists());
    }

    @Test
    void entrada_conEspacioIdExplicito_ocupaEseEspacioEnVezDeAutoAsignar() throws Exception {
        UUID vehiculoId = crearVehiculo();
        UUID espacioElegidoId = obtenerUltimoEspacioDisponibleId();

        mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s","espacioId":"%s"}
                                """.formatted(vehiculoId, espacioElegidoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.espacioId").value(espacioElegidoId.toString()));
    }

    @Test
    void entrada_conEspacioIdYaOcupado_devuelve409() throws Exception {
        UUID vehiculoId1 = crearVehiculo();
        UUID vehiculoId2 = crearVehiculo();
        UUID espacioElegidoId = obtenerUltimoEspacioDisponibleId();

        mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s","espacioId":"%s"}
                                """.formatted(vehiculoId1, espacioElegidoId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s","espacioId":"%s"}
                                """.formatted(vehiculoId2, espacioElegidoId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ESPACIO_NO_DISPONIBLE"));
    }

    @Test
    void entrada_conEspacioIdInexistente_devuelve400() throws Exception {
        UUID vehiculoId = crearVehiculo();

        mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s","espacioId":"%s"}
                                """.formatted(vehiculoId, UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ESPACIO_NO_ENCONTRADO"));
    }

    @Test
    void entradaVehiculoInexistente_devuelve400() throws Exception {
        mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("VEHICULO_NO_ENCONTRADO"));
    }

    @Test
    void dobleEntrada_devuelve409() throws Exception {
        UUID vehiculoId = crearVehiculo();

        mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s"}
                                """.formatted(vehiculoId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s"}
                                """.formatted(vehiculoId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("VEHICULO_YA_EN_PARQUEADERO"));
    }

    @Test
    void salidaDeMovimientoNoActivo_devuelve409() throws Exception {
        UUID vehiculoId = crearVehiculo();

        MvcResult entrada = mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s"}
                                """.formatted(vehiculoId)))
                .andExpect(status().isCreated())
                .andReturn();

        String movimientoId = objectMapper.readTree(entrada.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/movimientos/{id}/salida", movimientoId))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/movimientos/{id}/salida", movimientoId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("MOVIMIENTO_NO_ACTIVO"));
    }

    @Test
    void salidaDeMovimientoInexistente_devuelve404() throws Exception {
        mockMvc.perform(post("/api/movimientos/{id}/salida", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void actualizarPropietario_devuelve200() throws Exception {
        MvcResult creado = mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"primerNombre":"Ana","primerApellido":"Pérez","numeroDocumento":"DOC-UPD-001","tipoUsuario":"ESTUDIANTE"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode nodo = objectMapper.readTree(creado.getResponse().getContentAsString());
        String propietarioId = nodo.get("id").asText();
        String numeroDocumento = nodo.get("numeroDocumento").asText();

        mockMvc.perform(put("/api/propietarios/{id}", propietarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"primerNombre":"María","primerApellido":"Gómez","numeroDocumento":"%s","tipoUsuario":"DOCENTE","correo":"maria@fet.edu.co"}
                                """.formatted(numeroDocumento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(propietarioId))
                .andExpect(jsonPath("$.nombreCompleto").value("María Gómez"))
                .andExpect(jsonPath("$.tipoUsuario").value("DOCENTE"));
    }

    @Test
    void historial_de_movimientos_incluye_el_movimiento() throws Exception {
        UUID vehiculoId = crearVehiculo();

        MvcResult entrada = mockMvc.perform(post("/api/movimientos/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"vehiculoId":"%s"}
                                """.formatted(vehiculoId)))
                .andExpect(status().isCreated())
                .andReturn();

        String movimientoId = objectMapper.readTree(entrada.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(post("/api/movimientos/{id}/salida", movimientoId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/movimientos/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(movimientoId)).exists());
    }

    @Test
    void listarVehiculos_conPaginacion_devuelveMetadataDePaginaCorrecta() throws Exception {
        UUID propietarioId = crearPropietario();
        for (int i = 0; i < 12; i++) {
            mockMvc.perform(post("/api/vehiculos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(vehiculoJson("PAG-%02d".formatted(i + 1), "AUTOMOVIL", propietarioId)))
                    .andExpect(status().isCreated());
        }

        // Página 0 de 3 (12 elementos, tamaño 5): hay siguiente, no hay anterior.
        mockMvc.perform(get("/api/vehiculos?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(12))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        // Página 1: ya hay anterior y siguiente.
        mockMvc.perform(get("/api/vehiculos?page=1&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(true));

        // Última página (2 de 12 elementos restantes): no hay siguiente.
        mockMvc.perform(get("/api/vehiculos?page=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    @Test
    void paginacionConParametrosInvalidos_devuelve400() throws Exception {
        mockMvc.perform(get("/api/vehiculos?page=-1&size=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").isNotEmpty());
    }

    private UUID obtenerUltimoEspacioDisponibleId() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/espacios/disponibles?size=100"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode content = objectMapper.readTree(resultado.getResponse().getContentAsString()).get("content");
        return UUID.fromString(content.get(content.size() - 1).get("id").asText());
    }

    private UUID crearPropietario() throws Exception {
        // NUMERO_DOCUMENTO es VARCHAR(30): un UUID completo ("DOC-" + 36) lo desborda.
        String numeroDocumento = "DOC-" + UUID.randomUUID().toString().substring(0, 8);
        MvcResult resultado = mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"primerNombre":"Ana","primerApellido":"Pérez","numeroDocumento":"%s","tipoUsuario":"ESTUDIANTE"}
                                """.formatted(numeroDocumento)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText());
    }

    private UUID crearVehiculo() throws Exception {
        UUID propietarioId = crearPropietario();
        // Placa única: hay pruebas que llaman a este helper dos veces y la placa
        // es un identificador de negocio único (repetirla devolvería 409).
        String placa = "P" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        MvcResult resultado = mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehiculoJson(placa, "MOTOCICLETA", propietarioId)))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText());
    }

    private String vehiculoJson(String placa, String tipo, UUID propietarioId) {
        return """
                {"placa":"%s","tipo":"%s","marca":"Toyota","propietarioId":"%s"}
                """.formatted(placa, tipo, propietarioId);
    }
}
