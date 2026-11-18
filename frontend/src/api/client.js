const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081';

export class ApiError extends Error {
  constructor(status, body) {
    super(body?.mensaje || `Error HTTP ${status}`);
    this.status = status;
    this.codigo = body?.codigo;
  }
}

async function request(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options.headers },
    ...options,
  });

  if (response.status === 204) return null;

  const isJson = response.headers.get('content-type')?.includes('application/json');
  const body = isJson ? await response.json() : null;

  if (!response.ok) {
    throw new ApiError(response.status, body);
  }

  return body;
}

export function buildQuery(params = {}) {
  const query = Object.entries(params)
    .filter(([, value]) => value !== undefined && value !== null && value !== '')
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
    .join('&');
  return query ? `?${query}` : '';
}

export function get(path) {
  return request(path, { method: 'GET' });
}

export function post(path, body) {
  return request(path, { method: 'POST', body: body ? JSON.stringify(body) : undefined });
}

export function put(path, body) {
  return request(path, { method: 'PUT', body: body ? JSON.stringify(body) : undefined });
}

export { BASE_URL };
