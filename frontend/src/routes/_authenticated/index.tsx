import { createFileRoute } from '@tanstack/react-router';
import { useHomeFeed } from '@/hooks/use-catalog';
import { useMyList, useWatchHistory } from '@/hooks/use-preferences';
import { HeroBanner } from '@/components/media/HeroBanner';
import { MediaCarousel } from '@/components/media/MediaCarousel';
import { PlayerModal } from '@/components/media/PlayerModal';
import { useState } from 'react';
import type { MediaItem } from '@/types/media';

export const Route = createFileRoute('/_authenticated/')({
  component: HomePage,
});

function HomePage() {
  const { data, isLoading } = useHomeFeed();
  const [myListIds, addToList, removeFromList] = useMyList();
  const [, addToHistory] = useWatchHistory();
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  const handlePlay = (item: MediaItem) => {
    addToHistory(item.id);
    setPlaying(item);
  };

  const handleToggleList = (item: MediaItem) => {
    if (myListIds.includes(item.id)) removeFromList(item.id);
    else addToList(item.id);
  };

  if (isLoading || !data) {
    return (
      <div className="pt-16">
        <div className="h-[70vh] bg-card animate-pulse" />
        {[1, 2, 3].map(i => (
          <div key={i} className="mb-8 px-4 md:px-12">
            <div className="h-6 w-40 bg-card rounded mb-4 animate-pulse" />
            <div className="flex gap-3">
              {[1, 2, 3, 4, 5, 6].map(j => (
                <div key={j} className="w-[150px] md:w-[200px] aspect-[2/3] bg-card rounded-lg animate-pulse flex-shrink-0" />
              ))}
            </div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div>
      <HeroBanner hero={data.hero} onPlay={handlePlay} />
      <div className="-mt-16 relative z-10">
        {data.categories.map(cat => (
          <MediaCarousel
            key={cat.name}
            title={cat.name}
            items={cat.items}
            onPlay={handlePlay}
            myListIds={myListIds}
            onToggleList={handleToggleList}
          />
        ))}
      </div>
      <PlayerModal item={playing} open={!!playing} onClose={() => setPlaying(null)} />
    </div>
  );
}
