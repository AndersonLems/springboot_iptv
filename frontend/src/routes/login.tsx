import { createFileRoute, redirect, useNavigate } from '@tanstack/react-router';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useAuth } from '@/hooks/use-auth';
import { Play } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useState } from 'react';

const loginSchema = z.object({
  username: z.string().min(1, 'Usuário é obrigatório'),
  password: z.string().min(1, 'Senha é obrigatória'),
});

type LoginForm = z.infer<typeof loginSchema>;

export const Route = createFileRoute('/login')({
  beforeLoad: () => {
    if (typeof window !== 'undefined') {
      const session = localStorage.getItem('stream_auth_session');
      if (session) throw redirect({ to: '/' });
    }
  },
  component: LoginPage,
});

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginForm) => {
    setError('');
    setLoading(true);
    try {
      await login(data.username, data.password);
      navigate({ to: '/' });
    } catch {
      setError('Usuário ou senha inválidos');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4 relative overflow-hidden">
      <div className="absolute inset-0 bg-gradient-to-br from-primary/10 via-background to-background" />
      <div className="absolute inset-0 opacity-20" style={{ backgroundImage: 'url(https://picsum.photos/seed/loginbg/1920/1080)', backgroundSize: 'cover', backgroundPosition: 'center' }} />
      <div className="absolute inset-0 bg-background/85" />

      <div className="relative w-full max-w-md bg-card/95 backdrop-blur-xl rounded-2xl p-8 shadow-2xl border border-border">
        <div className="flex items-center justify-center gap-2 mb-8">
          <Play className="h-9 w-9 text-primary fill-primary" />
          <span className="text-2xl font-bold text-foreground tracking-tight">StreamApp</span>
        </div>
        <h1 className="text-xl font-semibold text-foreground text-center mb-6">Entrar na sua conta</h1>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <Input
              placeholder="Usuário"
              {...register('username')}
              className="bg-secondary border-border text-foreground placeholder:text-muted-foreground"
            />
            {errors.username && <p className="text-xs text-destructive mt-1">{errors.username.message}</p>}
          </div>
          <div>
            <Input
              type="password"
              placeholder="Senha"
              {...register('password')}
              className="bg-secondary border-border text-foreground placeholder:text-muted-foreground"
            />
            {errors.password && <p className="text-xs text-destructive mt-1">{errors.password.message}</p>}
          </div>
          {error && <p className="text-sm text-destructive text-center">{error}</p>}
          <Button type="submit" className="w-full bg-primary text-primary-foreground hover:bg-primary/90" disabled={loading}>
            {loading ? 'Entrando...' : 'Entrar'}
          </Button>
        </form>
        <p className="text-xs text-muted-foreground text-center mt-6 opacity-70">Demo: admin / admin</p>
      </div>
    </div>
  );
}
