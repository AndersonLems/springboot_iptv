import { createFileRoute, useNavigate } from '@tanstack/react-router';
import { useAuth } from '@/hooks/use-auth';
import { useMyList, useWatchHistory } from '@/hooks/use-preferences';
import { MediaCarousel } from '@/components/media/MediaCarousel';
import { mockMovies, mockSeries } from '@/data/mocks';
import { Button } from '@/components/ui/button';
import { LogOut, User } from 'lucide-react';
import { PlayerModal } from '@/components/media/PlayerModal';
import { useState } from 'react';
import type { MediaItem } from '@/types/media';

export const Route = createFileRoute('/_authenticated/profile')({
  component: ProfilePage,
});

function ProfilePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [myListIds] = useMyList();
  const [historyIds] = useWatchHistory();
  const [playing, setPlaying] = useState<MediaItem | null>(null);

  const allMedia = [...mockMovies, ...mockSeries];
  const myListItems = allMedia.filter(i => myListIds.includes(i.id));
  const historyItems = allMedia.filter(i => historyIds.includes(i.id));

  const handleLogout = () => {
    logout();
    navigate({ to: '/login' });
  };

  return (
    <div className="pt-24 pb-12">
      <div className="px-4 md:px-12 mb-12">
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-6">
          <div className="w-20 h-20 rounded-full bg-primary/20 border-2 border-primary flex items-center justify-center">
            <User className="h-10 w-10 text-primary" />
          </div>
          <div className="flex-1">
            <h1 className="text-2xl font-bold text-foreground">{user?.displayName || 'Usuário'}</h1>
            <p className="text-muted-foreground">{user?.email}</p>
          </div>
          <Button variant="secondary" onClick={handleLogout} className="gap-2">
            <LogOut className="h-4 w-4" /> Sair
          </Button>
        </div>
      </div>
      {myListItems.length > 0 && (
        <MediaCarousel title="Minha Lista" items={myListItems} onPlay={setPlaying} />
      )}
      {historyItems.length > 0 && (
        <MediaCarousel title="Últimos Assistidos" items={historyItems} onPlay={setPlaying} />
      )}
      {myListItems.length === 0 && historyItems.length === 0 && (
        <div className="text-center py-16 px-4">
          <User className="h-16 w-16 text-muted-foreground/30 mx-auto mb-4" />
          <p className="text-muted-foreground">Você ainda não adicionou itens à sua lista ou assistiu conteúdo.</p>
          <p className="text-sm text-muted-foreground mt-2">Explore o catálogo e comece a assistir!</p>
        </div>
      )}
      <PlayerModal item={playing} open={!!playing} onClose={() => setPlaying(null)} />
    </div>
  );
}
