package com.fet.parqueadero.adapter.in.graphql.dto;

import java.util.List;

/** Una marca del catálogo con sus modelos conocidos. */
public record MarcaConModelos(String marca, List<String> modelos) {
}
