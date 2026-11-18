import { get, post, put, buildQuery } from './client';

export const TIPOS_USUARIO = ['ESTUDIANTE', 'DOCENTE', 'ADMINISTRATIVO', 'VISITANTE'];

export function listarPropietarios({ page = 0, size = 20 } = {}) {
  return get(`/api/propietarios${buildQuery({ page, size })}`);
}

export function obtenerPropietario(id) {
  return get(`/api/propietarios/${id}`);
}

export function registrarPropietario(data) {
  return post('/api/propietarios', data);
}

export function actualizarPropietario(id, data) {
  return put(`/api/propietarios/${id}`, data);
}
