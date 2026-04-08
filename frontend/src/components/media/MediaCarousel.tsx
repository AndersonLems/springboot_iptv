import { ChevronLeft, ChevronRight } from 'lucide-react';
import { useRef } from 'react';
import type { MediaItem } from '@/types/media';
import { MediaCard } from './MediaCard';

interface MediaCarouselProps {
  title: string;
  items: MediaItem[];
  onPlay: (item: MediaItem) => void;
  myListIds?: string[];
  onToggleList?: (item: MediaItem) => void;
}

export function MediaCarousel({ title, items, onPlay, myListIds = [], onToggleList }: MediaCarouselProps) {
  const scrollRef = useRef<HTMLDivElement>(null);

  const scroll = (direction: 'left' | 'right') => {
    if (!scrollRef.current) return;
    const amount = scrollRef.current.clientWidth * 0.75;
    scrollRef.current.scrollBy({ left: direction === 'left' ? -amount : amount, behavior: 'smooth' });
  };

  if (!items.length) return null;

  return (
    <section className="mb-8">
      <h2 className="text-lg md:text-xl font-semibold text-foreground mb-3 px-4 md:px-12">{title}</h2>
      <div className="group/carousel relative">
        <button
          onClick={() => scroll('left')}
          className="absolute left-0 top-0 bottom-10 z-10 w-10 bg-gradient-to-r from-background/90 to-transparent flex items-center justify-center opacity-0 group-hover/carousel:opacity-100 transition-opacity"
        >
          <ChevronLeft className="h-6 w-6 text-foreground" />
        </button>
        <div
          ref={scrollRef}
          className="flex gap-3 overflow-x-auto scrollbar-hide px-4 md:px-12 scroll-smooth pb-2"
        >
          {items.map(item => (
            <MediaCard
              key={item.id}
              item={item}
              onPlay={onPlay}
              isInList={myListIds.includes(item.id)}
              onToggleList={onToggleList}
            />
          ))}
        </div>
        <button
          onClick={() => scroll('right')}
          className="absolute right-0 top-0 bottom-10 z-10 w-10 bg-gradient-to-l from-background/90 to-transparent flex items-center justify-center opacity-0 group-hover/carousel:opacity-100 transition-opacity"
        >
          <ChevronRight className="h-6 w-6 text-foreground" />
        </button>
      </div>
    </section>
  );
}
