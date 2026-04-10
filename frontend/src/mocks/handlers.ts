import { delay, http, HttpResponse } from "msw";
import type {
  CategoryGroup,
  Channel,
  EnrichedMovie,
  EnrichedSeries,
  EpgProgram,
  GlobalSearchResult,
  HealthStatus,
  LiveChannel,
  PaginatedResponse,
  WatchHistoryItem,
} from "@/types/media";

const movies: EnrichedMovie[] = [
  {
    tmdbId: 1,
    title: "Aventura no Espaço",
    overview: "Uma missão inesperada pelo cosmos.",
    releaseDate: "2021-08-12",
    voteAverage: 7.8,
    voteCount: 1200,
    popularity: 88.4,
    posterPath: "/poster1.jpg",
    backdropPath: "/backdrop1.jpg",
    genreIds: [12, 878],
    streams: [
      {
        name: "Servidor 1",
        streamUrl: "https://stream/1",
        groupTitle: "Filmes | Ação",
        logoUrl: "https://img/logo1.png",
        quality: "UHD_4K",
      },
    ],
    available: true,
  },
  {
    tmdbId: 2,
    title: "Romance na Chuva",
    overview: "Dois destinos que se encontram.",
    releaseDate: "2019-02-02",
    voteAverage: 6.1,
    voteCount: 540,
    popularity: 62.1,
    posterPath: "/poster2.jpg",
    backdropPath: "/backdrop2.jpg",
    genreIds: [10749],
    streams: [
      {
        name: "Servidor 2",
        streamUrl: "https://stream/2",
        groupTitle: "Filmes | Romance",
        logoUrl: "https://img/logo2.png",
        quality: "DUBLADO",
      },
    ],
    available: true,
  },
];

const series: EnrichedSeries[] = [
  {
    tmdbId: 10,
    name: "Cidade Invisível",
    overview: "Mistérios na metrópole.",
    firstAirDate: "2020-05-10",
    voteAverage: 7.2,
    voteCount: 320,
    popularity: 71.2,
    posterPath: "/serie1.jpg",
    genreIds: [18, 9648],
    streams: [
      {
        name: "Servidor S1",
        streamUrl: "https://stream/s1",
        groupTitle: "Séries | Drama",
        logoUrl: "https://img/serie1.png",
        quality: "HDR",
      },
    ],
    available: true,
  },
  {
    tmdbId: 11,
    name: "Horizonte Zero",
    overview: "Sobrevivência e desafios.",
    firstAirDate: "2022-09-21",
    voteAverage: 8.3,
    voteCount: 200,
    popularity: 79.5,
    posterPath: "/serie2.jpg",
    genreIds: [10759],
    streams: [
      {
        name: "Servidor S2",
        streamUrl: "https://stream/s2",
        groupTitle: "Séries | Aventura",
        logoUrl: "https://img/serie2.png",
        quality: "DUBLADO",
      },
    ],
    available: true,
  },
];

const liveChannels: LiveChannel[] = [
  {
    id: "live-1",
    name: "News 24",
    logoUrl: "https://img/live1.png",
    groupTitle: "Canais | Notícias",
    streamUrl: "https://live/1",
    duration: 0,
    isLive: true,
    category: "Notícias",
  },
  {
    id: "live-2",
    name: "Sports Plus",
    logoUrl: "https://img/live2.png",
    groupTitle: "Canais | Esportes",
    streamUrl: "https://live/2",
    duration: 0,
    isLive: true,
    category: "Esportes",
  },
];

const playlistChannels: Channel[] = [
  {
    id: "pl-1",
    name: "Canal Ação",
    logoUrl: "https://img/pl1.png",
    groupTitle: "Filmes | Ação",
    streamUrl: "https://pl/1",
    duration: 7200,
  },
];

const categoryGroups: CategoryGroup[] = [
  {
    parent: "Canais | Notícias",
    subcategories: ["Notícias"],
    totalChannels: 12,
  },
  {
    parent: "Canais | Esportes",
    subcategories: ["Esportes"],
    totalChannels: 8,
  },
  {
    parent: "Filmes",
    subcategories: ["Ação", "Romance"],
    totalChannels: 120,
  },
];

let favoritesStore: Channel[] = [playlistChannels[0]];
let historyStore: WatchHistoryItem[] = [
  {
    channel: playlistChannels[0],
    watchedAt: new Date(Date.now() - 1000 * 60 * 60).toISOString(),
  },
];

function paginate<T>(items: T[], page: number, size: number) {
  const start = page * size;
  const end = start + size;
  return {
    content: items.slice(start, end),
    page,
    size,
    totalElements: items.length,
    totalPages: Math.ceil(items.length / size),
  };
}

function requireAuth(request: Request) {
  const auth = request.headers.get("Authorization");
  if (!auth) {
    return HttpResponse.json({ message: "Unauthorized" }, { status: 401 });
  }
  return null;
}

export const handlers = [
  http.post("*/api/auth/login", async ({ request }) => {
    const body = (await request.json()) as { username?: string; password?: string };
    if (!body?.username || !body?.password) {
      return HttpResponse.json({ message: "Unauthorized" }, { status: 401 });
    }
    return HttpResponse.json({ token: `token-${body.username}`, username: body.username });
  }),

  http.get("*/api/movies/trending", () => HttpResponse.json(movies)),
  http.get("*/api/movies/popular", () => HttpResponse.json(movies)),
  http.get("*/api/movies/top-rated", () => HttpResponse.json(movies)),
  http.get("*/api/series/trending", () => HttpResponse.json(series)),
  http.get("*/api/series/top-rated", () => HttpResponse.json(series)),
  http.get("*/api/series/popular", () => HttpResponse.json(series)),
  http.get("*/api/series/playlist", () => HttpResponse.json(series)),

  http.get("*/api/movies/all", async ({ request }) => {
    await delay(300);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page") ?? 0);
    const size = Number(url.searchParams.get("size") ?? 50);
    const result = paginate(movies, page, size);
    const response: PaginatedResponse<EnrichedMovie> = {
      ...result,
      sort: (url.searchParams.get("sort") as PaginatedResponse<EnrichedMovie>["sort"]) || "name",
      order: (url.searchParams.get("order") as PaginatedResponse<EnrichedMovie>["order"]) || "asc",
    };
    return HttpResponse.json(response);
  }),

  http.get("*/api/series/all", async ({ request }) => {
    await delay(300);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page") ?? 0);
    const size = Number(url.searchParams.get("size") ?? 50);
    const result = paginate(series, page, size);
    const response: PaginatedResponse<EnrichedSeries> = {
      ...result,
      sort: (url.searchParams.get("sort") as PaginatedResponse<EnrichedSeries>["sort"]) || "name",
      order: (url.searchParams.get("order") as PaginatedResponse<EnrichedSeries>["order"]) || "asc",
    };
    return HttpResponse.json(response);
  }),

  http.get("*/api/live", async ({ request }) => {
    await delay(300);
    const url = new URL(request.url);
    const page = Number(url.searchParams.get("page") ?? 0);
    const size = Number(url.searchParams.get("size") ?? 50);
    const category = url.searchParams.get("category");
    const list = category
      ? liveChannels.filter((ch) => ch.category === category)
      : liveChannels;
    const result = paginate(list, page, size);
    const response: PaginatedResponse<LiveChannel> = {
      ...result,
      sort: "name",
      order: "asc",
    };
    return HttpResponse.json(response);
  }),

  http.get("*/api/search", ({ request }) => {
    const url = new URL(request.url);
    const q = (url.searchParams.get("q") ?? "").toLowerCase();
    const matches = (item: { name?: string; title?: string }) =>
      (item.name ?? item.title ?? "").toLowerCase().includes(q);
    const response: GlobalSearchResult = {
      query: q,
      movies: playlistChannels.filter(matches),
      series: playlistChannels.filter(matches),
      live: playlistChannels.filter(matches),
      totalResults: 3,
    };
    return HttpResponse.json(response);
  }),

  http.get(
    "*/api/playlist/categories",
    () => HttpResponse.json(categoryGroups),
  ),
  http.get(
    "*/api/playlist/categories/:parent",
    () => HttpResponse.json(playlistChannels),
  ),
  http.get(
    "*/api/playlist/categories/:parent/search",
    () => HttpResponse.json(playlistChannels),
  ),
  http.get(
    "*/api/playlist/categories/:parent/:sub",
    () => HttpResponse.json(playlistChannels),
  ),

  http.get("*/api/favorites", ({ request }) => {
    const unauthorized = requireAuth(request);
    if (unauthorized) return unauthorized;
    return HttpResponse.json(favoritesStore);
  }),
  http.post("*/api/favorites/:channelId", ({ request, params }) => {
    const unauthorized = requireAuth(request);
    if (unauthorized) return unauthorized;
    const channelId = params.channelId as string;
    const exists = favoritesStore.find((item) => item.id === channelId);
    if (!exists) {
      favoritesStore = [...favoritesStore, { ...playlistChannels[0], id: channelId }];
    }
    return HttpResponse.json(null);
  }),
  http.delete("*/api/favorites/:channelId", ({ request, params }) => {
    const unauthorized = requireAuth(request);
    if (unauthorized) return unauthorized;
    const channelId = params.channelId as string;
    favoritesStore = favoritesStore.filter((item) => item.id !== channelId);
    return HttpResponse.json(null);
  }),

  http.get("*/api/history", ({ request }) => {
    const unauthorized = requireAuth(request);
    if (unauthorized) return unauthorized;
    return HttpResponse.json(historyStore);
  }),
  http.post("*/api/history/:channelId", ({ request, params }) => {
    const unauthorized = requireAuth(request);
    if (unauthorized) return unauthorized;
    const channelId = params.channelId as string;
    historyStore = [
      { channel: { ...playlistChannels[0], id: channelId }, watchedAt: new Date().toISOString() },
      ...historyStore,
    ];
    return HttpResponse.json(null);
  }),
  http.delete("*/api/history", ({ request }) => {
    const unauthorized = requireAuth(request);
    if (unauthorized) return unauthorized;
    historyStore = [];
    return HttpResponse.json(null);
  }),
  http.delete("*/api/history/:channelId", ({ request, params }) => {
    const unauthorized = requireAuth(request);
    if (unauthorized) return unauthorized;
    const channelId = params.channelId as string;
    historyStore = historyStore.filter((item) => item.channel.id !== channelId);
    return HttpResponse.json(null);
  }),

  http.get("*/api/epg/channel/:channelId/now", ({ params }) => {
    const response: EpgProgram = {
      channelId: params.channelId as string,
      title: "Jornal da Tarde",
      description: "Notícias em tempo real",
      startTime: new Date(Date.now() - 1000 * 60 * 10).toISOString(),
      endTime: new Date(Date.now() + 1000 * 60 * 50).toISOString(),
      isCurrentlyAiring: true,
    };
    return HttpResponse.json(response);
  }),

  http.get("*/api/health", () => {
    const response: HealthStatus = {
      status: "ok",
      redis: "connected",
      playlistLoaded: true,
      totalChannels: 120,
      totalMovies: 80,
      totalSeries: 40,
      totalLive: 20,
      lastParsedAt: new Date().toISOString(),
      parseTimeMs: 1200,
      cacheHitRate: "92%",
    };
    return HttpResponse.json(response);
  }),
];
