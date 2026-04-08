import { createFileRoute } from '@tanstack/react-router';
import { useState } from 'react';
import { Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { useSearchCatalog } from '@/hooks/use-catalog';
import { MediaCard } from '@/components/media/MediaCard';
import { PlayerModal } from '@/components/media/PlayerModal';
import type { MediaItem } from '@/types/media';

export const Route = createFileRoute('/_authenticated/search')({
  component: SearchPage,
});

function SearchPage() {
  const [query, setQuery] = useState('');
  const { data: results, isLoading } = useSearchCatalog(query);
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  return (
    <div className="pt-24 pb-12 px-4 md:px-12">
      <h1 className="text-3xl font-bold text-foreground mb-8">Buscar</h1>
      <div className="relative max-w-xl mb-8">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-muted-foreground" />
        <Input
          value={query}
          onChange={e => setQuery(e.target.value)}
          placeholder="Buscar filmes, séries, canais..."
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
      ) : results?.length ? (
        <>
          <p className="text-sm text-muted-foreground mb-4">{results.length} resultado(s) para "{query}"</p>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
            {results.map(item => <MediaCard key={item.id} item={item} onPlay={setPlaying} className="w-full" />)}
          </div>
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
