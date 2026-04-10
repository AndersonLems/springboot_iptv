export type MediaType = "movie" | "series" | "channel";

export interface MediaStreamOption {
  name?: string;
  streamUrl: string;
  groupTitle?: string;
  logoUrl?: string;
  quality?: string;
  season?: number;
  episode?: number;
}

export interface MediaItem {
  id: string;
  title: string;
  cover: string;
  backdrop?: string;
  streamUrl: string;
  streamOptions?: MediaStreamOption[];
  type: MediaType;
  available?: boolean;
  description?: string;
  year?: number;
  rating?: string;
  duration?: string;
  genres?: string[];
  seasons?: number;
  episodes?: number;
}

export interface Category {
  name: string;
  items: MediaItem[];
}

export interface User {
  id: string;
  username: string;
  displayName: string;
  avatar: string;
  email: string;
}

export interface AuthSession {
  user: User;
  token: string;
}

export interface HeroContent {
  item: MediaItem;
  tagline?: string;
}

export interface HomeFeed {
  hero: HeroContent;
  categories: Category[];
}

export interface Channel {
  id: string | null;
  name: string;
  logoUrl: string;
  groupTitle: string;
  streamUrl: string;
  duration: number;
}

export interface CategoryGroup {
  parent: string;
  subcategories: string[];
  totalChannels: number;
}

export interface StreamOption {
  name: string;
  streamUrl: string;
  groupTitle: string;
  logoUrl: string;
  quality: "UHD_4K" | "HDR" | "LEGENDADO" | "DUBLADO" | "OUTRO";
}

export interface EnrichedMovie {
  tmdbId: number;
  title: string;
  overview: string;
  releaseDate: string;
  voteAverage: number;
  voteCount: number;
  popularity: number;
  posterPath: string;
  backdropPath: string;
  genreIds: number[];
  streams: StreamOption[];
  available: boolean;
}

export interface EnrichedSeries {
  tmdbId: number;
  name: string;
  overview: string;
  firstAirDate: string;
  voteAverage: number;
  voteCount: number;
  popularity: number;
  posterPath: string;
  backdropPath?: string;
  genreIds: number[];
  streams: StreamOption[];
  available: boolean;
}

export interface LiveChannel extends Channel {
  isLive: boolean;
  category: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sort: SortField;
  order: SortOrder;
}

export interface WatchHistoryItem {
  channel: Channel;
  watchedAt: string;
}

export interface GlobalSearchResult {
  query: string;
  movies: Channel[];
  series: Channel[];
  live: Channel[];
  totalResults: number;
}

export interface EpgProgram {
  channelId: string;
  title: string;
  description: string;
  startTime: string;
  endTime: string;
  isCurrentlyAiring: boolean;
}

export interface HealthStatus {
  status: string;
  redis: string;
  playlistLoaded: boolean;
  totalChannels: number;
  totalMovies: number;
  totalSeries: number;
  totalLive: number;
  lastParsedAt: string;
  parseTimeMs: number;
  cacheHitRate: string;
}

export type SortField = "name" | "year" | "voteAverage" | "popularity";
export type SortOrder = "asc" | "desc";

export interface FilterState {
  yearMin: number;
  yearMax: number;
  minRating: number;
  availableOnly: boolean;
  genres: number[];
}
