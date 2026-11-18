package com.fet.parqueadero.application.port.out;

/** Tipo de evento publicado en el tópico {@code parqueadero.movimientos}. */
public enum TipoEventoMovimiento {
    VEHICULO_INGRESADO,
    VEHICULO_SALIDO
}