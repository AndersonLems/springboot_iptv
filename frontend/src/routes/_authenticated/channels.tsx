import { createFileRoute } from '@tanstack/react-router';
import { useChannels } from '@/hooks/use-catalog';
import { PlayerModal } from '@/components/media/PlayerModal';
import { useState } from 'react';
import type { MediaItem } from '@/types/media';
import { Play, Radio } from 'lucide-react';

export const Route = createFileRoute('/_authenticated/channels')({
  component: ChannelsPage,
});

function ChannelsPage() {
  const { data: channels, isLoading } = useChannels();
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  return (
    <div className="pt-24 pb-12 px-4 md:px-12">
      <h1 className="text-3xl font-bold text-foreground mb-8">Canais ao Vivo</h1>
      {isLoading ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {[1, 2, 3, 4, 5, 6].map(i => <div key={i} className="aspect-video bg-card rounded-lg animate-pulse" />)}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {channels?.map(ch => (
            <div key={ch.id} onClick={() => setPlaying(ch)} className="group cursor-pointer rounded-lg overflow-hidden bg-card border border-border hover:border-primary/50 transition-all duration-300 hover:scale-[1.02]">
              <div className="relative aspect-video">
                <img src={ch.cover} alt={ch.title} className="w-full h-full object-cover" loading="lazy" />
                <div className="absolute inset-0 bg-background/40 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                  <Play className="h-12 w-12 text-foreground fill-foreground drop-shadow-lg" />
                </div>
                <div className="absolute top-3 right-3 flex items-center gap-1 bg-primary px-2 py-0.5 rounded text-xs font-semibold text-primary-foreground">
                  <Radio className="h-3 w-3" /> AO VIVO
                </div>
              </div>
              <div className="p-4">
                <h3 className="font-semibold text-foreground">{ch.title}</h3>
                <p className="text-sm text-muted-foreground mt-1 line-clamp-2">{ch.description}</p>
              </div>
            </div>
          ))}
        </div>
      )}
      <PlayerModal item={playing} open={!!playing} onClose={() => setPlaying(null)} />
    </div>
  );
}
