/**
 * Endpoints centralizados da API futura.
 * Facilita manutenção e evita strings duplicadas nos services.
 */

export const API_ENDPOINTS = {
  // Autenticação
  login: "/auth/login",
  logout: "/auth/logout",
  me: "/auth/me",

  // Catálogo
  homeFeed: "/catalog/home",
  movies: {
    catalog: "/catalog/movies",
    all: "/api/movies/all",
  },
  moviesTrending: "/api/movies/trending",
  moviesByCategory: (parent: string, sub: string) =>
    `/api/playlist/categories/${encodeURIComponent(parent)}/${encodeURIComponent(sub)}`,
  series: {
    catalog: "/catalog/series",
    all: "/api/series/all",
  },
  seriesTrending: "/api/series/trending",
  seriesTopRated: "/api/series/top-rated",
  seriesPopular: "/api/series/popular",
  seriesPlaylist: "/api/series/playlist",
  channels: "/catalog/channels",
  categories: "/catalog/categories",
  search: {
    catalog: "/catalog/search",
    global: "/api/search",
  },

  // Perfil
  profile: "/profile",
  myList: "/profile/my-list",
  watchHistory: "/profile/watch-history",

  auth: {
    login: "/api/auth/login",
  },
  live: {
    list: "/api/live",
  },
  favorites: {
    list: "/api/favorites",
    item: (channelId: string) =>
      `/api/favorites/${encodeURIComponent(channelId)}`,
  },
  history: {
    list: "/api/history",
    item: (channelId: string) =>
      `/api/history/${encodeURIComponent(channelId)}`,
  },
  epg: {
    now: (channelId: string) =>
      `/api/epg/channel/${encodeURIComponent(channelId)}/now`,
  },
  health: "/api/health",
} as const;
