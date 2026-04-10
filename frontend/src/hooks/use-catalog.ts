import { useInfiniteQuery, useQuery } from "@tanstack/react-query";
import { catalogService } from "@/services/catalog";
import type { SortField, SortOrder } from "@/types/media";

export function useHomeFeed() {
  return useQuery({
    queryKey: ["home-feed"],
    queryFn: () => catalogService.getHomeFeed(),
  });
}

export function useMovies() {
  return useQuery({
    queryKey: ["movies"],
    queryFn: () => catalogService.getMovies(),
  });
}

export function useMoviesTrending() {
  return useQuery({
    queryKey: ["movies-trending"],
    queryFn: () => catalogService.getMoviesTrending(),
  });
}

export function useSeries() {
  return useQuery({
    queryKey: ["series"],
    queryFn: () => catalogService.getSeries(),
  });
}

export function useChannels() {
  return useQuery({
    queryKey: ["channels"],
    queryFn: () => catalogService.getChannels(),
  });
}

export function useSearchCatalog(query: string) {
  return useQuery({
    queryKey: ["search", query],
    queryFn: () => catalogService.search(query),
    enabled: query.length >= 2,
  });
}

const QUERY_CONFIG = {
  staleTime: 5 * 60 * 1000,
  gcTime: 10 * 60 * 1000,
  retry: 2,
  refetchOnWindowFocus: false,
} as const;

export function useMoviesAll(params: {
  page: number;
  size: number;
  sort: SortField;
  order: SortOrder;
  group?: string;
}) {
  return useInfiniteQuery({
    queryKey: ["movies-all", params],
    queryFn: ({ pageParam = params.page }) =>
      catalogService.getMoviesAll({ ...params, page: pageParam }),
    initialPageParam: params.page,
    getNextPageParam: (lastPage) =>
      lastPage.page < lastPage.totalPages - 1 ? lastPage.page + 1 : undefined,
    ...QUERY_CONFIG,
  });
}

export function useSeriesAll(params: {
  page: number;
  size: number;
  sort: SortField;
  order: SortOrder;
  group?: string;
}) {
  return useInfiniteQuery({
    queryKey: ["series-all", params],
    queryFn: ({ pageParam = params.page }) =>
      catalogService.getSeriesAll({ ...params, page: pageParam }),
    initialPageParam: params.page,
    getNextPageParam: (lastPage) =>
      lastPage.page < lastPage.totalPages - 1 ? lastPage.page + 1 : undefined,
    ...QUERY_CONFIG,
  });
}

export function useLiveChannels(params: {
  category?: string;
  page: number;
  size: number;
}) {
  return useInfiniteQuery({
    queryKey: ["live-channels", params],
    queryFn: ({ pageParam = params.page }) =>
      catalogService.getLiveChannels({ ...params, page: pageParam }),
    initialPageParam: params.page,
    getNextPageParam: (lastPage) =>
      lastPage.page < lastPage.totalPages - 1 ? lastPage.page + 1 : undefined,
    ...QUERY_CONFIG,
  });
}

export function useGlobalSearch(query: string) {
  return useQuery({
    queryKey: ["global-search", query],
    queryFn: ({ signal }) => catalogService.globalSearch(query, undefined, signal),
    enabled: query.length >= 2,
    ...QUERY_CONFIG,
  });
}

export function useFavorites() {
  return useQuery({
    queryKey: ["favorites"],
    queryFn: () => catalogService.getFavorites(),
    ...QUERY_CONFIG,
  });
}

export function useHistory() {
  return useQuery({
    queryKey: ["history"],
    queryFn: () => catalogService.getHistory(),
    ...QUERY_CONFIG,
  });
}
