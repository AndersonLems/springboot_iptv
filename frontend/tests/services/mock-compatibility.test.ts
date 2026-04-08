import { describe, it, expect } from 'vitest';
import { mockMovies, mockSeries, mockChannels, mockCategories, mockHomeFeed } from '@/data/mocks';

describe('Mock Data Shape Compatibility', () => {
  describe('MediaItem shape', () => {
    const allItems = [...mockMovies, ...mockSeries, ...mockChannels];

    it('todos os itens devem ter campos obrigatórios', () => {
      allItems.forEach(item => {
        expect(item.id).toBeDefined();
        expect(typeof item.title).toBe('string');
        expect(typeof item.cover).toBe('string');
        expect(typeof item.streamUrl).toBe('string');
        expect(['movie', 'series', 'channel']).toContain(item.type);
      });
    });

    it('filmes devem ter campos específicos', () => {
      mockMovies.forEach(movie => {
        expect(movie.type).toBe('movie');
        expect(movie.cover).toMatch(/^https?:\/\//);
        expect(movie.streamUrl).toMatch(/^https?:\/\//);
      });
    });

    it('séries devem ter campos específicos', () => {
      mockSeries.forEach(series => {
        expect(series.type).toBe('series');
        expect(series.seasons).toBeDefined();
        expect(series.episodes).toBeDefined();
      });
    });

    it('canais devem ter type channel', () => {
      mockChannels.forEach(ch => {
        expect(ch.type).toBe('channel');
      });
    });
  });

  describe('Category shape', () => {
    it('categorias devem ter name e items array', () => {
      mockCategories.forEach(cat => {
        expect(typeof cat.name).toBe('string');
        expect(cat.items).toBeInstanceOf(Array);
        expect(cat.items.length).toBeGreaterThan(0);
      });
    });
  });

  describe('HomeFeed shape', () => {
    it('deve ter hero com item e categorias', () => {
      expect(mockHomeFeed.hero).toBeDefined();
      expect(mockHomeFeed.hero.item).toBeDefined();
      expect(mockHomeFeed.hero.item.title).toBeDefined();
      expect(mockHomeFeed.hero.item.streamUrl).toBeDefined();
      expect(mockHomeFeed.categories).toBeInstanceOf(Array);
      expect(mockHomeFeed.categories.length).toBeGreaterThan(0);
    });

    it('hero tagline deve ser string quando definida', () => {
      if (mockHomeFeed.hero.tagline) {
        expect(typeof mockHomeFeed.hero.tagline).toBe('string');
      }
    });
  });
});
