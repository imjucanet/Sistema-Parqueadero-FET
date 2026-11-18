package com.fet.auditoria.exception;

/**
 * Se lanza cuando el mensaje recibido no se puede parsear o no cumple la
 * estructura esperada (campo obligatorio ausente, {@code eventType}
 * desconocido, JSON malformado, etc).
 *
 * <p>Es una excepción de negocio no transitoria: reintentarla no la resuelve,
 * así que el manejador de errores de Kafka la enruta directamente al tópico
 * dead-letter en lugar de reintentar.</p>
 */
public class EventoInvalidoException extends RuntimeException {

    public EventoInvalidoException(String mensaje) {
        super(mensaje);
    }

    public EventoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
