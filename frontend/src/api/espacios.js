import { get, post, buildQuery } from './client';

export function listarEspacios({ page = 0, size = 20 } = {}) {
  return get(`/api/espacios${buildQuery({ page, size })}`);
}

export function listarEspaciosDisponibles({ page = 0, size = 20 } = {}) {
  return get(`/api/espacios/disponibles${buildQuery({ page, size })}`);
}

export function registrarEspacio(data) {
  return post('/api/espacios', data);
}
