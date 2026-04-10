import { createFileRoute } from '@tanstack/react-router';
import { useHistory, useHomeFeed } from '@/hooks/use-catalog';
import { useMyList, useWatchHistory } from '@/hooks/use-preferences';
import { HeroBanner } from '@/components/media/HeroBanner';
import { MediaCarousel } from '@/components/media/MediaCarousel';
import { PlayerModal } from '@/components/media/PlayerModal';
import { useState } from 'react';
import { MediaCard } from '@/components/media/MediaCard';
import { ProgressBar } from '@/components/media/ProgressBar';
import type { Channel, MediaItem, WatchHistoryItem } from '@/types/media';

export const Route = createFileRoute('/_authenticated/')({
  component: HomePage,
});

function HomePage() {
  const { data, isLoading } = useHomeFeed();
  const { data: historyItems, isLoading: isHistoryLoading } = useHistory();
  const [myListIds, addToList, removeFromList] = useMyList();
  const [, addToHistory] = useWatchHistory();
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  const toMediaItem = (channel: Channel): MediaItem => ({
    id: channel.id ?? channel.streamUrl,
    title: channel.name,
    type: 'channel',
    cover: channel.logoUrl,
    streamUrl: channel.streamUrl,
    streamOptions: [
      {
        name: channel.name,
        streamUrl: channel.streamUrl,
        groupTitle: channel.groupTitle,
        logoUrl: channel.logoUrl,
      },
    ],
    duration: channel.duration ? `${Math.round(channel.duration / 60)} min` : undefined,
  });

  const getProgress = (item: WatchHistoryItem) => {
    const channelId = item.channel.id ?? item.channel.streamUrl;
    const raw = localStorage.getItem(`playback:${channelId}`);
    const seconds = raw ? Number(raw) : 0;
    if (!Number.isFinite(seconds) || seconds <= 0) return 0;
    if (!item.channel.duration || item.channel.duration <= 0) return 0;
    return Math.min(1, seconds / item.channel.duration);
  };

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
        {!isHistoryLoading && (historyItems?.length ?? 0) > 0 && (
          <div className="pt-12">
            <MediaCarousel
              title="Continuar Assistindo"
              items={historyItems!.slice(0, 5).map((item) => toMediaItem(item.channel))}
              onPlay={handlePlay}
              renderItem={(item) => {
                const source = historyItems!.find((entry) =>
                  (entry.channel.id ?? entry.channel.streamUrl) === item.id,
                );
                const progress = source ? getProgress(source) : 0;
                return (
                  <div className="relative w-[150px] md:w-[200px] flex-shrink-0">
                    <MediaCard item={item} onPlay={handlePlay} className="w-full" />
                    {progress > 0 && (
                      <ProgressBar
                        progress={progress}
                        className="absolute bottom-3 left-2 right-2"
                      />
                    )}
                  </div>
                );
              }}
            />
          </div>
        )}
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
      {!isHistoryLoading && (historyItems?.length ?? 0) > 0 && (
        <div className="pt-12">
          <MediaCarousel
            title="Continuar Assistindo"
            items={historyItems!.slice(0, 5).map((item) => toMediaItem(item.channel))}
            onPlay={handlePlay}
            renderItem={(item) => {
              const source = historyItems!.find((entry) =>
                (entry.channel.id ?? entry.channel.streamUrl) === item.id,
              );
              const progress = source ? getProgress(source) : 0;
              return (
                <div className="relative w-[150px] md:w-[200px] flex-shrink-0">
                  <MediaCard item={item} onPlay={handlePlay} className="w-full" />
                  {progress > 0 && (
                    <ProgressBar
                      progress={progress}
                      className="absolute bottom-3 left-2 right-2"
                    />
                  )}
                </div>
              );
            }}
          />
        </div>
      )}
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
