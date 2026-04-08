/**
 * Endpoints centralizados da API futura.
 * Facilita manutenção e evita strings duplicadas nos services.
 */

export const API_ENDPOINTS = {
  // Autenticação
  login: '/auth/login',
  logout: '/auth/logout',
  me: '/auth/me',

  // Catálogo
  homeFeed: '/catalog/home',
  movies: '/catalog/movies',
  series: '/catalog/series',
  channels: '/catalog/channels',
  categories: '/catalog/categories',
  search: '/catalog/search',

  // Perfil
  profile: '/profile',
  myList: '/profile/my-list',
  watchHistory: '/profile/watch-history',
} as const;
