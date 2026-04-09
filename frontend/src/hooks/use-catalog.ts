import { useQuery } from "@tanstack/react-query";
import { catalogService } from "@/services/catalog";

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
