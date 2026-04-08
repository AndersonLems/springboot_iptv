import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MediaCard } from '@/components/media/MediaCard';
import type { MediaItem } from '@/types/media';

const mockItem: MediaItem = {
  id: 'm1',
  title: 'Teste Filme',
  type: 'movie',
  cover: 'https://example.com/cover.jpg',
  streamUrl: 'https://example.com/stream.m3u8',
  description: 'Descrição do filme de teste',
  year: 2024,
  rating: '8.5',
  genres: ['Ação'],
};

describe('MediaCard', () => {
  it('deve renderizar a imagem com alt text', () => {
    render(<MediaCard item={mockItem} onPlay={vi.fn()} />);
    const img = screen.getByAltText('Teste Filme');
    expect(img).toBeInTheDocument();
  });

  it('deve exibir título', () => {
    render(<MediaCard item={mockItem} onPlay={vi.fn()} />);
    expect(screen.getByText('Teste Filme')).toBeInTheDocument();
  });

  it('deve exibir ano e rating', () => {
    render(<MediaCard item={mockItem} onPlay={vi.fn()} />);
    expect(screen.getByText('2024')).toBeInTheDocument();
    expect(screen.getByText('★ 8.5')).toBeInTheDocument();
  });

  it('deve chamar onPlay ao clicar no card', async () => {
    const onPlay = vi.fn();
    const user = userEvent.setup();
    render(<MediaCard item={mockItem} onPlay={onPlay} />);
    await user.click(screen.getByAltText('Teste Filme'));
    expect(onPlay).toHaveBeenCalledWith(mockItem);
  });

  it('deve exibir botão de lista quando onToggleList é passado', () => {
    render(<MediaCard item={mockItem} onPlay={vi.fn()} onToggleList={vi.fn()} />);
    expect(screen.getByRole('button')).toBeInTheDocument();
  });
});
