import { Dialog, DialogContent, DialogTitle } from '@/components/ui/dialog';
import type { MediaItem } from '@/types/media';
import { useRef, useEffect } from 'react';

interface PlayerModalProps {
  item: MediaItem | null;
  open: boolean;
  onClose: () => void;
}

export function PlayerModal({ item, open, onClose }: PlayerModalProps) {
  const videoRef = useRef<HTMLVideoElement>(null);
  const hlsRef = useRef<any>(null);

  useEffect(() => {
    if (!open || !item || !videoRef.current) return;
    const video = videoRef.current;

    if (item.streamUrl.includes('.m3u8')) {
      import('hls.js').then(({ default: Hls }) => {
        if (Hls.isSupported()) {
          const hls = new Hls();
          hlsRef.current = hls;
          hls.loadSource(item.streamUrl);
          hls.attachMedia(video);
          hls.on(Hls.Events.MANIFEST_PARSED, () => {
            video.play().catch(() => {});
          });
        } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
          video.src = item.streamUrl;
          video.play().catch(() => {});
        }
      });
    } else {
      video.src = item.streamUrl;
      video.play().catch(() => {});
    }

    return () => {
      if (hlsRef.current) {
        hlsRef.current.destroy();
        hlsRef.current = null;
      }
    };
  }, [open, item]);

  if (!item) return null;

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-5xl w-[95vw] p-0 bg-card border-border overflow-hidden gap-0">
        <DialogTitle className="sr-only">{item.title}</DialogTitle>
        <div className="relative aspect-video bg-background">
          <video ref={videoRef} className="w-full h-full" controls autoPlay playsInline />
        </div>
        <div className="p-4 md:p-6">
          <h3 className="text-lg font-semibold text-foreground">{item.title}</h3>
          <div className="flex items-center gap-3 text-sm text-muted-foreground mt-1">
            {item.year && <span>{item.year}</span>}
            {item.rating && <span className="text-primary">★ {item.rating}</span>}
            {item.duration && <span>{item.duration}</span>}
            {item.type === 'series' && item.seasons && <span>{item.seasons} temporadas</span>}
          </div>
          <p className="text-sm text-muted-foreground mt-3">{item.description}</p>
        </div>
      </DialogContent>
    </Dialog>
  );
}
