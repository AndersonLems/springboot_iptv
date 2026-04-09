import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import type { MediaItem } from "@/types/media";
import { useEffect, useMemo, useState } from "react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { VideoJsPlayer } from "@/components/media/VideoJsPlayer";

interface PlayerModalProps {
  item: MediaItem | null;
  open: boolean;
  onClose: () => void;
}

function getStreamScore(stream: {
  name?: string;
  quality?: string;
  groupTitle?: string;
}) {
  const text =
    `${stream.name ?? ""} ${stream.quality ?? ""} ${stream.groupTitle ?? ""}`.toLowerCase();

  if (text.includes("4k") || text.includes("uhd")) return 100;
  if (text.includes("1080") || text.includes("fhd") || text.includes("full hd"))
    return 80;
  if (text.includes("hdr")) return 70;
  if (text.includes("720") || text.includes("hd")) return 50;
  return 10;
}

function getBestDefaultStreamUrl(
  streams: Array<{
    streamUrl: string;
    name?: string;
    quality?: string;
    groupTitle?: string;
  }>,
) {
  if (!streams.length) return "";
  return [...streams].sort((a, b) => getStreamScore(b) - getStreamScore(a))[0]
    .streamUrl;
}

export function PlayerModal({ item, open, onClose }: PlayerModalProps) {
  const [selectedStreamUrl, setSelectedStreamUrl] = useState<string>("");

  const streamOptions = useMemo(() => {
    if (!item) return [];
    if (item.streamOptions?.length) return item.streamOptions;
    return [
      {
        name: "Padrão",
        streamUrl: item.streamUrl,
      },
    ];
  }, [item]);

  useEffect(() => {
    if (!open || !item) return;
    setSelectedStreamUrl(
      getBestDefaultStreamUrl(streamOptions) || item.streamUrl,
    );
  }, [open, item, streamOptions]);

  if (!item) return null;

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="max-w-5xl w-[95vw] p-0 bg-card border-border overflow-hidden gap-0">
        <DialogTitle className="sr-only">{item.title}</DialogTitle>
        <div className="relative aspect-video bg-background">
          {selectedStreamUrl ? (
            <VideoJsPlayer src={selectedStreamUrl} poster={item.backdrop} />
          ) : null}
        </div>
        <div className="p-4 md:p-6">
          <h3 className="text-lg font-semibold text-foreground">
            {item.title}
          </h3>
          {streamOptions.length > 1 && (
            <div className="mt-3 max-w-sm">
              <p className="text-xs text-muted-foreground mb-1">
                Opção de stream
              </p>
              <Select
                value={selectedStreamUrl}
                onValueChange={setSelectedStreamUrl}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Selecione uma opção" />
                </SelectTrigger>
                <SelectContent>
                  {streamOptions.map((stream, index) => (
                    <SelectItem
                      key={`${stream.streamUrl}-${index}`}
                      value={stream.streamUrl}
                    >
                      {stream.quality && stream.groupTitle
                        ? `${stream.quality} • ${stream.groupTitle}`
                        : stream.name || stream.quality || `Opção ${index + 1}`}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}
          <div className="flex items-center gap-3 text-sm text-muted-foreground mt-1">
            {item.year && <span>{item.year}</span>}
            {item.rating && (
              <span className="text-primary">★ {item.rating}</span>
            )}
            {item.duration && <span>{item.duration}</span>}
            {item.type === "series" && item.seasons && (
              <span>{item.seasons} temporadas</span>
            )}
          </div>
          <p className="text-sm text-muted-foreground mt-3">
            {item.description}
          </p>
        </div>
      </DialogContent>
    </Dialog>
  );
}
