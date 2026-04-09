package com.anderson.iptv.services;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.EnrichedMovie;
import com.anderson.iptv.model.EnrichedMovie.StreamOption;
import com.anderson.iptv.model.EnrichedMovie.StreamQuality;
import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.model.tmdb.TmdbMovie;
import com.anderson.iptv.model.tmdb.TmdbPageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.text.Normalizer;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieMatcherService {

    private final TmdbService tmdbService;
    private final PlaylistService playlistService;

    private static final String FILMES_PARENT = "filmes";

    public Mono<List<EnrichedMovie>> trendingWithStreams() {
        return Mono.fromCallable(() -> {
            TmdbPageResult<TmdbMovie> tmdb = tmdbService.trendingMovies();
            Playlist playlist = playlistService.getPlaylist();
            List<Channel> movieChannels = filterMovieChannels(playlist);
            return enrich(tmdb.getResults(), movieChannels);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<EnrichedMovie>> topRatedWithStreams(int page) {
        return Mono.fromCallable(() -> {
            TmdbPageResult<TmdbMovie> tmdb = tmdbService.topRatedMovies(page);
            Playlist playlist = playlistService.getPlaylist();
            List<Channel> movieChannels = filterMovieChannels(playlist);
            return enrich(tmdb.getResults(), movieChannels);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<EnrichedMovie>> popularWithStreams(int page) {
        return Mono.fromCallable(() -> {
            TmdbPageResult<TmdbMovie> tmdb = tmdbService.popularMovies(page);
            Playlist playlist = playlistService.getPlaylist();
            List<Channel> movieChannels = filterMovieChannels(playlist);
            return enrich(tmdb.getResults(), movieChannels);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<EnrichedMovie> enrich(List<TmdbMovie> movies, List<Channel> channels) {
        return movies.stream()
                .map(movie -> {
                    List<StreamOption> streams = findStreams(movie.getTitle(), channels);
                    return toEnriched(movie, streams);
                })
                .toList();
    }

    private List<StreamOption> findStreams(String tmdbTitle, List<Channel> channels) {
        String normalizedTitle = normalize(tmdbTitle);
        if (normalizedTitle.length() < 4) {
            log.warn("Título muito curto após normalização, pulando match: '{}'", tmdbTitle);
            return List.of();
        }
        return channels.stream()
                .filter(c -> {
                    if (c.getName() == null) return false;
                    String normalizedChannel = normalize(c.getName());
                    return normalizedChannel.equals(normalizedTitle)
                            || normalizedChannel.startsWith(normalizedTitle + " ")
                            || normalizedChannel.startsWith(normalizedTitle + ":");
                })
                .map(this::toStreamOption)
                .toList();
    }

    private StreamOption toStreamOption(Channel c) {
        return StreamOption.builder()
                .name(c.getName())
                .streamUrl(c.getStreamUrl())
                .groupTitle(c.getGroupTitle())
                .logoUrl(c.getLogoUrl())
                .quality(inferQuality(c.getName()))
                .build();
    }

    private EnrichedMovie toEnriched(TmdbMovie movie, List<StreamOption> streams) {
        return EnrichedMovie.builder()
                .tmdbId((long) movie.getId())
                .title(movie.getTitle())
                .overview(movie.getOverview())
                .releaseDate(movie.getReleaseDate())
                .voteAverage(movie.getVoteAverage())
                .voteCount(movie.getVoteCount())
                .popularity(movie.getPopularity())
                .posterPath(movie.getPosterPath())
                .backdropPath(movie.getBackdropPath())
                .genreIds(movie.getGenreIds())
                .streams(streams)
                .available(!streams.isEmpty())
                .build();
    }

    private List<Channel> filterMovieChannels(Playlist playlist) {
        return playlist.getChannels().stream()
                .filter(c -> c.getGroupTitle() != null &&
                             c.getGroupTitle().toLowerCase().startsWith(FILMES_PARENT))
                .toList();
    }

    private String normalize(String input) {
        if (input == null) return "";
        String s = input.toLowerCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.replaceAll("\\[.*?\\]", "");
        s = s.replaceAll("\\(.*?\\)", "");
        s = s.replaceAll("\\b4k\\b", "");
        s = s.replaceAll("\\bhdr\\b", "");
        s = s.replaceAll("\\bdublado\\b", "");
        s = s.replaceAll("\\bdubbed\\b", "");
        s = s.replaceAll("\\blegenado\\b", "");
        s = s.replaceAll("\\blegendado\\b", "");
        s = s.replaceAll("[^a-z0-9\\s]", " ");
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    private StreamQuality inferQuality(String name) {
        if (name == null) return StreamQuality.OUTRO;
        String n = name.toUpperCase();
        if (n.contains("4K"))                             return StreamQuality.UHD_4K;
        if (n.contains("HDR"))                            return StreamQuality.HDR;
        if (n.contains("[L]") || n.contains("LEGENDADO")) return StreamQuality.LEGENDADO;
        if (n.contains("DUBLADO"))                        return StreamQuality.DUBLADO;
        return StreamQuality.DUBLADO;
    }
}