import { createFileRoute } from '@tanstack/react-router';
import { useEffect, useRef, useState } from 'react';
import { Play } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { PlayerModal } from '@/components/media/PlayerModal';
import { LiveBadge } from '@/components/media/LiveBadge';
import { NetflixSpinner } from '@/components/ui/NetflixSpinner';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { apiClient } from '@/services/api/client';
import { useInfiniteScroll } from '@/hooks/use-infinite-scroll';
import { useLiveChannels } from '@/hooks/use-catalog';
import type { CategoryGroup, LiveChannel, MediaItem } from '@/types/media';

export const Route = createFileRoute('/_authenticated/channels')({
  component: ChannelsPage,
});

function ChannelsPage() {
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const { data: categories } = useQuery({
    queryKey: ['playlist-categories'],
    queryFn: () => apiClient.get<CategoryGroup[]>('/api/playlist/categories'),
  });
  const {
    data,
    isLoading,
    hasNextPage,
    fetchNextPage,
    isFetchingNextPage,
  } = useLiveChannels({
    page: 0,
    size: 50,
    category: selectedCategory || undefined,
  });
  const [playing, setPlaying] = useState<MediaItem | null>(null);
  const sentinelRef = useRef<HTMLDivElement>(null);

  const DEFAULT_CATEGORY = 'Eventos de Hoje';

  const tabOptions = (categories ?? [])
    .filter((group) => group.parent?.toLowerCase().startsWith('canais'))
    .flatMap((group) => group.subcategories)
    .map((subcategory) => subcategory.split('|')[0]?.trim())
    .filter((value, index, self) => !!value && self.indexOf(value) === index)
    .sort((a, b) => a.localeCompare(b, 'pt-BR'));

  useEffect(() => {
    if (!selectedCategory && tabOptions.includes(DEFAULT_CATEGORY)) {
      setSelectedCategory(DEFAULT_CATEGORY);
    }
  }, [selectedCategory, tabOptions]);

  const channels = data?.pages.flatMap((page) => page.content) ?? [];

  useInfiniteScroll(
    sentinelRef,
    () => {
      if (hasNextPage && !isFetchingNextPage) {
        fetchNextPage();
      }
    },
    !!hasNextPage,
  );

  const toMediaItem = (channel: LiveChannel): MediaItem => ({
    id: channel.id ?? channel.streamUrl,
    title: channel.name,
    type: 'channel',
    cover: channel.logoUrl,
    streamUrl: channel.streamUrl,
    description: channel.groupTitle,
  });

  return (
    <div className="pt-24 pb-12 px-4 md:px-12">
      <h1 className="text-3xl font-bold text-foreground mb-6">Canais ao Vivo</h1>
      <Tabs
        value={selectedCategory || DEFAULT_CATEGORY || 'all'}
        onValueChange={(value) =>
          setSelectedCategory(value === 'all' ? '' : value)
        }
        className="mb-6"
      >
        <TabsList className="flex flex-wrap gap-2 h-auto py-2 items-start">
          <TabsTrigger value="all">Todos</TabsTrigger>
          {tabOptions.map((category) => (
            <TabsTrigger key={category} value={category}>
              {category}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      {isLoading ? (
        <div className="space-y-6">
          <NetflixSpinner className="pt-6" />
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="aspect-video bg-card rounded-lg animate-pulse" />
            ))}
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {channels.map((ch, index) => (
            <LiveChannelCard
              key={`${ch.id ?? ""}|${ch.streamUrl ?? ""}|${index}`}
              channel={ch}
              onPlay={() => setPlaying(toMediaItem(ch))}
            />
          ))}
        </div>
      )}

      <div ref={sentinelRef} className="h-10" />
      {isFetchingNextPage && (
        <div className="mt-4 space-y-4">
          <NetflixSpinner />
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map((i) => (
              <div key={`loading-${i}`} className="aspect-video bg-card rounded-lg animate-pulse" />
            ))}
          </div>
        </div>
      )}

      <PlayerModal item={playing} open={!!playing} onClose={() => setPlaying(null)} />
    </div>
  );
}

function LiveChannelCard({
  channel,
  onPlay,
}: {
  channel: LiveChannel;
  onPlay: () => void;
}) {
  return (
    <div
      onClick={onPlay}
      className="group cursor-pointer rounded-lg overflow-hidden bg-card border border-border hover:border-primary/50 transition-all duration-300 hover:scale-[1.02]"
    >
      <div className="relative aspect-video">
        <img
          src={channel.logoUrl}
          alt={channel.name}
          className="w-full h-full object-cover"
          loading="lazy"
        />
        <div className="absolute inset-0 bg-background/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
          <Play className="h-12 w-12 text-foreground fill-foreground drop-shadow-lg" />
        </div>
        <div className="absolute top-3 right-3">
          <LiveBadge />
        </div>
      </div>
      <div className="p-4">
        <h3 className="font-semibold text-foreground">{channel.name}</h3>
      </div>
    </div>
  );
}
