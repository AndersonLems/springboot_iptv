import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ThemeToggle } from '@/components/ui/theme-toggle';

describe('ThemeToggle', () => {
  it('deve renderizar o botão', () => {
    render(<ThemeToggle />);
    const button = screen.getByRole('button');
    expect(button).toBeInTheDocument();
  });

  it('deve ter aria-label', () => {
    render(<ThemeToggle />);
    const button = screen.getByRole('button');
    expect(button).toHaveAttribute('aria-label');
  });

  it('deve alternar ao clicar', async () => {
    const user = userEvent.setup();
    render(<ThemeToggle />);
    const button = screen.getByRole('button');
    const initialLabel = button.getAttribute('aria-label');
    await user.click(button);
    expect(button.getAttribute('aria-label')).not.toBe(initialLabel);
  });
});
