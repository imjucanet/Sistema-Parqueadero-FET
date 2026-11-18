import { get, post, put, buildQuery } from './client';

export const TIPOS_VEHICULO = ['AUTOMOVIL', 'MOTOCICLETA', 'OTRO'];

export function listarVehiculos({ page = 0, size = 20 } = {}) {
  return get(`/api/vehiculos${buildQuery({ page, size })}`);
}

export function obtenerVehiculo(id) {
  return get(`/api/vehiculos/${id}`);
}

export function obtenerMarcasPorTipo() {
  return get('/api/vehiculos/marcas');
}

export function registrarVehiculo(data) {
  return post('/api/vehiculos', data);
}

export function registrarVehiculosEnLote(vehiculos) {
  return post('/api/vehiculos/lote', { vehiculos });
}

export function actualizarVehiculo(id, data) {
  return put(`/api/vehiculos/${id}`, data);
}
