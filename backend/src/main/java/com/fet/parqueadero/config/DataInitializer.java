package com.fet.parqueadero.config;

import com.fet.parqueadero.application.port.out.EspacioRepositoryPort;
import com.fet.parqueadero.domain.model.EstadoEspacio;
import com.fet.parqueadero.domain.model.Espacio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inicializa la información de referencia del parqueadero.
 *
 * <p>El contrato ya expone {@code POST /api/espacios} para registrar espacios
 * adicionales; esta siembra se mantiene solo para tener datos base DISPONIBLE
 * listos la primera vez que la aplicación arranca (tabla vacía).</p>
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final EspacioRepositoryPort espacios;
    private final int totalEspacios;

    public DataInitializer(
            EspacioRepositoryPort espacios,
            @Value("${parqueadero.espacios-iniciales:20}") int totalEspacios) {
        this.espacios = espacios;
        this.totalEspacios = totalEspacios;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!espacios.findAll(PageRequest.of(0, 1)).isEmpty()) {
            return;
        }
        for (int i = 1; i <= totalEspacios; i++) {
            espacios.save(new Espacio(null, "E-%02d".formatted(i), EstadoEspacio.DISPONIBLE));
        }
    }
}