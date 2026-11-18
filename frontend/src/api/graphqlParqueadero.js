import { gql, gqlSubscribe } from './graphqlClient';

/**
 * Todo el estado del panel en UNA sola petición.
 *
 * Es lo que GraphQL aporta frente a REST aquí: con la API REST este mismo panel
 * necesita 3 llamadas de lista (`/api/movimientos/activos`,
 * `/api/espacios/disponibles`, `/api/propietarios`) y además una llamada por
 * cada movimiento para resolver su vehículo y una por cada vehículo para su
 * propietario, porque `Movimiento` solo trae `vehiculoId` y `espacioId`.
 * El servidor resuelve esas relaciones con @BatchMapping.
 */
const PANEL = `
  query Panel($tamano: Int!) {
    espaciosDisponibles {
      id
      numero
    }
    movimientosActivos(pagina: 0, tamano: $tamano) {
      id
      fechaEntrada
      estado
      vehiculo {
        placa
        marca
        tipo
        propietario {
          nombreCompleto
          tipoUsuario
        }
      }
      espacio {
        numero
      }
    }
    propietarios(pagina: 0, tamano: $tamano) {
      id
      nombreCompleto
      tipoUsuario
      vehiculos {
        id
        placa
        marca
      }
    }
  }
`;

export function consultarPanel({ tamano = 20 } = {}) {
  return gql(PANEL, { tamano });
}

const VEHICULOS_PARA_ENTRADA = `
  query VehiculosParaEntrada {
    vehiculos {
      id
      placa
      marca
      propietario {
        nombreCompleto
      }
    }
  }
`;

export function listarVehiculosParaEntrada() {
  return gql(VEHICULOS_PARA_ENTRADA).then((data) => data.vehiculos);
}

const REGISTRAR_ENTRADA = `
  mutation RegistrarEntrada($vehiculoId: ID!, $espacioId: ID) {
    registrarEntrada(vehiculoId: $vehiculoId, espacioId: $espacioId) {
      id
      estado
      espacio {
        numero
      }
    }
  }
`;

export function registrarEntrada({ vehiculoId, espacioId = null }) {
  return gql(REGISTRAR_ENTRADA, { vehiculoId, espacioId }).then((data) => data.registrarEntrada);
}

const REGISTRAR_SALIDA = `
  mutation RegistrarSalida($movimientoId: ID!) {
    registrarSalida(movimientoId: $movimientoId) {
      id
      estado
      tiempoPermanenciaMinutos
    }
  }
`;

export function registrarSalida(movimientoId) {
  return gql(REGISTRAR_SALIDA, { movimientoId }).then((data) => data.registrarSalida);
}

/**
 * Subscription alimentada por el mismo evento de aplicación que publica en
 * Kafka, con @TransactionalEventListener(AFTER_COMMIT): un movimiento solo
 * llega aquí si ya quedó confirmado en PostgreSQL.
 */
const MOVIMIENTO_REGISTRADO = `
  subscription MovimientoRegistrado {
    movimientoRegistrado {
      id
      estado
      fechaEntrada
      fechaSalida
      tiempoPermanenciaMinutos
      vehiculo {
        placa
        marca
      }
      espacio {
        numero
      }
    }
  }
`;

export function suscribirseAMovimientos({ onMovimiento, onError, onStatus }) {
  return gqlSubscribe(MOVIMIENTO_REGISTRADO, {
    onData: (data) => onMovimiento?.(data.movimientoRegistrado),
    onError,
    onStatus,
  });
}
