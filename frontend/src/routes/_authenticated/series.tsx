// @ts-nocheck
/// <reference types="react" />
import { createFileRoute, Outlet, useNavigate, useRouterState } from '@tanstack/react-router';
import { useSeries } from '@/hooks/use-catalog';
import { useMyList } from '@/hooks/use-preferences';
import { MediaCarousel } from '@/components/media/MediaCarousel';
import { PlayerModal } from '@/components/media/PlayerModal';
import { NetflixSpinner } from '@/components/ui/NetflixSpinner';
import { useState, type ReactNode } from 'react';
import type { Category, MediaItem } from '@/types/media';

export const Route = createFileRoute('/_authenticated/series')({
  component: SeriesPage,
});

function SeriesPage() {
  const pathname = useRouterState({
    select: (s: { location: { pathname: string } }) => s.location.pathname,
  });
  const isChildRoute = pathname.startsWith('/series/');

  const browseSearch = {
    group: '',
    sort: 'name' as const,
    order: 'asc' as const,
    yearMin: 1970,
    yearMax: new Date().getFullYear(),
    minRating: 0,
    availableOnly: false,
    genres: [] as number[],
  };

  if (isChildRoute && pathname !== '/series') {
    return <Outlet />;
  }

  const { data: categories, isLoading } = useSeries();
  const navigate = useNavigate();
  const [myListIds, addToList, removeFromList] = useMyList();
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  const loadingCards = ([1, 2, 3, 4, 5].map((j) => (
    <div
      key={j}
      className="w-[200px] aspect-[2/3] bg-card rounded-lg animate-pulse flex-shrink-0"
    />
  )) as unknown) as ReactNode;

  const loadingRows = ([1, 2, 3].map((i) => (
    <div key={i} className="px-4 md:px-12">
      <div className="h-6 w-40 bg-card rounded mb-4 animate-pulse" />
      <div className="flex gap-3">{loadingCards}</div>
    </div>
  )) as unknown) as ReactNode;

  const handleToggleList = (item: MediaItem) => {
    myListIds.includes(item.id) ? removeFromList(item.id) : addToList(item.id);
  };

  return (
    <div className="pt-24 pb-12">
      <div className="px-4 md:px-12 mb-8 flex items-center justify-between gap-4">
        <h1 className="text-3xl font-bold text-foreground">Séries</h1>
        <button
          onClick={() => navigate({ to: '/series/browse', search: browseSearch })}
          className="text-sm text-muted-foreground hover:text-foreground hover:underline"
        >
          Ver todas →
        </button>
      </div>
      {isLoading ? (
        <div className="space-y-8">
          <NetflixSpinner className="px-4 md:px-12" />
          {loadingRows}
        </div>
      ) : (
        categories?.map((cat: Category) => (
          <MediaCarousel
            key={cat.name}
            title={cat.name}
            items={cat.items}
            onPlay={setPlaying}
            myListIds={myListIds}
            onToggleList={handleToggleList}
            titleAction={(
              <button
                onClick={() => navigate({ to: '/series/browse', search: browseSearch })}
                className="text-xs text-muted-foreground hover:text-foreground hover:underline"
              >
                Ver mais →
              </button>
            )}
          />
        ))
      )}
      <PlayerModal item={playing} open={!!playing} onClose={() => setPlaying(null)} />
    </div>
  );
}
