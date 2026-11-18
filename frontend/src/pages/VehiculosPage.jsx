import { useEffect, useState } from 'react';
import { listarVehiculos, registrarVehiculo, obtenerMarcasPorTipo, TIPOS_VEHICULO } from '../api/vehiculos';
import { listarPropietarios } from '../api/propietarios';
import { usePagedResource } from '../hooks/usePagedResource';
import DataTable from '../components/DataTable';
import Pagination from '../components/Pagination';
import Banner from '../components/Banner';

const FORM_INICIAL = {
  placa: '',
  tipo: TIPOS_VEHICULO[0],
  marca: '',
  modelo: '',
  propietarioId: '',
};

export default function VehiculosPage() {
  const { data, page, setPage, loading, error, reload } = usePagedResource(listarVehiculos);
  const [propietarios, setPropietarios] = useState([]);
  const [marcasPorTipo, setMarcasPorTipo] = useState({});
  const [form, setForm] = useState(FORM_INICIAL);
  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    listarPropietarios({ page: 0, size: 100 })
      .then((result) => setPropietarios(result.content))
      .catch(() => setPropietarios([]));

    obtenerMarcasPorTipo()
      .then((result) => setMarcasPorTipo(result))
      .catch(() => setMarcasPorTipo({}));
  }, []);

  const modelosPorMarca = marcasPorTipo[form.tipo] || {};
  const marcasDisponibles = Object.keys(modelosPorMarca);
  const modelosDisponibles = modelosPorMarca[form.marca] || [];

  function handleChange(event) {
    const { name, value } = event.target;
    if (name === 'tipo') {
      setForm((prev) => ({ ...prev, tipo: value, marca: '', modelo: '' }));
      return;
    }
    if (name === 'marca') {
      setForm((prev) => ({ ...prev, marca: value, modelo: '' }));
      return;
    }
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setFormError(null);
    try {
      await registrarVehiculo(form);
      setForm(FORM_INICIAL);
      reload();
    } catch (err) {
      setFormError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section>
      <h2>Vehículos</h2>

      <form className="card form-grid" onSubmit={handleSubmit}>
        <h3>Registrar vehículo</h3>
        <Banner message={formError} />
        <label>
          Placa
          <input name="placa" value={form.placa} onChange={handleChange} required />
        </label>
        <label>
          Tipo
          <select name="tipo" value={form.tipo} onChange={handleChange}>
            {TIPOS_VEHICULO.map((tipo) => (
              <option key={tipo} value={tipo}>
                {tipo}
              </option>
            ))}
          </select>
        </label>
        <label>
          Marca
          {marcasDisponibles.length > 0 ? (
            <select name="marca" value={form.marca} onChange={handleChange} required>
              <option value="">Seleccione...</option>
              {marcasDisponibles.map((marca) => (
                <option key={marca} value={marca}>
                  {marca}
                </option>
              ))}
            </select>
          ) : (
            <input name="marca" value={form.marca} onChange={handleChange} required />
          )}
        </label>
        <label>
          Modelo
          {modelosDisponibles.length > 0 ? (
            <select name="modelo" value={form.modelo} onChange={handleChange}>
              <option value="">Seleccione...</option>
              {modelosDisponibles.map((modelo) => (
                <option key={modelo} value={modelo}>
                  {modelo}
                </option>
              ))}
            </select>
          ) : (
            <input name="modelo" value={form.modelo} onChange={handleChange} />
          )}
        </label>
        <label>
          Propietario
          <select name="propietarioId" value={form.propietarioId} onChange={handleChange} required>
            <option value="">Seleccione...</option>
            {propietarios.map((propietario) => (
              <option key={propietario.id} value={propietario.id}>
                {propietario.nombreCompleto} ({propietario.numeroDocumento})
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={submitting}>
          {submitting ? 'Registrando...' : 'Registrar'}
        </button>
      </form>

      <div className="card">
        <h3>Listado</h3>
        <Banner message={error} />
        {loading ? (
          <p>Cargando...</p>
        ) : (
          <>
            <DataTable
              columns={[
                { key: 'placa', label: 'Placa' },
                { key: 'tipo', label: 'Tipo' },
                { key: 'marca', label: 'Marca' },
                { key: 'modelo', label: 'Modelo' },
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
