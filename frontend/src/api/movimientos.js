import { get, post, buildQuery } from './client';

export function listarMovimientosActivos({ page = 0, size = 20 } = {}) {
  return get(`/api/movimientos/activos${buildQuery({ page, size })}`);
}

export function listarHistorialMovimientos({ page = 0, size = 20 } = {}) {
  return get(`/api/movimientos/historial${buildQuery({ page, size })}`);
}

export function registrarEntrada(vehiculoId) {
  return post('/api/movimientos/entrada', { vehiculoId });
}

export function registrarSalida(id) {
  return post(`/api/movimientos/${id}/salida`);
}
