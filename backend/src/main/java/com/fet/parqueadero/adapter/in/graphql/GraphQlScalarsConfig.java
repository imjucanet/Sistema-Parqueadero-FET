package com.fet.parqueadero.adapter.in.graphql;

import graphql.scalars.ExtendedScalars;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.context.annotation.Bean;

/**
 * Registra el scalar {@code DateTime} usado por {@code Movimiento.fechaEntrada}
 * y {@code Movimiento.fechaSalida}.
 *
 * <p>Antes esos campos se exponían como {@code String} y cada resolver llamaba
 * a {@code toString()} a mano. Con el scalar, graphql-java serializa el
 * {@link java.time.OffsetDateTime} del dominio en ISO-8601 y el esquema declara
 * el tipo real del dato.</p>
 */
@Configuration
public class GraphQlScalarsConfig {

    @Bean
    public RuntimeWiringConfigurer scalarsConfigurer() {
        return wiring -> wiring.scalar(ExtendedScalars.DateTime);
    }
}
