import { createFileRoute, Link } from "@tanstack/react-router";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Heart } from "lucide-react";
import { useFavorites } from "@/hooks/use-catalog";
import { catalogService } from "@/services/catalog";
import { MediaCard } from "@/components/media/MediaCard";
import { PlayerModal } from "@/components/media/PlayerModal";
import type { Channel, MediaItem } from "@/types/media";

export const Route = createFileRoute("/_authenticated/favorites")({
  component: FavoritesPage,
});

export function FavoritesPage() {
  const queryClient = useQueryClient();
  const { data = [], isLoading } = useFavorites();
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  const removeMutation = useMutation({
    mutationFn: (channelId: string) => catalogService.removeFavorite(channelId),
    onMutate: async (channelId) => {
      await queryClient.cancelQueries({ queryKey: ["favorites"] });
      const previous = queryClient.getQueryData<Channel[]>(["favorites"]);
      queryClient.setQueryData<Channel[]>(["favorites"], (old = []) =>
        old.filter(
          (channel) => (channel.id ?? channel.streamUrl) !== channelId,
        ),
      );
      return { previous };
    },
    onError: (_error, _channelId, context) => {
      if (context?.previous) {
        queryClient.setQueryData(["favorites"], context.previous);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["favorites"] });
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

  const favorites = data.map(toMediaItem);

  return (
    <div className="pt-24 pb-12 px-4 md:px-12">
      <h1 className="text-3xl font-bold text-foreground mb-8">
        Meus Favoritos
      </h1>

      {isLoading ? (
        <div className="grid gap-4 grid-cols-[repeat(auto-fill,minmax(160px,1fr))]">
          {Array.from({ length: 8 }).map((_, index) => (
            <div
              key={`loading-${index}`}
              className="aspect-[2/3] bg-card rounded-lg animate-pulse"
            />
          ))}
        </div>
      ) : favorites.length ? (
        <div className="grid gap-4 grid-cols-[repeat(auto-fill,minmax(160px,1fr))]">
          {favorites.map((item) => (
            <div key={item.id} className="relative group">
              <MediaCard item={item} onPlay={setPlaying} className="w-full" />
              <button
                onClick={(event) => {
                  event.stopPropagation();
                  if (item.id) removeMutation.mutate(item.id);
                }}
                className="absolute top-2 right-2 p-1.5 rounded-full bg-background/80 backdrop-blur-sm opacity-0 group-hover:opacity-100 transition-opacity text-foreground hover:bg-background border border-border"
                aria-label="Remover favorito"
              >
                <Heart className="h-4 w-4 text-red-500 fill-red-500" />
              </button>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-16">
          <Heart className="h-16 w-16 text-muted-foreground/30 mx-auto mb-4" />
          <p className="text-muted-foreground">Voce ainda nao tem favoritos</p>
          <Link
            to="/movies"
            className="text-sm text-primary hover:underline mt-2 inline-block"
          >
            Explorar filmes
          </Link>
        </div>
      )}

      <PlayerModal
        item={playing}
        open={!!playing}
        onClose={() => setPlaying(null)}
      />
    </div>
  );
}
