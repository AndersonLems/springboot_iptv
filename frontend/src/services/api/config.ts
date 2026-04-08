/**
 * Configuração centralizada da API.
 *
 * A URL base é lida de `import.meta.env.VITE_API_BASE_URL`.
 * Caso a variável não esteja definida, um fallback seguro é utilizado.
 */

export const API_BASE_URL: string =
  (typeof import.meta !== 'undefined' && import.meta.env?.VITE_API_BASE_URL) ||
  'http://localhost:3000';

export const API_TIMEOUT = 15_000; // 15 segundos

export const API_DEFAULTS = {
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
} as const;
