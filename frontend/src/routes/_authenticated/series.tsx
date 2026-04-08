import { createFileRoute } from '@tanstack/react-router';
import { useSeries } from '@/hooks/use-catalog';
import { useMyList } from '@/hooks/use-preferences';
import { MediaCarousel } from '@/components/media/MediaCarousel';
import { PlayerModal } from '@/components/media/PlayerModal';
import { useState } from 'react';
import type { MediaItem } from '@/types/media';

export const Route = createFileRoute('/_authenticated/series')({
  component: SeriesPage,
});

function SeriesPage() {
  const { data: categories, isLoading } = useSeries();
  const [myListIds, addToList, removeFromList] = useMyList();
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  const handleToggleList = (item: MediaItem) => {
    myListIds.includes(item.id) ? removeFromList(item.id) : addToList(item.id);
  };

  return (
    <div className="pt-24 pb-12">
      <h1 className="text-3xl font-bold text-foreground px-4 md:px-12 mb-8">Séries</h1>
      {isLoading ? (
        <div className="space-y-8">
          {[1, 2, 3].map(i => (
            <div key={i} className="px-4 md:px-12">
              <div className="h-6 w-40 bg-card rounded mb-4 animate-pulse" />
              <div className="flex gap-3">
                {[1, 2, 3, 4, 5].map(j => <div key={j} className="w-[200px] aspect-[2/3] bg-card rounded-lg animate-pulse flex-shrink-0" />)}
              </div>
            </div>
          ))}
        </div>
      ) : (
        categories?.map(cat => (
          <MediaCarousel key={cat.name} title={cat.name} items={cat.items} onPlay={setPlaying} myListIds={myListIds} onToggleList={handleToggleList} />
        ))
      )}
      <PlayerModal item={playing} open={!!playing} onClose={() => setPlaying(null)} />
    </div>
  );
}
