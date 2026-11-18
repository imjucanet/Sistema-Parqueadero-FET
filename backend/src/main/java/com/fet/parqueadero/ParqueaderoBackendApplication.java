package com.fet.parqueadero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del microservicio backend del sistema de gestión
 * de parqueadero institucional de la FET.
 *
 * <p>Expone la API REST definida en {@code src/main/resources/api/openapi.yaml}
 * sobre el puerto {@code 8080} y actúa como productor de los eventos Kafka
 * {@code VEHICULO_INGRESADO} y {@code VEHICULO_SALIDO}.</p>
 */
@SpringBootApplication
public class ParqueaderoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParqueaderoBackendApplication.class, args);
    }
}