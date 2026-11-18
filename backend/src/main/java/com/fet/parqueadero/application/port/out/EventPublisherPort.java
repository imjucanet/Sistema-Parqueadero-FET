package com.fet.parqueadero.application.port.out;

/**
 * Puerto de salida para la publicación de eventos operacionales.
 *
 * <p>El adaptador de producción debe publicar el evento {@code después} de que
 * la operación haya sido validada y persistida en PostgreSQL.</p>
 */
public interface EventPublisherPort {

    void publicarMovimiento(MovimientoEvento evento);
}