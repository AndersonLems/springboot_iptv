import type {
  Category,
  MediaItem,
  HomeFeed,
  Channel,
  CategoryGroup,
  EnrichedMovie,
  EnrichedSeries,
  GlobalSearchResult,
  LiveChannel,
  PaginatedResponse,
  SortField,
  SortOrder,
  WatchHistoryItem,
  EpgProgram,
} from "@/types/media";
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
const MOVIES_BY_CATEGORY_TIMEOUT_MS = 60_000;
const MAX_ITEMS_PER_CATEGORY = 40;

function toQueryString(
  params: Record<string, string | number | undefined | null>,
): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    search.set(key, String(value));
  });
  const query = search.toString();
  return query ? `?${query}` : "";
}

type MovieSubcategory = "Acao" | "Aventura" | "Drama" | "Fantasia" | "Ficção Científica" | "Terror" | "Suspense" | "Romance" | "Comédia" | "Animação" | "Documentário"; 

interface TrendingMovieApiResponse {
  tmdbId: number;
  title: string;
  overview?: string;
  releaseDate?: string;
  voteAverage?: number;
  posterPath?: string;
  backdropPath?: string;
  available?: boolean;
  streams?: Array<{
    name?: string;
    streamUrl?: string;
    groupTitle?: string;
    logoUrl?: string;
    quality?: string;
  }>;
}

interface EnrichedSeriesApiResponse {
  tmdbId?: number | null;
  name: string;
  overview?: string | null;
  firstAirDate?: string | null;
  voteAverage?: number;
  posterPath?: string | null;
  genreIds?: number[];
  available?: boolean;
  streams?: Array<{
    name?: string;
    streamUrl?: string;
    groupTitle?: string;
    logoUrl?: string;
    quality?: string;
    season?: number | null;
    episode?: number | null;
  }>;
}

interface PlaylistChannelApiResponse {
  id?: string;
  name: string;
  logoUrl?: string;
  groupTitle?: string;
  streamUrl: string;
  duration?: number;
}

function toMediaItemFromPlaylist(channel: PlaylistChannelApiResponse): MediaItem {
  return {
    id: channel.id ?? `pl-${channel.streamUrl}`,
    title: channel.name,
    type: "movie",
    cover: channel.logoUrl || mockMovies[0].cover,
    streamUrl: channel.streamUrl,
    streamOptions: [
      {
        name: channel.name,
        streamUrl: channel.streamUrl,
        groupTitle: channel.groupTitle,
        logoUrl: channel.logoUrl,
      },
    ],
    genres: channel.groupTitle ? [channel.groupTitle] : undefined,
  };
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
    available: movie.available,
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

function toMediaItemFromSeries(series: EnrichedSeriesApiResponse): MediaItem {
  const year = series.firstAirDate
    ? Number(series.firstAirDate.slice(0, 4))
    : undefined;
  const rating =
    typeof series.voteAverage === "number"
      ? series.voteAverage.toFixed(1)
      : undefined;

  const streamOptions = (series.streams ?? [])
    .filter((stream) => !!stream.streamUrl)
    .map((stream) => ({
      name: stream.name,
      streamUrl: stream.streamUrl!,
      groupTitle: stream.groupTitle,
      logoUrl: stream.logoUrl,
      quality: stream.quality,
      season: stream.season ?? undefined,
      episode: stream.episode ?? undefined,
    }));

  return {
    id: series.tmdbId ? `tmdb-series-${series.tmdbId}` : `pl-series-${series.name}`,
    title: series.name,
    type: "series",
    available: series.available,
    cover: series.posterPath
      ? `${TMDB_IMAGE_BASE_URL}${series.posterPath}`
      : streamOptions[0]?.logoUrl || mockSeries[0].cover,
    streamUrl: streamOptions[0]?.streamUrl ?? mockSeries[0].streamUrl,
    streamOptions,
    description: series.overview ?? undefined,
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

function replaceCategoryByName(
  categories: Category[],
  name: string,
  items: MediaItem[],
): Category[] {
  return categories.map((category) =>
    category.name === name
      ? { ...category, items: items.length ? items : category.items }
      : category,
  );
}

function limitItems(items: MediaItem[], max = MAX_ITEMS_PER_CATEGORY): MediaItem[] {
  return items.length > max ? items.slice(0, max) : items;
}

export const catalogService = {
  /**
   * Retorna o feed da página inicial (hero + categorias).
   * Implementação atual: mock local.
   */
  async getHomeFeed(): Promise<HomeFeed> {
    await delay(300);
    try {
      const [trendingItems, actionAdventureItems, dramaItems, fantasyScifiItems, terrorSuspenseItems, comedyRomanceItems, seriesTrendingItems] = await Promise.all([
        this.getMoviesTrending(),
        this.getMoviesBySubcategory(["Acao", "Aventura"]),
        this.getMoviesBySubcategory("Drama"),
        this.getFantasyScifiMovies(),
        this.getTerrorSuspenseMovies(),
        this.getComedyromanceMovies(),
        this.getSeriesTrending(),
      ]);

      const withTrending = replaceTrendingCategory(
        mockHomeFeed.categories,
        trendingItems,
      );

      const heroItem = trendingItems[0] ?? actionAdventureItems[0] ?? mockHomeFeed.hero.item;

      return {
        ...mockHomeFeed,
        hero: {
          ...mockHomeFeed.hero,
          item: heroItem,
        },
        categories: replaceCategoryByName(
        replaceCategoryByName(
          replaceCategoryByName(
            replaceCategoryByName(
              replaceCategoryByName(
                replaceCategoryByName(withTrending, "Ação & Aventura", actionAdventureItems),
                "Drama",
                dramaItems
              ),
              "Ficção Científica & Fantasia",
              fantasyScifiItems
            ),
            "Terror & Suspense",
            terrorSuspenseItems
          ),
          "Romance & Comédia",
          comedyRomanceItems
        ),
        "Séries Populares",
        seriesTrendingItems
      ),
      };
    } catch {
      return mockHomeFeed;
    }
  },

  /**
   * Retorna categorias filtradas por filmes.
   * Implementação atual: mock local.
   */
  async getMovies(): Promise<Category[]> {
    await delay(200);
    const movieCategories = mockCategories
      .map((c) => ({ ...c, items: c.items.filter((i) => i.type === "movie") }))
      .filter((c) => c.items.length > 0);

    try {
      const [trendingItems, actionAdventureItems, dramaItems, fantasyScifiItems, terrorSuspenseItems, comedyRomanceItems] = await Promise.all([
        this.getMoviesTrending(),
        this.getMoviesBySubcategory(["Acao", "Aventura"]),
        this.getMoviesBySubcategory("Drama"),
        this.getFantasyScifiMovies(),
        this.getTerrorSuspenseMovies(),
        this.getComedyromanceMovies(),
      ]);

      const withTrending = replaceTrendingCategory(movieCategories, trendingItems);

      return replaceCategoryByName(
      replaceCategoryByName(
        replaceCategoryByName(
        replaceCategoryByName(
          replaceCategoryByName(withTrending, "Ação & Aventura", actionAdventureItems),
          "Drama",
          dramaItems
        ),
        "Ficção Científica & Fantasia",
        fantasyScifiItems,
      ),
      "Terror & Suspense",
      terrorSuspenseItems
    ),
    "Romance & Comédia",
    comedyRomanceItems
    
      );
    } catch {
      return movieCategories;
    }
  },

  async getMoviesTrending(): Promise<MediaItem[]> {
    const response = await apiClient.get<TrendingMovieApiResponse[]>(
      API_ENDPOINTS.moviesTrending,
      { timeout: TRENDING_TIMEOUT_MS },
    );
    return response
      .filter((movie) => movie.available)
      .map(toMediaItemFromTrending);
  },

  /**
   * Retorna categorias filtradas por séries.
   * Implementação atual: mock local.
   */
  async getSeries(): Promise<Category[]> {
    await delay(200);
    const fallback = mockCategories
      .map((c) => ({ ...c, items: c.items.filter((i) => i.type === "series") }))
      .filter((c) => c.items.length > 0);

    try {
      const [trending, popular, topRated, playlist] = await Promise.all([
        this.getSeriesTrending(),
        this.getSeriesPopular(),
        this.getSeriesTopRated(),
        this.getSeriesFromPlaylist(),
      ]);

      const categories: Category[] = [
        { name: "Em Alta", items: trending },
        { name: "Populares", items: popular },
        { name: "Bem Avaliadas", items: topRated },
        { name: "Da Playlist", items: playlist },
      ].filter((category) => category.items.length > 0);

      return categories.length ? categories : fallback;
    } catch {
      return fallback;
    }
  },

  async getSeriesTrending(): Promise<MediaItem[]> {
    const response = await apiClient.get<EnrichedSeriesApiResponse[]>(
      API_ENDPOINTS.seriesTrending,
      { timeout: TRENDING_TIMEOUT_MS },
    );
    return limitItems(
      response.filter((series) => series.available).map(toMediaItemFromSeries),
    );
  },

  async getSeriesTopRated(): Promise<MediaItem[]> {
    const response = await apiClient.get<EnrichedSeriesApiResponse[]>(
      API_ENDPOINTS.seriesTopRated,
    );
    return limitItems(
      response.filter((series) => series.available).map(toMediaItemFromSeries),
    );
  },

  async getSeriesPopular(): Promise<MediaItem[]> {
    const response = await apiClient.get<EnrichedSeriesApiResponse[]>(
      API_ENDPOINTS.seriesPopular,
    );
    return limitItems(
      response.filter((series) => series.available).map(toMediaItemFromSeries),
    );
  },

  async getSeriesFromPlaylist(): Promise<MediaItem[]> {
    const response = await apiClient.get<EnrichedSeriesApiResponse[]>(
      API_ENDPOINTS.seriesPlaylist,
    );
    return limitItems(response.map(toMediaItemFromSeries));
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

  

  async getMoviesBySubcategory(
    sub: MovieSubcategory | MovieSubcategory[],
  ): Promise<MediaItem[]> {
    const subs = Array.isArray(sub) ? sub : [sub];
    const results = await Promise.all(
      subs.map(async (item) => {
        const path = `/api/playlist/categories/${encodeURIComponent("Filmes")}/${encodeURIComponent(item)}`;
        const response = await apiClient.get<PlaylistChannelApiResponse[]>(path, {
          timeout: MOVIES_BY_CATEGORY_TIMEOUT_MS,
        });
        return response.map(toMediaItemFromPlaylist);
      }),
    );

    const unique = new Map<string, MediaItem>();
    results.flat().forEach((item) => {
      unique.set(item.streamUrl, item);
    });

    return limitItems([...unique.values()]);
  },


  async getActionAdventureMovies(): Promise<MediaItem[]> {
    const [acao, aventura] = await Promise.all([
      this.getMoviesBySubcategory("Acao"),
      this.getMoviesBySubcategory("Aventura"),
    ]);


    const unique = new Map<string, MediaItem>();
    [...acao, ...aventura].forEach((item) => {
      unique.set(item.streamUrl, item);
    });

    return [...unique.values()];
  },

    async getDramaMovies(): Promise<MediaItem[]>{
    const [drama] = await Promise.all([
      this.getMoviesBySubcategory("Drama"),
    ]);
    const unique = new Map<string, MediaItem>();
    [...drama].forEach((item) => {
      unique.set(item.streamUrl, item);
    });

    return [...unique.values()];
  },

  async getFantasyScifiMovies(): Promise<MediaItem[]> {
    const [fantasy, scifi] = await Promise.all([
      this.getMoviesBySubcategory("Fantasia"),
      this.getMoviesBySubcategory("Ficção Científica"),
    ]);
    const unique = new Map<string, MediaItem>();
    [...fantasy, ...scifi].forEach((item) => {
      unique.set(item.streamUrl, item);
    });

    return [...unique.values()];
  },

  async getTerrorSuspenseMovies(): Promise<MediaItem[]> {
    const [terror, suspense] = await Promise.all([
      this.getMoviesBySubcategory("Terror"),
      this.getMoviesBySubcategory("Suspense"),
    ]);
    const unique = new Map<string, MediaItem>();
    [...terror, ...suspense].forEach((item) => {
      unique.set(item.streamUrl, item);
    });

    return [...unique.values()];
  },
  async getComedyromanceMovies(): Promise<MediaItem[]> {
    const [comedy, romance] = await Promise.all([
      this.getMoviesBySubcategory("Comédia"),
      this.getMoviesBySubcategory("Romance"),
    ]);
    const unique = new Map<string, MediaItem>();
    [...comedy, ...romance].forEach((item) => {
      unique.set(item.streamUrl, item);
    });

    return [...unique.values()];
  },

  async getMoviesAll(params: {
    page: number;
    size: number;
    sort: SortField;
    order: SortOrder;
    group?: string;
  }): Promise<PaginatedResponse<EnrichedMovie>> {
    const query = toQueryString({
      page: params.page,
      size: params.size,
      sort: params.sort,
      order: params.order,
      group: params.group,
    });
    return apiClient.get<PaginatedResponse<EnrichedMovie>>(
      `${API_ENDPOINTS.movies.all}${query}`,
    );
  },

  async getSeriesAll(params: {
    page: number;
    size: number;
    sort: SortField;
    order: SortOrder;
    group?: string;
  }): Promise<PaginatedResponse<EnrichedSeries>> {
    const query = toQueryString({
      page: params.page,
      size: params.size,
      sort: params.sort,
      order: params.order,
      group: params.group,
    });
    return apiClient.get<PaginatedResponse<EnrichedSeries>>(
      `${API_ENDPOINTS.series.all}${query}`,
    );
  },

  async getLiveChannels(params: {
    category?: string;
    page: number;
    size: number;
  }): Promise<PaginatedResponse<LiveChannel>> {
    const query = toQueryString({
      category: params.category,
      page: params.page,
      size: params.size,
    });
    return apiClient.get<PaginatedResponse<LiveChannel>>(
      `${API_ENDPOINTS.live.list}${query}`,
    );
  },

  async globalSearch(
    query: string,
    types = "movies,series,live",
    signal?: AbortSignal,
  ): Promise<GlobalSearchResult> {
    if (query.length < 2) {
      return {
        query,
        movies: [],
        series: [],
        live: [],
        totalResults: 0,
      };
    }
    const qs = toQueryString({ q: query, types });
    return apiClient.get<GlobalSearchResult>(
      `${API_ENDPOINTS.search.global}${qs}`,
      { signal, timeout: 0 },
    );
  },

  async getFavorites(): Promise<Channel[]> {
    return apiClient.get<Channel[]>(API_ENDPOINTS.favorites.list);
  },

  async addFavorite(channelId: string): Promise<void> {
    await apiClient.post<void>(API_ENDPOINTS.favorites.item(channelId));
  },

  async removeFavorite(channelId: string): Promise<void> {
    await apiClient.delete<void>(API_ENDPOINTS.favorites.item(channelId));
  },

  async getHistory(): Promise<WatchHistoryItem[]> {
    return apiClient.get<WatchHistoryItem[]>(API_ENDPOINTS.history.list);
  },

  async addToHistory(channelId: string): Promise<void> {
    await apiClient.post<void>(API_ENDPOINTS.history.item(channelId));
  },

  async removeFromHistory(channelId: string): Promise<void> {
    await apiClient.delete<void>(API_ENDPOINTS.history.item(channelId));
  },

  async clearHistory(): Promise<void> {
    await apiClient.delete<void>(API_ENDPOINTS.history.list);
  },

  async getEpgNow(channelId: string): Promise<EpgProgram | null> {
    try {
      return await apiClient.get<EpgProgram>(API_ENDPOINTS.epg.now(channelId));
    } catch {
      return null;
    }
  },
};
