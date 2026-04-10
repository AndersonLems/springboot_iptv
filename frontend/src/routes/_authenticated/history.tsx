import { createFileRoute } from "@tanstack/react-router";
import { useMemo, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Clock, Trash } from "lucide-react";
import { useHistory } from "@/hooks/use-catalog";
import { catalogService } from "@/services/catalog";
import { PlayerModal } from "@/components/media/PlayerModal";
import { ProgressBar } from "@/components/media/ProgressBar";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import type { Channel, MediaItem, WatchHistoryItem } from "@/types/media";

export const Route = createFileRoute("/_authenticated/history")({
  component: HistoryPage,
});

function formatRelativeTime(dateString: string) {
  const date = new Date(dateString);
  if (Number.isNaN(date.getTime())) return "";
  const diffMs = Date.now() - date.getTime();
  const diffMinutes = Math.floor(diffMs / 60000);
  if (diffMinutes < 60) return `ha ${diffMinutes} minutos`;
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `ha ${diffHours} horas`;
  if (diffHours < 48) return "ontem";
  const diffDays = Math.floor(diffHours / 24);
  return `ha ${diffDays} dias`;
}

export function HistoryPage() {
  const queryClient = useQueryClient();
  const { data = [], isLoading } = useHistory();
  const [playing, setPlaying] = useState<MediaItem | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const removeMutation = useMutation({
    mutationFn: (channelId: string) => catalogService.removeFromHistory(channelId),
    onMutate: async (channelId) => {
      await queryClient.cancelQueries({ queryKey: ["history"] });
      const previous = queryClient.getQueryData<WatchHistoryItem[]>(["history"]);
      queryClient.setQueryData<WatchHistoryItem[]>(["history"], (old = []) =>
        old.filter(
          (item) => (item.channel.id ?? item.channel.streamUrl) !== channelId,
        ),
      );
      return { previous };
    },
    onError: (_error, _channelId, context) => {
      if (context?.previous) {
        queryClient.setQueryData(["history"], context.previous);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["history"] });
    },
  });

  const clearMutation = useMutation({
    mutationFn: () => catalogService.clearHistory(),
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: ["history"] });
      const previous = queryClient.getQueryData<WatchHistoryItem[]>(["history"]);
      queryClient.setQueryData<WatchHistoryItem[]>(["history"], []);
      return { previous };
    },
    onError: (_error, _vars, context) => {
      if (context?.previous) {
        queryClient.setQueryData(["history"], context.previous);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["history"] });
    },
  });

  const toMediaItem = (channel: Channel): MediaItem => ({
    id: channel.id ?? channel.streamUrl,
    title: channel.name,
    type: "channel",
    cover: channel.logoUrl,
    streamUrl: channel.streamUrl,
    description: channel.groupTitle,
  });

  const items = useMemo(() => {
    return [...data].sort(
      (a, b) => new Date(b.watchedAt).getTime() - new Date(a.watchedAt).getTime(),
    );
  }, [data]);

  return (
    <div className="pt-24 pb-12 px-4 md:px-12">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-3xl font-bold text-foreground">Historico</h1>
        {items.length > 0 && (
          <Button variant="secondary" onClick={() => setConfirmOpen(true)}>
            Limpar tudo
          </Button>
        )}
      </div>

      {isLoading ? (
        <div className="space-y-4">
          {Array.from({ length: 4 }).map((_, index) => (
            <div
              key={`loading-${index}`}
              className="h-20 rounded-lg bg-card animate-pulse"
            />
          ))}
        </div>
      ) : items.length ? (
        <div className="space-y-4">
          {items.map((item) => {
            const channelId = item.channel.id ?? item.channel.streamUrl;
            const progressRaw = localStorage.getItem(`playback:${channelId}`);
            const progressSeconds = progressRaw ? Number(progressRaw) : 0;
            const progress =
              item.channel.duration && item.channel.duration > 0
                ? Math.min(1, progressSeconds / item.channel.duration)
                : 0;
            return (
              <div
                key={channelId}
                className="flex flex-col gap-2 rounded-lg border border-border bg-card p-4 hover:border-primary/40 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <button
                    onClick={() => setPlaying(toMediaItem(item.channel))}
                    className="flex items-center gap-4 flex-1 text-left"
                  >
                    <img
                      src={item.channel.logoUrl}
                      alt={item.channel.name}
                      className="h-10 w-10 rounded-full object-cover"
                    />
                    <div className="flex-1">
                      <p className="font-semibold text-foreground">
                        {item.channel.name}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {item.channel.groupTitle}
                      </p>
                    </div>
                  </button>
                  <span className="text-xs text-muted-foreground">
                    {formatRelativeTime(item.watchedAt)}
                  </span>
                  <button
                    onClick={() =>
                      channelId && removeMutation.mutate(String(channelId))
                    }
                    className="text-muted-foreground hover:text-foreground"
                    aria-label="Remover do histórico"
                  >
                    <Trash className="h-4 w-4" />
                  </button>
                </div>
                {progress > 0 && <ProgressBar progress={progress} />}
              </div>
            );
          })}
        </div>
      ) : (
        <div className="text-center py-16">
          <Clock className="h-16 w-16 text-muted-foreground/30 mx-auto mb-4" />
          <p className="text-muted-foreground">Nenhum conteudo assistido ainda</p>
        </div>
      )}

      <PlayerModal
        item={playing}
        open={!!playing}
        onClose={() => setPlaying(null)}
      />

      <Dialog open={confirmOpen} onOpenChange={setConfirmOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Limpar histórico</DialogTitle>
            <DialogDescription>
              Tem certeza que deseja remover todo o histórico?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="secondary" onClick={() => setConfirmOpen(false)}>
              Cancelar
            </Button>
            <Button
              variant="destructive"
              onClick={() => {
                clearMutation.mutate();
                setConfirmOpen(false);
              }}
            >
              Limpar tudo
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
