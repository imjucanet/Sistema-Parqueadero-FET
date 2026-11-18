import { useCallback, useEffect, useState } from 'react';
import {
  consultarPanel,
  listarVehiculosParaEntrada,
  registrarEntrada,
  registrarSalida,
} from '../api/graphqlParqueadero';
import { GRAPHQL_URL, GRAPHQL_WS_URL } from '../api/graphqlClient';
import { useMovimientosEnVivo } from '../hooks/useMovimientosEnVivo';
import DataTable from '../components/DataTable';
import Banner from '../components/Banner';

const ETIQUETA_ESTADO = {
  inactivo: 'pausada',
  conectando: 'conectando...',
  conectado: 'en vivo',
  desconectado: 'reconectando...',
  finalizado: 'cerrada por el servidor',
  cerrado: 'cerrada',
};

function formatFecha(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString();
}

function formatHora(value) {
  if (!value) return '-';
  return new Date(value).toLocaleTimeString();
}

export default function GraphQLPage() {
  const [panel, setPanel] = useState(null);
  const [vehiculos, setVehiculos] = useState([]);
  const [vehiculoId, setVehiculoId] = useState('');
  const [espacioId, setEspacioId] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState(null);
  const [errorFormulario, setErrorFormulario] = useState(null);
  const [aviso, setAviso] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const [escuchando, setEscuchando] = useState(true);

  const {
    eventos,
    totalRecibidos,
    estado,
    error: errorSubscription,
    limpiar,
  } = useMovimientosEnVivo(escuchando);

  const recargar = useCallback(() => {
    setCargando(true);
    setError(null);
    return Promise.all([consultarPanel(), listarVehiculosParaEntrada()])
      .then(([datosPanel, listaVehiculos]) => {
        setPanel(datosPanel);
        setVehiculos(listaVehiculos);
      })
      .catch((err) => setError(err.message))
      .finally(() => setCargando(false));
  }, []);

  useEffect(() => {
    recargar();
  }, [recargar]);

  // Cada evento de la subscription refleja un cambio ya confirmado en la base de
  // datos, así que es el momento exacto para refrescar los totales del panel.
  useEffect(() => {
    if (totalRecibidos > 0) recargar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [totalRecibidos]);

  async function manejarEntrada(event) {
    event.preventDefault();
    setEnviando(true);
    setErrorFormulario(null);
    setAviso(null);
    try {
      const movimiento = await registrarEntrada({
        vehiculoId,
        espacioId: espacioId || null,
      });
      setAviso(`Entrada registrada en el espacio ${movimiento.espacio.numero}.`);
      setVehiculoId('');
      setEspacioId('');
      await recargar();
    } catch (err) {
      setErrorFormulario(err.mensajes?.join(' · ') || err.message);
    } finally {
      setEnviando(false);
    }
  }

  async function manejarSalida(movimientoId) {
    setError(null);
    setAviso(null);
    try {
      const movimiento = await registrarSalida(movimientoId);
      setAviso(`Salida registrada. Permanencia: ${movimiento.tiempoPermanenciaMinutos} min.`);
      await recargar();
    } catch (err) {
      setError(err.mensajes?.join(' · ') || err.message);
    }
  }

  const activos = panel?.movimientosActivos ?? [];
  const disponibles = panel?.espaciosDisponibles ?? [];
  const propietarios = panel?.propietarios ?? [];

  return (
    <section>
      <h2>GraphQL</h2>

      <div className="card">
        <p className="api-url">
          Endpoint: {GRAPHQL_URL} · Subscription: {GRAPHQL_WS_URL}
        </p>
        <p className="hint">
          Todo el panel se arma con <strong>una sola consulta</strong>, incluidas las relaciones
          anidadas (movimiento → vehículo → propietario). La misma vista contra REST necesitaría
          una llamada por lista más una por cada relación, porque los DTOs solo traen los ids.
        </p>
      </div>

      <Banner message={error} />
      <Banner message={aviso} type="info" />

      <div className="metricas">
        <div className="metrica">
          <span className="metrica-valor">{disponibles.length}</span>
          <span className="metrica-label">Espacios disponibles</span>
        </div>
        <div className="metrica">
          <span className="metrica-valor">{activos.length}</span>
          <span className="metrica-label">Vehículos dentro</span>
        </div>
        <div className="metrica">
          <span className="metrica-valor">{propietarios.length}</span>
          <span className="metrica-label">Propietarios</span>
        </div>
      </div>

      <form className="card form-grid" onSubmit={manejarEntrada}>
        <h3>Registrar entrada (mutation)</h3>
        <Banner message={errorFormulario} />
        <label>
          Vehículo
          <select
            value={vehiculoId}
            onChange={(event) => setVehiculoId(event.target.value)}
            required
          >
            <option value="">Seleccione...</option>
            {vehiculos.map((vehiculo) => (
              <option key={vehiculo.id} value={vehiculo.id}>
                {vehiculo.placa} — {vehiculo.marca} ({vehiculo.propietario.nombreCompleto})
              </option>
            ))}
          </select>
        </label>
        <label>
          Espacio (opcional)
          <select value={espacioId} onChange={(event) => setEspacioId(event.target.value)}>
            <option value="">Asignar el primero disponible</option>
            {disponibles.map((espacio) => (
              <option key={espacio.id} value={espacio.id}>
                {espacio.numero}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={enviando || vehiculos.length === 0}>
          {enviando ? 'Registrando...' : 'Registrar entrada'}
        </button>
      </form>

      <div className="card">
        <div className="card-header">
          <h3>Movimientos en vivo (subscription)</h3>
          <div className="vivo-controles">
            <span className={`estado-pill estado-${estado}`}>{ETIQUETA_ESTADO[estado] ?? estado}</span>
            <button onClick={() => setEscuchando((previo) => !previo)}>
              {escuchando ? 'Pausar' : 'Reanudar'}
            </button>
            <button onClick={limpiar} disabled={eventos.length === 0}>
              Limpiar
            </button>
          </div>
        </div>
        <Banner message={errorSubscription} />
        <p className="hint">
          El servidor emite cada movimiento después del commit en PostgreSQL, el mismo evento que
          alimenta el tópico de Kafka. Registra una entrada o una salida y aparecerá aquí sin
          recargar.
        </p>
        <DataTable
          columns={[
            { key: 'recibidoEn', label: 'Recibido', render: (row) => formatHora(row.recibidoEn) },
            { key: 'placa', label: 'Placa', render: (row) => row.vehiculo.placa },
            { key: 'espacio', label: 'Espacio', render: (row) => row.espacio.numero },
            { key: 'estado', label: 'Estado' },
            {
              key: 'minutos',
              label: 'Minutos',
              render: (row) => row.tiempoPermanenciaMinutos ?? '-',
            },
          ]}
          rows={eventos}
          emptyLabel={
            escuchando
              ? 'Esperando movimientos... registra una entrada para verlo aquí.'
              : 'Subscription pausada.'
          }
        />
      </div>

      <div className="card">
        <h3>Vehículos dentro del parqueadero</h3>
        {cargando && !panel ? (
          <p>Cargando...</p>
        ) : (
          <DataTable
            columns={[
              { key: 'placa', label: 'Placa', render: (row) => row.vehiculo.placa },
              {
                key: 'vehiculo',
                label: 'Vehículo',
                render: (row) => `${row.vehiculo.marca} (${row.vehiculo.tipo})`,
              },
              {
                key: 'propietario',
                label: 'Propietario',
                render: (row) =>
                  `${row.vehiculo.propietario.nombreCompleto} · ${row.vehiculo.propietario.tipoUsuario}`,
              },
              { key: 'espacio', label: 'Espacio', render: (row) => row.espacio.numero },
              {
                key: 'fechaEntrada',
                label: 'Entrada',
                render: (row) => formatFecha(row.fechaEntrada),
              },
              {
                key: 'acciones',
                label: 'Acciones',
                render: (row) => (
                  <button onClick={() => manejarSalida(row.id)}>Registrar salida</button>
                ),
              },
            ]}
            rows={activos}
            emptyLabel="No hay vehículos dentro"
          />
        )}
      </div>

      <div className="card">
        <h3>Propietarios y sus vehículos</h3>
        <p className="hint">
          Esta relación viene resuelta con <code>@BatchMapping</code>: una consulta para todos los
          propietarios, no una por cada uno.
        </p>
        <DataTable
          columns={[
            { key: 'nombreCompleto', label: 'Propietario' },
            { key: 'tipoUsuario', label: 'Tipo' },
            {
              key: 'vehiculos',
              label: 'Vehículos',
              render: (row) =>
                row.vehiculos.length === 0
                  ? '-'
                  : row.vehiculos.map((v) => `${v.placa} (${v.marca})`).join(', '),
            },
          ]}
          rows={propietarios}
          emptyLabel="Sin propietarios registrados"
        />
      </div>
    </section>
  );
}
