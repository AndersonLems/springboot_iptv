package com.anderson.iptv.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.EnrichedMovie;
import com.anderson.iptv.model.EnrichedMovie.StreamQuality;
import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.model.tmdb.TmdbMovie;
import com.anderson.iptv.model.tmdb.TmdbPageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class MovieMatcherServiceTest {

    private TmdbService tmdbService;
    private PlaylistService playlistService;
    private ChannelIndex channelIndex;
    private MovieMatcherService matcherService;

    @BeforeEach
    void setup() {
        tmdbService = Mockito.mock(TmdbService.class);
        playlistService = Mockito.mock(PlaylistService.class);
        channelIndex = new ChannelIndex();
        matcherService = new MovieMatcherService(tmdbService, playlistService, channelIndex);
    }

    @Test
    void testTrendingWithStreams() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Avatar", "overview", "2020-01-01",
                8.1, 100, "/poster", "/backdrop", List.of(), "en", 10.0);
        when(tmdbService.trendingMovies()).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.trendingWithStreams().block().getMovies();
        assertEquals(1, enriched.size());
        assertTrue(enriched.get(0).isAvailable());
    }

    @Test
    void testNormalizationRemovesAccents() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Coracao Valente").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Coração Valente", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.topRatedMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.topRatedWithStreams(1).block();
        assertTrue(enriched.get(0).isAvailable());
    }

    @Test
    void testNormalizationRemovesBrackets() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar [4K]").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Avatar", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.popularMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.popularWithStreams(1).block();
        assertTrue(enriched.get(0).isAvailable());
    }

    @Test
    void testNormalizationRemoves4K() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar 4K").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Avatar", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.topRatedMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.topRatedWithStreams(1).block();
        assertTrue(enriched.get(0).isAvailable());
    }

    @Test
    void testStartsWithMatchPreventsfalsePositives() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Empregada").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Romance da Empregada", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.topRatedMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.topRatedWithStreams(1).block();
        assertTrue(enriched.get(0).getStreams().isEmpty());
    }

    @Test
    void testExactMatchWorks() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Avatar", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.topRatedMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.topRatedWithStreams(1).block();
        assertEquals(1, enriched.get(0).getStreams().size());
    }

    @Test
    void testStartsWithSpaceWorks() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar Fogo e Cinzas").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Avatar", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.topRatedMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.topRatedWithStreams(1).block();
        assertEquals(1, enriched.get(0).getStreams().size());
    }

    @Test
    void testShortTitleSkipped() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Up").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Up", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.topRatedMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.topRatedWithStreams(1).block();
        assertTrue(enriched.get(0).getStreams().isEmpty());
    }

    @Test
    void testInferQuality4K() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar 4K").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Avatar", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.popularMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.popularWithStreams(1).block();
        assertEquals(StreamQuality.UHD_4K, enriched.get(0).getStreams().get(0).getQuality());
    }

    @Test
    void testInferQualityLegendado() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar Legendado").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Avatar", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.popularMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.popularWithStreams(1).block();
        assertEquals(StreamQuality.LEGENDADO, enriched.get(0).getStreams().get(0).getQuality());
    }

    @Test
    void testInferQualityDublado() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar Dublado").groupTitle("Filmes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        TmdbMovie movie = new TmdbMovie(1, "Avatar", null, null,
                0, 0, null, null, List.of(), null, 0);
        when(tmdbService.popularMovies(1)).thenReturn(new TmdbPageResult<>(1, List.of(movie), 1, 1));

        List<EnrichedMovie> enriched = matcherService.popularWithStreams(1).block();
        assertEquals(StreamQuality.DUBLADO, enriched.get(0).getStreams().get(0).getQuality());
    }
}
