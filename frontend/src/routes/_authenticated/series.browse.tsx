import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useEffect, useMemo, useRef, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useWindowVirtualizer, type VirtualItem } from "@tanstack/react-virtual";
import type {
  EnrichedSeries,
  FilterState,
  MediaItem,
  SortField,
  SortOrder,
} from "@/types/media";
import { useInfiniteScroll } from "@/hooks/use-infinite-scroll";
import { useSeriesAll } from "@/hooks/use-catalog";
import { PlayerModal } from "@/components/media/PlayerModal";
import { RatingBadge } from "@/components/media/RatingBadge";
import { AvailableBadge } from "@/components/media/AvailableBadge";
import { FilterPanel } from "@/components/ui/FilterPanel";
import { SortToolbar } from "@/components/ui/SortToolbar";
import { SkeletonCard } from "@/components/media/SkeletonCard";

const TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w780";
const currentYear = new Date().getFullYear();

const SERIES_GENRES = [
  { id: 10759, name: "Ação & Aventura" },
  { id: 16, name: "Animação" },
  { id: 35, name: "Comédia" },
  { id: 80, name: "Crime" },
  { id: 99, name: "Documentário" },
  { id: 18, name: "Drama" },
  { id: 10751, name: "Família" },
  { id: 10762, name: "Infantil" },
  { id: 9648, name: "Mistério" },
  { id: 10763, name: "Notícias" },
  { id: 10764, name: "Reality" },
  { id: 10765, name: "Sci-Fi & Fantasia" },
  { id: 10766, name: "Novela" },
  { id: 10767, name: "Talk Show" },
  { id: 10768, name: "Guerra & Política" },
  { id: 37, name: "Faroeste" },
];

function parseNumber(value: unknown, fallback: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

function parseSort(value: unknown): SortField {
  if (value === "year" || value === "voteAverage" || value === "popularity") {
    return value;
  }
  return "name";
}

function parseOrder(value: unknown): SortOrder {
  return value === "desc" ? "desc" : "asc";
}

function parseGenres(value: unknown): number[] {
  if (Array.isArray(value)) {
    return value
      .map((item) => Number(item))
      .filter((item) => Number.isFinite(item));
  }
  if (typeof value === "string") {
    return value
      .split(",")
      .map((item) => Number(item))
      .filter((item) => Number.isFinite(item));
  }
  return [];
}

export const Route = createFileRoute("/_authenticated/series/browse")({
  validateSearch: (search) => ({
    group: typeof search.group === "string" ? search.group : "",
    sort: parseSort(search.sort),
    order: parseOrder(search.order),
    yearMin: parseNumber(search.yearMin, 1970),
    yearMax: parseNumber(search.yearMax, currentYear),
    minRating: parseNumber(search.minRating, 0),
    availableOnly: search.availableOnly === true || search.availableOnly === "true",
    genres: parseGenres(search.genres),
  }),
  component: SeriesBrowsePage,
});

function SeriesBrowsePage() {
  const search = Route.useSearch();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  const [filters, setFilters] = useState<FilterState>({
    yearMin: search.yearMin,
    yearMax: search.yearMax,
    minRating: search.minRating,
    availableOnly: search.availableOnly,
    genres: search.genres,
  });

  const [sort, setSort] = useState<SortField>(search.sort);
  const [order, setOrder] = useState<SortOrder>(search.order);
  const [filtersOpen, setFiltersOpen] = useState(false);

  const sentinelRef = useRef<HTMLDivElement>(null);
  const gridRef = useRef<HTMLDivElement>(null);
  const [columns, setColumns] = useState(1);

  useEffect(() => {
    setFilters({
      yearMin: search.yearMin,
      yearMax: search.yearMax,
      minRating: search.minRating,
      availableOnly: search.availableOnly,
      genres: search.genres,
    });
    setSort(search.sort);
    setOrder(search.order);
  }, [
    search.availableOnly,
    search.genres,
    search.minRating,
    search.order,
    search.sort,
    search.yearMax,
    search.yearMin,
  ]);

  useEffect(() => {
    const nextSearch = {
      group: search.group,
      sort,
      order,
      yearMin: filters.yearMin,
      yearMax: filters.yearMax,
      minRating: filters.minRating,
      availableOnly: filters.availableOnly,
      genres: filters.genres,
    };

    const hasChanged =
      search.sort !== nextSearch.sort ||
      search.order !== nextSearch.order ||
      search.yearMin !== nextSearch.yearMin ||
      search.yearMax !== nextSearch.yearMax ||
      search.minRating !== nextSearch.minRating ||
      search.availableOnly !== nextSearch.availableOnly ||
      search.genres.join(",") !== nextSearch.genres.join(",");

    if (hasChanged) {
      navigate({ to: "/series/browse", search: nextSearch, replace: true });
      queryClient.removeQueries({ queryKey: ["series-all"] });
    }
  }, [
    filters.availableOnly,
    filters.genres,
    filters.minRating,
    filters.yearMax,
    filters.yearMin,
    navigate,
    order,
    queryClient,
    search.group,
    sort,
  ]);

  useEffect(() => {
    const element = gridRef.current;
    if (!element) return;

    const compute = () => {
      const width = element.clientWidth;
      const col = Math.max(1, Math.floor(width / 176));
      setColumns(col);
    };

    compute();
    const observer = new ResizeObserver(compute);
    observer.observe(element);
    return () => observer.disconnect();
  }, []);

  const { data, hasNextPage, fetchNextPage, isFetchingNextPage, isLoading } =
    useSeriesAll({
      page: 0,
      size: 50,
      sort,
      order,
      group: search.group || undefined,
    });

  const items = useMemo(() => {
    const pages = data?.pages.flatMap((page) => page.content) ?? [];
    return pages.filter((series) => {
      if (filters.availableOnly && !series.available) return false;
      if (series.voteAverage < filters.minRating) return false;
      const year = series.firstAirDate
        ? Number(series.firstAirDate.slice(0, 4))
        : null;
      if (year && year < filters.yearMin) return false;
      if (year && year > filters.yearMax) return false;
      if (filters.genres.length > 0) {
        const genres = series.genreIds ?? [];
        const matches = filters.genres.some((g) => genres.includes(g));
        if (!matches) return false;
      }
      return true;
    });
  }, [data?.pages, filters]);

  useInfiniteScroll(
    sentinelRef,
    () => {
      if (hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    !!hasNextPage,
  );

  const rowCount = Math.ceil(items.length / columns);
  const rowVirtualizer = useWindowVirtualizer({
    count: rowCount,
    estimateSize: () => 320,
    overscan: 6,
    scrollMargin: gridRef.current?.offsetTop ?? 0,
  });

  const virtualRows = rowVirtualizer.getVirtualItems() as VirtualItem[];

  const toMediaItem = (series: EnrichedSeries): MediaItem => {
    const streamOptions = series.streams.map((stream) => ({
      name: stream.name,
      streamUrl: stream.streamUrl,
      groupTitle: stream.groupTitle,
      logoUrl: stream.logoUrl,
      quality: stream.quality,
    }));

    return {
      id: `tmdb-series-${series.tmdbId}`,
      title: series.name,
      type: "series",
      cover: series.posterPath
        ? `${TMDB_IMAGE_BASE_URL}${series.posterPath}`
        : series.streams[0]?.logoUrl ?? "",
      backdrop: series.backdropPath
        ? `${TMDB_IMAGE_BASE_URL}${series.backdropPath}`
        : undefined,
      streamUrl: series.streams[0]?.streamUrl ?? "",
      streamOptions,
      description: series.overview,
      year: series.firstAirDate
        ? Number(series.firstAirDate.slice(0, 4))
        : undefined,
      rating: series.voteAverage?.toFixed(1),
      available: series.available,
    };
  };

  return (
    <div className="pt-24 pb-12 px-4 md:px-12">
      <div className="flex flex-wrap items-center justify-between gap-4 mb-4">
        <button
          onClick={() => navigate({ to: "/series" })}
          className="text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          ← Séries
        </button>

        <h1 className="text-2xl md:text-3xl font-bold text-foreground flex-1">
          {search.group ? decodeURIComponent(search.group) : "Todas as séries"}
        </h1>

        <button
          onClick={() => setFiltersOpen((prev) => !prev)}
          className="text-sm text-muted-foreground hover:text-foreground transition-colors"
        >
          {filtersOpen ? "Ocultar filtros" : "Mostrar filtros"}
        </button>
      </div>

      <SortToolbar
        sort={sort}
        order={order}
        onChange={(nextSort, nextOrder) => {
          setSort(nextSort);
          setOrder(nextOrder);
        }}
      />

      <FilterPanel
        filters={filters}
        onChange={setFilters}
        genres={SERIES_GENRES}
        open={filtersOpen}
        onToggle={() => setFiltersOpen((prev) => !prev)}
      />

      <div ref={gridRef} className="relative">
        {isLoading ? (
          <div className="grid gap-4 grid-cols-[repeat(auto-fill,minmax(160px,1fr))]">
            {Array.from({ length: 12 }).map((_, index) => (
              <SkeletonCard key={`skeleton-${index}`} />
            ))}
          </div>
        ) : (
          <div
            className="relative"
            style={{ height: `${rowVirtualizer.getTotalSize()}px` }}
          >
            {virtualRows.map((virtualRow) => {
              const start = virtualRow.index * columns;
              const rowItems = items.slice(start, start + columns);
              return (
                <div
                  key={virtualRow.key}
                  className="grid gap-4 grid-cols-[repeat(auto-fill,minmax(160px,1fr))]"
                  style={{
                    position: "absolute",
                    top: 0,
                    left: 0,
                    width: "100%",
                    transform: `translateY(${virtualRow.start}px)`,
                  }}
                >
                  {rowItems.map((series) => {
                    const item = toMediaItem(series);
                    return (
                      <button
                        key={item.id}
                        onClick={() => setPlaying(item)}
                        className="group text-left"
                      >
                        <div className="aspect-[2/3] rounded-lg overflow-hidden bg-card relative">
                          <img
                            src={item.cover}
                            alt={item.title}
                            className="w-full h-full object-cover"
                            loading="lazy"
                          />
                        </div>
                        <div className="mt-2 space-y-1">
                          <p className="text-sm font-semibold text-foreground truncate">
                            {item.title}
                          </p>
                          <div className="flex items-center justify-between">
                            <span className="text-xs text-muted-foreground">
                              {item.year ?? ""}
                            </span>
                            <RatingBadge rating={series.voteAverage} />
                          </div>
                          <AvailableBadge available={series.available} />
                        </div>
                      </button>
                    );
                  })}
                </div>
              );
            })}
          </div>
        )}
      </div>

      <div ref={sentinelRef} className="h-10" />

      {isFetchingNextPage && (
        <div className="mt-4 grid gap-4 grid-cols-[repeat(auto-fill,minmax(160px,1fr))]">
          {Array.from({ length: columns }).map((_, index) => (
            <SkeletonCard key={`loading-${index}`} />
          ))}
        </div>
      )}

      <PlayerModal item={playing} open={!!playing} onClose={() => setPlaying(null)} />
    </div>
  );
}
