import { useEffect, useRef, useState } from 'react';
import { suscribirseAMovimientos } from '../api/graphqlParqueadero';

const MAX_EVENTOS = 25;
const REINTENTO_MS = 3000;

/**
 * Mantiene abierta la subscription `movimientoRegistrado` y acumula los últimos
 * movimientos recibidos, el más reciente primero.
 *
 * Si el backend se reinicia la conexión se reabre sola. Ojo: el sink del
 * servidor es directBestEffort, así que lo ocurrido mientras estuvo caída no se
 * reenvía; para eso está la consulta de historial.
 *
 * @param activa si es false no se abre la conexión (permite pausar desde la UI).
 */
export function useMovimientosEnVivo(activa = true) {
  const [eventos, setEventos] = useState([]);
  // Contador monotono de eventos recibidos. `eventos.length` se estanca en
  // MAX_EVENTOS, asi que no sirve para que la vista detecte eventos nuevos.
  const [totalRecibidos, setTotalRecibidos] = useState(0);
  const [estadoConexion, setEstadoConexion] = useState('inactivo');
  const [error, setError] = useState(null);
  const reintento = useRef(null);

  useEffect(() => {
    if (!activa) return undefined;

    let cancelado = false;
    let cerrar = () => {};

    function conectar() {
      if (cancelado) return;

      cerrar = suscribirseAMovimientos({
        onMovimiento: (movimiento) => {
          if (cancelado) return;
          setEventos((previos) => [
            { ...movimiento, recibidoEn: new Date().toISOString() },
            ...previos.slice(0, MAX_EVENTOS - 1),
          ]);
          setTotalRecibidos((previo) => previo + 1);
        },
        onError: (err) => {
          if (!cancelado) setError(err.message);
        },
        onStatus: (nuevoEstado) => {
          if (cancelado) return;
          setEstadoConexion(nuevoEstado);
          if (nuevoEstado === 'conectado') setError(null);
          if (nuevoEstado === 'desconectado') {
            reintento.current = setTimeout(conectar, REINTENTO_MS);
          }
        },
      });
    }

    conectar();

    return () => {
      cancelado = true;
      clearTimeout(reintento.current);
      cerrar();
    };
  }, [activa]);

  return {
    eventos,
    totalRecibidos,
    // Cuando la UI pausa la escucha no hay conexión que reportar, así que el
    // estado visible se deriva en lugar de guardarse (evita un render extra).
    estado: activa ? estadoConexion : 'inactivo',
    error,
    limpiar: () => setEventos([]),
  };
}
