import { Play, Plus, Check } from 'lucide-react';
import type { MediaItem } from '@/types/media';

interface MediaCardProps {
  item: MediaItem;
  onPlay: (item: MediaItem) => void;
  isInList?: boolean;
  onToggleList?: (item: MediaItem) => void;
  className?: string;
}

export function MediaCard({ item, onPlay, isInList, onToggleList, className }: MediaCardProps) {
  return (
    <div
      className={`group relative cursor-pointer ${className ?? 'w-[150px] md:w-[200px] flex-shrink-0'}`}
      onClick={() => onPlay(item)}
    >
      <div className="aspect-[2/3] rounded-lg overflow-hidden bg-card transition-transform duration-300 group-hover:scale-105 group-hover:z-10 relative">
        <img src={item.cover} alt={item.title} className="w-full h-full object-cover" loading="lazy" />
        <div className="absolute inset-0 bg-background/0 group-hover:bg-background/50 transition-colors duration-300 flex items-center justify-center">
          <Play className="h-10 w-10 text-foreground fill-foreground opacity-0 group-hover:opacity-100 transition-opacity duration-300 drop-shadow-lg" />
        </div>
      </div>
      <div className="mt-2">
        <p className="text-sm text-foreground font-medium truncate">{item.title}</p>
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          {item.year && <span>{item.year}</span>}
          {item.rating && <span>★ {item.rating}</span>}
          {item.type === 'series' && item.seasons && <span>{item.seasons} temp.</span>}
        </div>
      </div>
      {onToggleList && (
        <button
          onClick={(e) => { e.stopPropagation(); onToggleList(item); }}
          className="absolute top-2 right-2 p-1.5 rounded-full bg-background/80 backdrop-blur-sm opacity-0 group-hover:opacity-100 transition-opacity text-foreground hover:bg-background border border-border"
        >
          {isInList ? <Check className="h-4 w-4 text-primary" /> : <Plus className="h-4 w-4" />}
        </button>
      )}
    </div>
  );
}
