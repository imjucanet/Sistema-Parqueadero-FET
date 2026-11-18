package com.fet.parqueadero.adapter.in.graphql.controller;

import com.fet.parqueadero.adapter.in.graphql.Paginacion;
import com.fet.parqueadero.adapter.in.graphql.dto.EspacioInput;
import com.fet.parqueadero.application.port.in.GestionEspaciosUseCase;
import com.fet.parqueadero.domain.model.Espacio;
import com.fet.parqueadero.domain.model.EstadoEspacio;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/** Controlador GraphQL de espacios (adaptador de entrada). */
@Controller
public class EspacioGraphQLController {

    private final GestionEspaciosUseCase espacios;

    public EspacioGraphQLController(GestionEspaciosUseCase espacios) {
        this.espacios = espacios;
    }

    @QueryMapping
    public Espacio espacio(@Argument UUID id) {
        return espacios.obtenerEspacio(id);
    }

    @QueryMapping
    public List<Espacio> espacios(@Argument Integer pagina, @Argument Integer tamano) {
        return espacios.listarEspacios(Paginacion.de(pagina, tamano)).getContent();
    }

    @QueryMapping
    public List<Espacio> espaciosDisponibles(@Argument Integer pagina, @Argument Integer tamano) {
        return espacios.listarEspaciosDisponibles(Paginacion.de(pagina, tamano)).getContent();
    }

    /** Equivale a {@code POST /api/espacios}: el espacio nace DISPONIBLE. */
    @MutationMapping
    public Espacio registrarEspacio(@Argument @Valid EspacioInput input) {
        return espacios.registrarEspacio(new Espacio(null, input.numero(), EstadoEspacio.DISPONIBLE));
    }
}
