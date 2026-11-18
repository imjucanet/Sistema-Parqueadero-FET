package com.fet.parqueadero.adapter.in.graphql;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Traduce los argumentos opcionales {@code pagina}/{@code tamano} del esquema
 * GraphQL al {@link Pageable} que esperan los casos de uso.
 *
 * <p>Si el cliente no envía ninguno de los dos se devuelven todos los registros,
 * que es el comportamiento que tenían estas consultas antes de exponer
 * paginación; así los documentos GraphQL existentes siguen funcionando.</p>
 */
public final class Paginacion {

    private static final int TAMANO_POR_DEFECTO = 20;

    private Paginacion() {
    }

    public static Pageable de(Integer pagina, Integer tamano) {
        if (pagina == null && tamano == null) {
            return Pageable.unpaged();
        }
        return PageRequest.of(
                pagina == null ? 0 : pagina,
                tamano == null ? TAMANO_POR_DEFECTO : tamano);
    }
}
