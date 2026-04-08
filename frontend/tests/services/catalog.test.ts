import { describe, it, expect } from 'vitest';
import { catalogService } from '@/services/catalog';

describe('Catalog Service', () => {
  it('getHomeFeed deve retornar hero e categories', async () => {
    const feed = await catalogService.getHomeFeed();
    expect(feed).toBeDefined();
    expect(feed.hero).toBeDefined();
    expect(feed.hero.item).toBeDefined();
    expect(feed.hero.item.title).toBeDefined();
    expect(feed.categories).toBeInstanceOf(Array);
    expect(feed.categories.length).toBeGreaterThan(0);
  });

  it('getMovies deve retornar categorias com apenas filmes', async () => {
    const categories = await catalogService.getMovies();
    expect(categories).toBeInstanceOf(Array);
    categories.forEach(cat => {
      expect(cat.name).toBeDefined();
      expect(cat.items).toBeInstanceOf(Array);
      cat.items.forEach(item => {
        expect(item.type).toBe('movie');
      });
    });
  });

  it('getSeries deve retornar categorias com apenas séries', async () => {
    const categories = await catalogService.getSeries();
    expect(categories).toBeInstanceOf(Array);
    categories.forEach(cat => {
      cat.items.forEach(item => {
        expect(item.type).toBe('series');
      });
    });
  });

  it('getChannels deve retornar canais', async () => {
    const channels = await catalogService.getChannels();
    expect(channels).toBeInstanceOf(Array);
    expect(channels.length).toBeGreaterThan(0);
    channels.forEach(ch => {
      expect(ch.type).toBe('channel');
    });
  });

  it('search deve filtrar por título', async () => {
    const results = await catalogService.search('Horizonte');
    expect(results).toBeInstanceOf(Array);
    expect(results.length).toBeGreaterThan(0);
    expect(results[0].title).toContain('Horizonte');
  });

  it('search deve retornar vazio para query sem resultados', async () => {
    const results = await catalogService.search('xyznonexistent999');
    expect(results).toBeInstanceOf(Array);
    expect(results.length).toBe(0);
  });

  it('getCategories deve retornar categorias', async () => {
    const categories = await catalogService.getCategories();
    expect(categories).toBeInstanceOf(Array);
    expect(categories.length).toBeGreaterThan(0);
    categories.forEach(cat => {
      expect(cat.name).toBeDefined();
      expect(cat.items).toBeInstanceOf(Array);
    });
  });
});
