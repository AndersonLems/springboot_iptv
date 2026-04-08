import { Link } from '@tanstack/react-router';
import { Search, User, Menu, X, Play } from 'lucide-react';
import { useState, useEffect } from 'react';
import { ThemeToggle } from '@/components/ui/theme-toggle';

const navLinks = [
  { to: '/' as const, label: 'Início' },
  { to: '/series' as const, label: 'Séries' },
  { to: '/movies' as const, label: 'Filmes' },
  { to: '/channels' as const, label: 'Canais' },
];

export function Header() {
  const [scrolled, setScrolled] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  useEffect(() => {
    const handler = () => setScrolled(window.scrollY > 50);
    window.addEventListener('scroll', handler);
    return () => window.removeEventListener('scroll', handler);
  }, []);

  return (
    <header className={`fixed top-0 left-0 right-0 z-50 transition-all duration-300 ${scrolled ? 'bg-background/95 backdrop-blur-sm shadow-lg' : 'bg-gradient-to-b from-background/80 to-transparent'}`}>
      <div className="flex items-center justify-between px-4 md:px-8 h-16">
        <div className="flex items-center gap-8">
          <Link to="/" className="flex items-center gap-2">
            <Play className="h-7 w-7 text-primary fill-primary" />
            <span className="text-xl font-bold text-foreground tracking-tight">StreamApp</span>
          </Link>
          <nav className="hidden md:flex items-center gap-6">
            {navLinks.map(link => (
              <Link
                key={link.to}
                to={link.to}
                className="text-sm text-muted-foreground hover:text-foreground transition-colors"
                activeProps={{ className: 'text-sm text-foreground font-medium' }}
                activeOptions={{ exact: link.to === '/' }}
              >
                {link.label}
              </Link>
            ))}
          </nav>
        </div>
        <div className="flex items-center gap-3">
          <ThemeToggle />
          <Link to="/search" className="text-muted-foreground hover:text-foreground transition-colors">
            <Search className="h-5 w-5" />
          </Link>
          <Link to="/profile" className="text-muted-foreground hover:text-foreground transition-colors">
            <User className="h-5 w-5" />
          </Link>
          <button onClick={() => setMobileOpen(!mobileOpen)} className="md:hidden text-muted-foreground hover:text-foreground">
            {mobileOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>
      {mobileOpen && (
        <nav className="md:hidden bg-background/95 backdrop-blur-sm border-t border-border px-4 py-4 flex flex-col gap-3">
          {navLinks.map(link => (
            <Link
              key={link.to}
              to={link.to}
              onClick={() => setMobileOpen(false)}
              className="text-sm text-muted-foreground hover:text-foreground py-2"
              activeProps={{ className: 'text-sm text-foreground font-medium py-2' }}
            >
              {link.label}
            </Link>
          ))}
        </nav>
      )}
    </header>
  );
}
