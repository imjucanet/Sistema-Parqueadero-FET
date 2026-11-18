import { useEffect, useState } from 'react';
import {
  listarMovimientosActivos,
  listarHistorialMovimientos,
  registrarEntrada,
  registrarSalida,
} from '../api/movimientos';
import { listarVehiculos } from '../api/vehiculos';
import { usePagedResource } from '../hooks/usePagedResource';
import DataTable from '../components/DataTable';
import Pagination from '../components/Pagination';
import Banner from '../components/Banner';

function formatFecha(value) {
  if (!value) return '-';
  return new Date(value).toLocaleString();
}

export default function MovimientosPage() {
  const [vista, setVista] = useState('activos');
  const fetcher = vista === 'activos' ? listarMovimientosActivos : listarHistorialMovimientos;
  const { data, page, setPage, loading, error, reload } = usePagedResource(fetcher, [vista]);

  const [vehiculos, setVehiculos] = useState([]);
  const [vehiculoId, setVehiculoId] = useState('');
  const [formError, setFormError] = useState(null);
  const [actionError, setActionError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    listarVehiculos({ page: 0, size: 100 })
      .then((result) => setVehiculos(result.content))
      .catch(() => setVehiculos([]));
  }, []);

  async function handleEntrada(event) {
    event.preventDefault();
    setSubmitting(true);
    setFormError(null);
    try {
      await registrarEntrada(vehiculoId);
      setVehiculoId('');
      reload();
    } catch (err) {
      setFormError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleSalida(id) {
    setActionError(null);
    try {
      await registrarSalida(id);
      reload();
    } catch (err) {
      setActionError(err.message);
    }
  }

  return (
    <section>
      <h2>Movimientos</h2>

      <form className="card form-grid" onSubmit={handleEntrada}>
        <h3>Registrar entrada</h3>
        <Banner message={formError} />
        <label>
          Vehículo
          <select value={vehiculoId} onChange={(event) => setVehiculoId(event.target.value)} required>
            <option value="">Seleccione...</option>
            {vehiculos.map((vehiculo) => (
              <option key={vehiculo.id} value={vehiculo.id}>
                {vehiculo.placa} - {vehiculo.marca}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Registrando...' : 'Registrar entrada'}
        </button>
      </form>

      <div className="card">
        <div className="card-header">
          <h3>Listado</h3>
          <div className="tab-switch">
            <button
              className={vista === 'activos' ? 'active' : ''}
              onClick={() => {
                setVista('activos');
                setPage(0);
              }}
            >
              Activos
            </button>
            <button
              className={vista === 'historial' ? 'active' : ''}
              onClick={() => {
                setVista('historial');
                setPage(0);
              }}
            >
              Historial
            </button>
          </div>
        </div>
        <Banner message={error} />
        <Banner message={actionError} />
        {loading ? (
          <p>Cargando...</p>
        ) : (
          <>
            <DataTable
              columns={[
                { key: 'vehiculoId', label: 'Vehículo ID' },
                { key: 'espacioId', label: 'Espacio ID' },
                { key: 'fechaEntrada', label: 'Entrada', render: (row) => formatFecha(row.fechaEntrada) },
                { key: 'fechaSalida', label: 'Salida', render: (row) => formatFecha(row.fechaSalida) },
                { key: 'tiempoPermanenciaMinutos', label: 'Minutos' },
                { key: 'estado', label: 'Estado' },
                {
                  key: 'acciones',
                  label: 'Acciones',
                  render: (row) =>
                    row.estado !== 'FINALIZADO' ? (
                      <button onClick={() => handleSalida(row.id)}>Registrar salida</button>
                    ) : null,
                },
              ]}
              rows={data.content}
            />
            <Pagination
              page={page}
              totalPages={data.totalPages}
              hasNext={data.hasNext}
              hasPrevious={data.hasPrevious}
              onChange={setPage}
            />
          </>
        )}
      </div>
    </section>
  );
}
