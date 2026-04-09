export type MediaType = "movie" | "series" | "channel";

export interface MediaStreamOption {
  name?: string;
  streamUrl: string;
  groupTitle?: string;
  logoUrl?: string;
  quality?: string;
}

export interface MediaItem {
  id: string;
  title: string;
  cover: string;
  backdrop?: string;
  streamUrl: string;
  streamOptions?: MediaStreamOption[];
  type: MediaType;
  description?: string;
  year?: number;
  rating?: string;
  duration?: string;
  genres?: string[];
  seasons?: number;
  episodes?: number;
}

export interface Category {
  name: string;
  items: MediaItem[];
}

export interface User {
  id: string;
  username: string;
  displayName: string;
  avatar: string;
  email: string;
}

export interface AuthSession {
  user: User;
  token: string;
}

export interface HeroContent {
  item: MediaItem;
  tagline?: string;
}

export interface HomeFeed {
  hero: HeroContent;
  categories: Category[];
}
