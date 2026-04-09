import type { Category, MediaItem, HomeFeed } from "@/types/media";
import {
  mockMovies,
  mockSeries,
  mockChannels,
  mockCategories,
  mockHomeFeed,
} from "@/data/mocks";
import { apiClient } from "./api/client";
import { API_ENDPOINTS } from "./api/endpoints";

const delay = (ms: number) => new Promise((r) => setTimeout(r, ms));
const TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w780";
const TRENDING_TIMEOUT_MS = import.meta.env.MODE === "test" ? 1000 : 0;

interface TrendingMovieApiResponse {
  tmdbId: number;
  title: string;
  overview?: string;
  releaseDate?: string;
  voteAverage?: number;
  posterPath?: string;
  backdropPath?: string;
  streams?: Array<{
    name?: string;
    streamUrl?: string;
    groupTitle?: string;
    logoUrl?: string;
    quality?: string;
  }>;
}

function toMediaItemFromTrending(movie: TrendingMovieApiResponse): MediaItem {
  const year = movie.releaseDate
    ? Number(movie.releaseDate.slice(0, 4))
    : undefined;
  const rating =
    typeof movie.voteAverage === "number"
      ? movie.voteAverage.toFixed(1)
      : undefined;

  const streamOptions = (movie.streams ?? [])
    .filter((stream) => !!stream.streamUrl)
    .map((stream) => ({
      name: stream.name,
      streamUrl: stream.streamUrl!,
      groupTitle: stream.groupTitle,
      logoUrl: stream.logoUrl,
      quality: stream.quality,
    }));

  return {
    id: `tmdb-${movie.tmdbId}`,
    title: movie.title,
    type: "movie",
    cover: movie.posterPath
      ? `${TMDB_IMAGE_BASE_URL}${movie.posterPath}`
      : mockMovies[0].cover,
    backdrop: movie.backdropPath
      ? `${TMDB_IMAGE_BASE_URL}${movie.backdropPath}`
      : undefined,
    streamUrl: streamOptions[0]?.streamUrl ?? mockMovies[0].streamUrl,
    streamOptions,
    description: movie.overview,
    year,
    rating,
  };
}

function replaceTrendingCategory(
  categories: Category[],
  trendingItems: MediaItem[],
): Category[] {
  return categories.map((category) =>
    category.name === "Em Alta"
      ? {
          ...category,
          items: trendingItems.length ? trendingItems : category.items,
        }
      : category,
  );
}

export const catalogService = {
  /**
   * Retorna o feed da página inicial (hero + categorias).
   * Implementação atual: mock local.
   */
  async getHomeFeed(): Promise<HomeFeed> {
    // Implementação atual com mock
    await delay(300);
    try {
      const trendingItems = await this.getMoviesTrending();
      return {
        ...mockHomeFeed,
        categories: replaceTrendingCategory(
          mockHomeFeed.categories,
          trendingItems,
        ),
      };
    } catch {
      return mockHomeFeed;
    }

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
    const movieCategories = mockCategories
      .map((c) => ({ ...c, items: c.items.filter((i) => i.type === "movie") }))
      .filter((c) => c.items.length > 0);

    try {
      const trendingItems = await this.getMoviesTrending();
      return replaceTrendingCategory(movieCategories, trendingItems);
    } catch {
      return movieCategories;
    }

    // Futuramente:
    // const response = await apiClient.get<Category[]>(API_ENDPOINTS.movies);
    // return response;
  },

  async getMoviesTrending(): Promise<MediaItem[]> {
    const response = await apiClient.get<TrendingMovieApiResponse[]>(
      API_ENDPOINTS.moviesTrending,
      { timeout: TRENDING_TIMEOUT_MS },
    );
    return response.map(toMediaItemFromTrending);
  },

  /**
   * Retorna categorias filtradas por séries.
   * Implementação atual: mock local.
   */
  async getSeries(): Promise<Category[]> {
    // Implementação atual com mock
    await delay(200);
    return mockCategories
      .map((c) => ({ ...c, items: c.items.filter((i) => i.type === "series") }))
      .filter((c) => c.items.length > 0);

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
    return all.filter(
      (item) =>
        item.title.toLowerCase().includes(q) ||
        item.description?.toLowerCase().includes(q) ||
        item.genres?.some((g) => g.toLowerCase().includes(q)),
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
