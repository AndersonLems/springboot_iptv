import { createFileRoute } from '@tanstack/react-router';
import { useMemo, useState } from 'react';
import { Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { useGlobalSearch } from '@/hooks/use-catalog';
import { MediaCard } from '@/components/media/MediaCard';
import { PlayerModal } from '@/components/media/PlayerModal';
import { useDebounce } from '@/hooks/use-debounce';
import type { Channel, MediaItem } from '@/types/media';

export const Route = createFileRoute('/_authenticated/search')({
  component: SearchPage,
});

export function SearchPage() {
  const [query, setQuery] = useState('');
  const debouncedQuery = useDebounce(query, 300);
  const { data: results, isLoading } = useGlobalSearch(debouncedQuery);
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  const toMediaItem = (channel: Channel, type: MediaItem['type']): MediaItem => ({
    id: channel.id ?? channel.streamUrl,
    title: channel.name,
    type,
    cover: channel.logoUrl,
    streamUrl: channel.streamUrl,
    description: channel.groupTitle,
  });

  const sections = useMemo(() => {
    if (!results) return [];
    return [
      {
        title: 'Filmes',
        items: results.movies.map((ch) => toMediaItem(ch, 'movie')),
      },
      {
        title: 'Series',
        items: results.series.map((ch) => toMediaItem(ch, 'series')),
      },
      {
        title: 'Canais ao Vivo',
        items: results.live.map((ch) => toMediaItem(ch, 'channel')),
      },
    ].filter((section) => section.items.length > 0);
  }, [results]);

  return (
    <div className="pt-24 pb-12 px-4 md:px-12">
      <h1 className="text-3xl font-bold text-foreground mb-8">Buscar</h1>
      <div className="relative max-w-xl mb-8">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
        <Input
          value={query}
          onChange={e => setQuery(e.target.value)}
          placeholder={isLoading ? 'Buscando...' : 'Buscar filmes, séries, canais...'}
          className="pl-10 bg-secondary border-border text-foreground placeholder:text-muted-foreground h-12 text-base"
        />
      </div>
      {query.length < 2 ? (
        <div className="text-center py-16">
          <Search className="h-16 w-16 text-muted-foreground/30 mx-auto mb-4" />
          <p className="text-muted-foreground">Digite pelo menos 2 caracteres para buscar</p>
        </div>
      ) : isLoading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
          {[1, 2, 3, 4, 5, 6].map(i => <div key={i} className="aspect-[2/3] bg-card rounded-lg animate-pulse" />)}
        </div>
      ) : sections.length ? (
        <>
          {sections.map((section) => (
            <div key={section.title} className="mb-8">
              <div className="flex items-center justify-between mb-3">
                <h2 className="text-lg font-semibold text-foreground">
                  {section.title}
                </h2>
                <span className="text-sm text-muted-foreground">
                  {section.items.length} {section.title.toLowerCase()}
                </span>
              </div>
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
                {section.items.map(item => (
                  <MediaCard key={item.id} item={item} onPlay={setPlaying} className="w-full" />
                ))}
              </div>
            </div>
          ))}
        </>
      ) : (
        <div className="text-center py-16">
          <p className="text-muted-foreground">Nenhum resultado encontrado para "{query}"</p>
        </div>
      )}
      <PlayerModal item={playing} open={!!playing} onClose={() => setPlaying(null)} />
    </div>
  );
}
