-- Tabla de auditoría para los eventos consumidos del tópico parqueadero.movimientos.
-- EVENT_ID es la clave primaria: garantiza idempotencia a nivel de base de datos
-- (un evento con el mismo id no puede insertarse dos veces).

CREATE TABLE AUDITORIA_EVENTOS (
    EVENT_ID                    VARCHAR(36) PRIMARY KEY,
    EVENT_TYPE                  VARCHAR(30) NOT NULL,
    EVENT_TIMESTAMP              TIMESTAMP WITH TIME ZONE NOT NULL,
    MOVIMIENTO_ID                 BIGINT NOT NULL,
    VEHICULO_ID                   BIGINT NOT NULL,
    PLACA                         VARCHAR(20) NOT NULL,
    ESPACIO_ID                    BIGINT NOT NULL,
    ESPACIO_NUMERO                VARCHAR(10) NOT NULL,
    TIEMPO_PERMANENCIA_MINUTOS    BIGINT,
    FECHA_PROCESAMIENTO           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IDX_AUDITORIA_MOVIMIENTO ON AUDITORIA_EVENTOS (MOVIMIENTO_ID);
CREATE INDEX IDX_AUDITORIA_EVENT_TYPE ON AUDITORIA_EVENTOS (EVENT_TYPE);
