import { Play, Info } from 'lucide-react';
import type { HeroContent, MediaItem } from '@/types/media';
import { Button } from '@/components/ui/button';
import heroBackdrop from '@/assets/hero-backdrop.jpg';

interface HeroBannerProps {
  hero: HeroContent;
  onPlay: (item: MediaItem) => void;
}

export function HeroBanner({ hero, onPlay }: HeroBannerProps) {
  const { item, tagline } = hero;

  return (
    <div className="relative h-[70vh] md:h-[85vh] w-full overflow-hidden">
      <div className="absolute inset-0">
        <img
          src={heroBackdrop}
          alt={item.title}
          className="w-full h-full object-cover"
          width={1920}
          height={1080}
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/50 to-background/20" />
        <div className="absolute inset-0 bg-gradient-to-r from-background/90 via-background/40 to-transparent" />
      </div>
      <div className="relative h-full flex flex-col justify-end pb-16 md:pb-24 px-4 md:px-12 max-w-3xl">
        {tagline && (
          <span className="text-primary text-sm font-semibold mb-3 uppercase tracking-widest">
            {tagline}
          </span>
        )}
        <h1 className="text-4xl md:text-6xl font-extrabold text-foreground mb-4 leading-tight">
          {item.title}
        </h1>
        <div className="flex flex-wrap items-center gap-3 text-sm text-muted-foreground mb-4">
          {item.year && <span>{item.year}</span>}
          {item.rating && <span className="text-primary font-semibold">★ {item.rating}</span>}
          {item.duration && <span>{item.duration}</span>}
          {item.genres?.map(g => (
            <span key={g} className="px-2 py-0.5 rounded bg-secondary text-secondary-foreground text-xs">
              {g}
            </span>
          ))}
        </div>
        <p className="text-muted-foreground text-base md:text-lg mb-6 line-clamp-3">
          {item.description}
        </p>
        <div className="flex gap-3">
          <Button onClick={() => onPlay(item)} size="lg" className="gap-2 bg-primary text-primary-foreground hover:bg-primary/90">
            <Play className="h-5 w-5 fill-current" /> Assistir
          </Button>
          <Button variant="secondary" size="lg" className="gap-2">
            <Info className="h-5 w-5" /> Mais Informações
          </Button>
        </div>
      </div>
    </div>
  );
}
