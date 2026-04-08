import { createFileRoute, redirect, Outlet } from '@tanstack/react-router';
import { AppShell } from '@/components/layout/AppShell';

export const Route = createFileRoute('/_authenticated')({
  beforeLoad: ({ location }) => {
    if (typeof window === 'undefined') return;
    const session = localStorage.getItem('stream_auth_session');
    if (!session) {
      throw redirect({ to: '/login' });
    }
  },
  component: AuthenticatedLayout,
});

function AuthenticatedLayout() {
  return (
    <AppShell>
      <Outlet />
    </AppShell>
  );
}
