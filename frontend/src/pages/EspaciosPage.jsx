import { useState } from 'react';
import { listarEspacios, listarEspaciosDisponibles, registrarEspacio } from '../api/espacios';
import { usePagedResource } from '../hooks/usePagedResource';
import DataTable from '../components/DataTable';
import Pagination from '../components/Pagination';
import Banner from '../components/Banner';

export default function EspaciosPage() {
  const [soloDisponibles, setSoloDisponibles] = useState(false);
  const fetcher = soloDisponibles ? listarEspaciosDisponibles : listarEspacios;
  const { data, page, setPage, loading, error, reload } = usePagedResource(fetcher, [soloDisponibles]);

  const [numero, setNumero] = useState('');
  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setFormError(null);
    try {
      await registrarEspacio({ numero });
      setNumero('');
      reload();
    } catch (err) {
      setFormError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section>
      <h2>Espacios</h2>

      <form className="card form-grid" onSubmit={handleSubmit}>
        <h3>Registrar espacio</h3>
        <Banner message={formError} />
        <label>
          Número
          <input value={numero} onChange={(event) => setNumero(event.target.value)} placeholder="E-21" required />
        </label>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Registrando...' : 'Registrar'}
        </button>
      </form>

      <div className="card">
        <div className="card-header">
          <h3>Listado</h3>
          <label className="checkbox-label">
            <input
              type="checkbox"
              checked={soloDisponibles}
              onChange={(event) => {
                setSoloDisponibles(event.target.checked);
                setPage(0);
              }}
            />
            Solo disponibles
          </label>
        </div>
        <Banner message={error} />
        {loading ? (
          <p>Cargando...</p>
        ) : (
          <>
            <DataTable
              columns={[
                { key: 'numero', label: 'Número' },
                { key: 'estado', label: 'Estado' },
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
