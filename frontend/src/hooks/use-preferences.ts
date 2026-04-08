import { useState, useCallback } from 'react';

function useLocalStorageList(key: string): [string[], (id: string) => void, (id: string) => void] {
  const [items, setItems] = useState<string[]>(() => {
    if (typeof window === 'undefined') return [];
    try {
      const saved = localStorage.getItem(key);
      return saved ? JSON.parse(saved) : [];
    } catch {
      return [];
    }
  });

  const add = useCallback((id: string) => {
    setItems(prev => {
      const next = [id, ...prev.filter(i => i !== id)];
      localStorage.setItem(key, JSON.stringify(next));
      return next;
    });
  }, [key]);

  const remove = useCallback((id: string) => {
    setItems(prev => {
      const next = prev.filter(i => i !== id);
      localStorage.setItem(key, JSON.stringify(next));
      return next;
    });
  }, [key]);

  return [items, add, remove];
}

export function useMyList() {
  return useLocalStorageList('stream_my_list');
}

export function useWatchHistory() {
  return useLocalStorageList('stream_watch_history');
}
