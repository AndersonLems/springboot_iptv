import type { Category, MediaItem, HomeFeed } from '@/types/media';
import { mockMovies, mockSeries, mockChannels, mockCategories, mockHomeFeed } from '@/data/mocks';
// import { apiClient } from './api/client';
// import { API_ENDPOINTS } from './api/endpoints';

const delay = (ms: number) => new Promise(r => setTimeout(r, ms));

export const catalogService = {
  /**
   * Retorna o feed da página inicial (hero + categorias).
   * Implementação atual: mock local.
   */
  async getHomeFeed(): Promise<HomeFeed> {
    // Implementação atual com mock
    await delay(300);
    return mockHomeFeed;

    // Futuramente:
    // const response = await apiClient.get<HomeFeed>(API_ENDPOINTS.homeFeed);
    // return response;
  },

  /**
   * Retorna categorias filtradas por filmes.
   * Implementação atual: mock local.
   */
  async getMovies(): Promise<Category[]> {
    // Implementação atual com mock
    await delay(200);
    return mockCategories
      .map(c => ({ ...c, items: c.items.filter(i => i.type === 'movie') }))
      .filter(c => c.items.length > 0);

    // Futuramente:
    // const response = await apiClient.get<Category[]>(API_ENDPOINTS.movies);
    // return response;
  },

  /**
   * Retorna categorias filtradas por séries.
   * Implementação atual: mock local.
   */
  async getSeries(): Promise<Category[]> {
    // Implementação atual com mock
    await delay(200);
    return mockCategories
      .map(c => ({ ...c, items: c.items.filter(i => i.type === 'series') }))
      .filter(c => c.items.length > 0);

    // Futuramente:
    // const response = await apiClient.get<Category[]>(API_ENDPOINTS.series);
    // return response;
  },

  /**
   * Retorna canais ao vivo.
   * Implementação atual: mock local.
   */
  async getChannels(): Promise<MediaItem[]> {
    // Implementação atual com mock
    await delay(200);
    return mockChannels;

    // Futuramente:
    // const response = await apiClient.get<MediaItem[]>(API_ENDPOINTS.channels);
    // return response;
  },

  /**
   * Busca por título, descrição ou gênero.
   * Implementação atual: mock local.
   */
  async search(query: string): Promise<MediaItem[]> {
    // Implementação atual com mock
    await delay(200);
    const all = [...mockMovies, ...mockSeries, ...mockChannels];
    const q = query.toLowerCase();
    return all.filter(item =>
      item.title.toLowerCase().includes(q) ||
      item.description?.toLowerCase().includes(q) ||
      item.genres?.some(g => g.toLowerCase().includes(q))
    );

    // Futuramente:
    // const response = await apiClient.get<MediaItem[]>(`${API_ENDPOINTS.search}?q=${encodeURIComponent(query)}`);
    // return response;
  },

  /**
   * Retorna todas as categorias.
   * Implementação atual: mock local.
   */
  async getCategories(): Promise<Category[]> {
    // Implementação atual com mock
    await delay(200);
    return mockCategories;

    // Futuramente:
    // const response = await apiClient.get<Category[]>(API_ENDPOINTS.categories);
    // return response;
  },
};
