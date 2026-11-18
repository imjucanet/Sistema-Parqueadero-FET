import { BASE_URL } from './client';

export const GRAPHQL_URL = `${BASE_URL}/graphql`;

// El endpoint de subscriptions es el mismo /graphql, pero sobre WebSocket
// (spring.graphql.websocket.path en application.yml).
export const GRAPHQL_WS_URL = `${GRAPHQL_URL.replace(/^http/, 'ws')}`;

/**
 * Error devuelto por el backend en el array `errors` de la respuesta GraphQL.
 *
 * Expone el mismo `codigo` estable que ErrorResponse en REST, porque
 * GraphQLExceptionResolver lo publica en `extensions.codigo`.
 */
export class GraphQLRequestError extends Error {
  constructor(errors) {
    const primero = errors?.[0];
    super(primero?.message || 'Error en la consulta GraphQL');
    this.codigo = primero?.extensions?.codigo;
    this.errors = errors ?? [];
  }

  /** Todos los mensajes, útil cuando la validación reporta varios campos. */
  get mensajes() {
    return this.errors.map((error) => error.message);
  }
}

/**
 * Ejecuta una query o mutation por HTTP.
 *
 * A diferencia de REST, GraphQL responde 200 incluso con errores de negocio:
 * el fallo viene en el array `errors`, así que hay que revisarlo explícitamente.
 */
export async function gql(document, variables = {}) {
  const response = await fetch(GRAPHQL_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query: document, variables }),
  });

  if (!response.ok) {
    throw new Error(`Error HTTP ${response.status} al consultar ${GRAPHQL_URL}`);
  }

  const { data, errors } = await response.json();
  if (errors?.length) {
    throw new GraphQLRequestError(errors);
  }
  return data;
}

/**
 * Abre una subscription usando el subprotocolo `graphql-transport-ws`, que es
 * el que implementa el GraphQlWebSocketHandler de Spring.
 *
 * El handshake es: connection_init -> connection_ack -> subscribe, y a partir de
 * ahí el servidor envía un mensaje `next` por cada evento emitido.
 *
 * @returns función para cerrar la subscription.
 */
export function gqlSubscribe(document, { variables = {}, onData, onError, onStatus } = {}) {
  const idOperacion = '1';
  let socket;
  let cerradoPorElCliente = false;

  try {
    socket = new WebSocket(GRAPHQL_WS_URL, 'graphql-transport-ws');
  } catch (err) {
    onError?.(err);
    return () => {};
  }

  onStatus?.('conectando');

  socket.addEventListener('open', () => {
    socket.send(JSON.stringify({ type: 'connection_init' }));
  });

  socket.addEventListener('message', (evento) => {
    let mensaje;
    try {
      mensaje = JSON.parse(evento.data);
    } catch {
      return;
    }

    switch (mensaje.type) {
      case 'connection_ack':
        socket.send(
          JSON.stringify({
            id: idOperacion,
            type: 'subscribe',
            payload: { query: document, variables },
          }),
        );
        onStatus?.('conectado');
        break;

      case 'next':
        if (mensaje.payload?.errors?.length) {
          onError?.(new GraphQLRequestError(mensaje.payload.errors));
        } else if (mensaje.payload?.data) {
          onData?.(mensaje.payload.data);
        }
        break;

      case 'error':
        onError?.(new GraphQLRequestError(mensaje.payload));
        break;

      case 'complete':
        onStatus?.('finalizado');
        break;

      // Keep-alive del protocolo: hay que responder para no perder la conexión.
      case 'ping':
        socket.send(JSON.stringify({ type: 'pong' }));
        break;

      default:
        break;
    }
  });

  socket.addEventListener('error', () => {
    if (!cerradoPorElCliente) {
      onError?.(new Error(`No fue posible conectar con ${GRAPHQL_WS_URL}`));
    }
  });

  socket.addEventListener('close', () => {
    if (!cerradoPorElCliente) {
      onStatus?.('desconectado');
    }
  });

  return () => {
    cerradoPorElCliente = true;
    onStatus?.('cerrado');
    if (socket.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ id: idOperacion, type: 'complete' }));
    }
    socket.close();
  };
}
