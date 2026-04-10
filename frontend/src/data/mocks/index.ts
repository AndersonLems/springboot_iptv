import type {
  MediaItem,
  Category,
  User,
  HomeFeed,
  HeroContent,
} from "@/types/media";

const STREAM_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";

export const mockUser: User = {
  id: "u1",
  username: "admin",
  displayName: "Admin",
  avatar: "https://picsum.photos/seed/avatar1/200/200",
  email: "admin@streamapp.com",
};

export const mockMovies: MediaItem[] = [
  {
    id: "m1",
    title: "Horizonte Perdido",
    type: "movie",
    cover: "https://picsum.photos/seed/movie1/400/600",
    backdrop: "https://picsum.photos/seed/bd1/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Uma jornada épica através de terras desconhecidas em busca de redenção e propósito.",
    year: 2024,
    rating: "8.5",
    duration: "2h 15min",
    genres: ["Aventura", "Drama"],
  },
  {
    id: "m2",
    title: "Código Sombrio",
    type: "movie",
    cover: "https://picsum.photos/seed/movie2/400/600",
    backdrop: "https://picsum.photos/seed/bd2/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Um hacker descobre uma conspiração global que pode mudar o futuro da humanidade.",
    year: 2024,
    rating: "7.8",
    duration: "1h 58min",
    genres: ["Ação", "Thriller"],
  },
  {
    id: "m3",
    title: "Ecos do Passado",
    type: "movie",
    cover: "https://picsum.photos/seed/movie3/400/600",
    backdrop: "https://picsum.photos/seed/bd3/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Memórias esquecidas ressurgem para revelar verdades perturbadoras sobre uma família.",
    year: 2023,
    rating: "8.2",
    duration: "2h 05min",
    genres: ["Drama", "Mistério"],
  },
  {
    id: "m4",
    title: "Tempestade de Fogo",
    type: "movie",
    cover: "https://picsum.photos/seed/movie4/400/600",
    backdrop: "https://picsum.photos/seed/bd4/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Bombeiros enfrentam o maior incêndio florestal da história do país.",
    year: 2024,
    rating: "7.5",
    duration: "1h 52min",
    genres: ["Ação", "Drama"],
  },
  {
    id: "m5",
    title: "Noite Eterna",
    type: "movie",
    cover: "https://picsum.photos/seed/movie5/400/600",
    backdrop: "https://picsum.photos/seed/bd5/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Em um mundo onde o sol desapareceu, a humanidade luta para sobreviver na escuridão perpétua.",
    year: 2024,
    rating: "8.7",
    duration: "2h 20min",
    genres: ["Ficção Científica", "Thriller"],
  },
  {
    id: "m6",
    title: "A Última Dança",
    type: "movie",
    cover: "https://picsum.photos/seed/movie6/400/600",
    backdrop: "https://picsum.photos/seed/bd6/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Uma bailarina enfrenta seus medos para realizar o espetáculo mais importante de sua vida.",
    year: 2023,
    rating: "8.0",
    duration: "1h 45min",
    genres: ["Drama", "Romance"],
  },
  {
    id: "m7",
    title: "Operação Fantasma",
    type: "movie",
    cover: "https://picsum.photos/seed/movie7/400/600",
    backdrop: "https://picsum.photos/seed/bd7/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Uma equipe de elite é enviada em uma missão impossível atrás das linhas inimigas.",
    year: 2024,
    rating: "7.9",
    duration: "2h 10min",
    genres: ["Ação", "Espionagem"],
  },
  {
    id: "m8",
    title: "Sussurros na Floresta",
    type: "movie",
    cover: "https://picsum.photos/seed/movie8/400/600",
    backdrop: "https://picsum.photos/seed/bd8/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Uma família se muda para uma casa isolada e descobre segredos sombrios entre as árvores.",
    year: 2023,
    rating: "7.3",
    duration: "1h 48min",
    genres: ["Terror", "Suspense"],
  },
  {
    id: "m9",
    title: "Velocidade Máxima",
    type: "movie",
    cover: "https://picsum.photos/seed/movie9/400/600",
    backdrop: "https://picsum.photos/seed/bd9/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Corridas ilegais, traição e adrenalina nas ruas de São Paulo.",
    year: 2024,
    rating: "7.1",
    duration: "1h 55min",
    genres: ["Ação", "Crime"],
  },
  {
    id: "m10",
    title: "Entre Mundos",
    type: "movie",
    cover: "https://picsum.photos/seed/movie10/400/600",
    backdrop: "https://picsum.photos/seed/bd10/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Um cientista descobre uma porta para universos paralelos com consequências devastadoras.",
    year: 2024,
    rating: "8.4",
    duration: "2h 25min",
    genres: ["Ficção Científica", "Aventura"],
  },
  {
    id: "m11",
    title: "Amor em Paris",
    type: "movie",
    cover: "https://picsum.photos/seed/movie11/400/600",
    backdrop: "https://picsum.photos/seed/bd11/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Dois estranhos se encontram na Cidade Luz e descobrem o verdadeiro significado do amor.",
    year: 2023,
    rating: "7.6",
    duration: "1h 42min",
    genres: ["Romance", "Comédia"],
  },
  {
    id: "m12",
    title: "O Último Guardião",
    type: "movie",
    cover: "https://picsum.photos/seed/movie12/400/600",
    backdrop: "https://picsum.photos/seed/bd12/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Um guerreiro solitário protege uma relíquia antiga de forças malignas que ameaçam o mundo.",
    year: 2024,
    rating: "8.1",
    duration: "2h 30min",
    genres: ["Fantasia", "Ação"],
  },
];

export const mockSeries: MediaItem[] = [
  {
    id: "s1",
    title: "Império Digital",
    type: "series",
    cover: "https://picsum.photos/seed/series1/400/600",
    backdrop: "https://picsum.photos/seed/sbd1/1280/720",
    streamUrl: STREAM_URL,
    description:
      "A ascensão e queda de um império tecnológico que moldou uma geração.",
    year: 2024,
    rating: "9.0",
    genres: ["Drama", "Tecnologia"],
    seasons: 3,
    episodes: 24,
  },
  {
    id: "s2",
    title: "Detetive Sombra",
    type: "series",
    cover: "https://picsum.photos/seed/series2/400/600",
    backdrop: "https://picsum.photos/seed/sbd2/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Um detetive com um passado sombrio investiga crimes aparentemente inexplicáveis.",
    year: 2023,
    rating: "8.8",
    genres: ["Crime", "Mistério"],
    seasons: 2,
    episodes: 16,
  },
  {
    id: "s3",
    title: "Fronteira Selvagem",
    type: "series",
    cover: "https://picsum.photos/seed/series3/400/600",
    backdrop: "https://picsum.photos/seed/sbd3/1280/720",
    streamUrl: STREAM_URL,
    description: "Sobrevivência e drama intenso no sertão brasileiro.",
    year: 2024,
    rating: "8.5",
    genres: ["Western", "Drama"],
    seasons: 1,
    episodes: 8,
  },
  {
    id: "s4",
    title: "Rede de Mentiras",
    type: "series",
    cover: "https://picsum.photos/seed/series4/400/600",
    backdrop: "https://picsum.photos/seed/sbd4/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Uma jornalista descobre uma rede de corrupção que vai até o topo do poder.",
    year: 2024,
    rating: "8.3",
    genres: ["Thriller", "Drama"],
    seasons: 2,
    episodes: 20,
  },
  {
    id: "s5",
    title: "Criaturas da Noite",
    type: "series",
    cover: "https://picsum.photos/seed/series5/400/600",
    backdrop: "https://picsum.photos/seed/sbd5/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Vampiros modernos tentam se integrar à sociedade humana em uma metrópole.",
    year: 2023,
    rating: "7.9",
    genres: ["Fantasia", "Terror"],
    seasons: 3,
    episodes: 30,
  },
  {
    id: "s6",
    title: "Destinos Cruzados",
    type: "series",
    cover: "https://picsum.photos/seed/series6/400/600",
    backdrop: "https://picsum.photos/seed/sbd6/1280/720",
    streamUrl: STREAM_URL,
    description:
      "Cinco desconhecidos descobrem que seus destinos estão misteriosamente interligados.",
    year: 2024,
    rating: "8.6",
    genres: ["Drama", "Mistério"],
    seasons: 1,
    episodes: 10,
  },
];

export const mockChannels: MediaItem[] = [
  {
    id: "c1",
    title: "Stream News 24h",
    type: "channel",
    cover: "https://picsum.photos/seed/ch1/400/300",
    streamUrl: STREAM_URL,
    description: "Notícias ao vivo 24 horas por dia, 7 dias por semana.",
    genres: ["Notícias"],
  },
  {
    id: "c2",
    title: "Sports Live",
    type: "channel",
    cover: "https://picsum.photos/seed/ch2/400/300",
    streamUrl: STREAM_URL,
    description: "Os melhores eventos esportivos ao vivo.",
    genres: ["Esportes"],
  },
  {
    id: "c3",
    title: "Music TV",
    type: "channel",
    cover: "https://picsum.photos/seed/ch3/400/300",
    streamUrl: STREAM_URL,
    description: "Clipes, shows ao vivo e festivais de música.",
    genres: ["Música"],
  },
  {
    id: "c4",
    title: "Kids Channel",
    type: "channel",
    cover: "https://picsum.photos/seed/ch4/400/300",
    streamUrl: STREAM_URL,
    description: "Programação infantil educativa e divertida.",
    genres: ["Infantil"],
  },
  {
    id: "c5",
    title: "Discovery Stream",
    type: "channel",
    cover: "https://picsum.photos/seed/ch5/400/300",
    streamUrl: STREAM_URL,
    description: "Documentários e exploração do mundo.",
    genres: ["Documentário"],
  },
  {
    id: "c6",
    title: "Cinema Clássico",
    type: "channel",
    cover: "https://picsum.photos/seed/ch6/400/300",
    streamUrl: STREAM_URL,
    description: "Filmes clássicos que marcaram gerações.",
    genres: ["Cinema"],
  },
];

const allMedia: MediaItem[] = [...mockMovies, ...mockSeries];

export const mockCategories: Category[] = [
  {
    name: "Em Alta",
    items: [
      mockMovies[4],
      mockSeries[0],
      mockMovies[0],
      mockSeries[5],
      mockMovies[9],
      mockMovies[11],
    ],
  },
  {
    name: "Ação & Aventura",
    items: allMedia.filter((i) =>
      i.genres?.some((g) => ["Ação", "Aventura"].includes(g)),
    ),
  },
  { name: "Drama", items: allMedia.filter((i) => i.genres?.includes("Drama")) },
  {
    name: "Ficção Científica & Fantasia",
    items: allMedia.filter((i) =>
      i.genres?.some((g) => ["Ficção Científica", "Fantasia"].includes(g)),
    ),
  },
  {
    name: "Terror & Suspense",
    items: allMedia.filter((i) =>
      i.genres?.some((g) => ["Terror", "Suspense", "Mistério"].includes(g)),
    ),
  },
  {
    name: "Romance & Comédia",
    items: allMedia.filter((i) =>
      i.genres?.some((g) => ["Romance", "Comédia"].includes(g)),
    ),
  },
  { name: "Séries Populares", items: mockSeries },
];

export const mockHero: HeroContent = {
  item: mockMovies[4],
  tagline: "O filme mais aguardado do ano",
};

export const mockHomeFeed: HomeFeed = {
  hero: mockHero,
  categories: mockCategories,
};
