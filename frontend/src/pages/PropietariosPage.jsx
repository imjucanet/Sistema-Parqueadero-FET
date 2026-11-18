import { useState } from 'react';
import { listarPropietarios, registrarPropietario, TIPOS_USUARIO } from '../api/propietarios';
import { usePagedResource } from '../hooks/usePagedResource';
import DataTable from '../components/DataTable';
import Pagination from '../components/Pagination';
import Banner from '../components/Banner';

const FORM_INICIAL = {
  primerNombre: '',
  primerApellido: '',
  numeroDocumento: '',
  tipoUsuario: TIPOS_USUARIO[0],
  telefono: '',
  correo: '',
};

export default function PropietariosPage() {
  const { data, page, setPage, loading, error, reload } = usePagedResource(listarPropietarios);
  const [form, setForm] = useState(FORM_INICIAL);
  const [formError, setFormError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSubmitting(true);
    setFormError(null);
    try {
      await registrarPropietario(form);
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
      <h2>Propietarios</h2>

      <form className="card form-grid" onSubmit={handleSubmit}>
        <h3>Registrar propietario</h3>
        <Banner message={formError} />
        <label>
          Primer nombre
          <input name="primerNombre" value={form.primerNombre} onChange={handleChange} required />
        </label>
        <label>
          Primer apellido
          <input name="primerApellido" value={form.primerApellido} onChange={handleChange} required />
        </label>
        <label>
          Número de documento
          <input name="numeroDocumento" value={form.numeroDocumento} onChange={handleChange} required />
        </label>
        <label>
          Tipo de usuario
          <select name="tipoUsuario" value={form.tipoUsuario} onChange={handleChange}>
            {TIPOS_USUARIO.map((tipo) => (
              <option key={tipo} value={tipo}>
                {tipo}
              </option>
            ))}
          </select>
        </label>
        <label>
          Teléfono
          <input name="telefono" value={form.telefono} onChange={handleChange} />
        </label>
        <label>
          Correo
          <input type="email" name="correo" value={form.correo} onChange={handleChange} />
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
                { key: 'nombreCompleto', label: 'Nombre' },
                { key: 'numeroDocumento', label: 'Documento' },
                { key: 'tipoUsuario', label: 'Tipo' },
                { key: 'telefono', label: 'Teléfono' },
                { key: 'correo', label: 'Correo' },
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
