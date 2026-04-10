import { describe, it, expect, vi, beforeEach } from 'vitest';
import { catalogService } from '@/services/catalog';

describe('Catalog Service', () => {
  beforeEach(() => {
    localStorage.setItem('stream_auth_session', JSON.stringify({ token: 'test' }));
  });

  it('getMoviesAll envia query params corretamente', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    await catalogService.getMoviesAll({ page: 1, size: 20, sort: 'name', order: 'asc', group: 'Ação' });
    const url = new URL(fetchSpy.mock.calls[0][0] as string);
    expect(url.pathname).toBe('/api/movies/all');
    expect(url.searchParams.get('page')).toBe('1');
    expect(url.searchParams.get('size')).toBe('20');
    expect(url.searchParams.get('sort')).toBe('name');
    expect(url.searchParams.get('order')).toBe('asc');
    expect(url.searchParams.get('group')).toBe('Ação');
    fetchSpy.mockRestore();
  });

  it('getSeriesAll envia query params corretamente', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    await catalogService.getSeriesAll({ page: 2, size: 10, sort: 'popularity', order: 'desc', group: 'Drama' });
    const url = new URL(fetchSpy.mock.calls[0][0] as string);
    expect(url.pathname).toBe('/api/series/all');
    expect(url.searchParams.get('page')).toBe('2');
    expect(url.searchParams.get('size')).toBe('10');
    expect(url.searchParams.get('sort')).toBe('popularity');
    expect(url.searchParams.get('order')).toBe('desc');
    expect(url.searchParams.get('group')).toBe('Drama');
    fetchSpy.mockRestore();
  });

  it('getLiveChannels envia category corretamente', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    await catalogService.getLiveChannels({ category: 'Esportes', page: 0, size: 50 });
    const url = new URL(fetchSpy.mock.calls[0][0] as string);
    expect(url.pathname).toBe('/api/live');
    expect(url.searchParams.get('category')).toBe('Esportes');
    fetchSpy.mockRestore();
  });

  it('globalSearch retorna vazio para query < 2', async () => {
    const result = await catalogService.globalSearch('a');
    expect(result.totalResults).toBe(0);
  });

  it('addFavorite chama POST /api/favorites/{id}', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    await catalogService.addFavorite('ch1');
    const [url, options] = fetchSpy.mock.calls[0];
    expect(new URL(url as string).pathname).toBe('/api/favorites/ch1');
    expect((options as RequestInit).method).toBe('POST');
    fetchSpy.mockRestore();
  });

  it('removeFavorite chama DELETE /api/favorites/{id}', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    await catalogService.removeFavorite('ch2');
    const [url, options] = fetchSpy.mock.calls[0];
    expect(new URL(url as string).pathname).toBe('/api/favorites/ch2');
    expect((options as RequestInit).method).toBe('DELETE');
    fetchSpy.mockRestore();
  });

  it('addToHistory chama POST /api/history/{id}', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    await catalogService.addToHistory('ch3');
    const [url, options] = fetchSpy.mock.calls[0];
    expect(new URL(url as string).pathname).toBe('/api/history/ch3');
    expect((options as RequestInit).method).toBe('POST');
    fetchSpy.mockRestore();
  });

  it('clearHistory chama DELETE /api/history', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch');
    await catalogService.clearHistory();
    const [url, options] = fetchSpy.mock.calls[0];
    expect(new URL(url as string).pathname).toBe('/api/history');
    expect((options as RequestInit).method).toBe('DELETE');
    fetchSpy.mockRestore();
  });
});
