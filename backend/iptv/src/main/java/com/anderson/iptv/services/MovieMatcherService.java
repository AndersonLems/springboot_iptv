package com.anderson.iptv.services;

import com.anderson.iptv.model.EnrichedMovie;
import com.anderson.iptv.model.EnrichedMovie.StreamOption;
import com.anderson.iptv.model.EnrichedMovie.StreamQuality;
import com.anderson.iptv.model.EnrichedMovieListCache;
import com.anderson.iptv.model.tmdb.TmdbMovie;
import com.anderson.iptv.model.tmdb.TmdbPageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.util.TextNormalizer;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieMatcherService {

    private final TmdbService tmdbService;
    private final PlaylistService playlistService;
    private final ChannelIndex channelIndex;

    private static final String FILMES_PARENT = "filmes";

    @Cacheable(value = "iptv:movies:trending", key = "#root.methodName", condition = "@cacheToggle.enabled()")
    public Mono<EnrichedMovieListCache> trendingWithStreams() {
        return Mono.fromCallable(() -> {
            TmdbPageResult<TmdbMovie> tmdb = tmdbService.trendingMovies();
            List<Channel> movieChannels = getMovieChannels();
            List<EnrichedMovie> movies = enrich(tmdb.getResults(), movieChannels)
                    .stream()
                    .filter(EnrichedMovie::isAvailable)
                    .toList();
            return new EnrichedMovieListCache(movies);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Cacheable(value = "iptv:movies:top-rated", key = "#root.methodName + ':' + #page", condition = "@cacheToggle.enabled()")
    public Mono<List<EnrichedMovie>> topRatedWithStreams(int page) {
        return Mono.fromCallable(() -> {
            TmdbPageResult<TmdbMovie> tmdb = tmdbService.topRatedMovies(page);
            List<Channel> movieChannels = getMovieChannels();
            return enrich(tmdb.getResults(), movieChannels).stream()
                    .filter(EnrichedMovie::isAvailable)
                    .toList();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Cacheable(value = "iptv:movies:popular", key = "#root.methodName + ':' + #page", condition = "@cacheToggle.enabled()")
    public Mono<List<EnrichedMovie>> popularWithStreams(int page) {
        return Mono.fromCallable(() -> {
            TmdbPageResult<TmdbMovie> tmdb = tmdbService.popularMovies(page);
            List<Channel> movieChannels = getMovieChannels();
            return enrich(tmdb.getResults(), movieChannels).stream()
                    .filter(EnrichedMovie::isAvailable)
                    .toList();
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
        String normalizedTitle = TextNormalizer.normalizeTitle(tmdbTitle);
        if (normalizedTitle.length() < 4) {
            log.warn("Título muito curto após normalização, pulando match: '{}'", tmdbTitle);
            return List.of();
        }
        Map<String, List<Channel>> index = channelIndex.getIndexByNormalizedName();
        List<Channel> exact = index.getOrDefault(normalizedTitle, List.of());
        Stream<Channel> prefixMatches = index.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(normalizedTitle + " ")
                        || entry.getKey().startsWith(normalizedTitle + ":"))
                .flatMap(entry -> entry.getValue().stream());

        return Stream.concat(exact.stream(), prefixMatches)
                .distinct()
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

    private List<Channel> getMovieChannels() {
        Map<String, List<Channel>> byGroup = channelIndex.getIndexByGroup();
        List<Channel> result = new java.util.ArrayList<>();
        byGroup.forEach((group, list) -> {
            if (group != null && group.startsWith(FILMES_PARENT)) {
                result.addAll(list);
            }
        });
        if (!result.isEmpty()) {
            return result;
        }
        return playlistService.getPlaylist().getChannels();
    }

    private StreamQuality inferQuality(String name) {
        if (name == null)
            return StreamQuality.OUTRO;
        String n = name.toUpperCase();
        if (n.contains("4K"))
            return StreamQuality.UHD_4K;
        if (n.contains("HDR"))
            return StreamQuality.HDR;
        if (n.contains("[L]") || n.contains("LEGENDADO"))
            return StreamQuality.LEGENDADO;
        if (n.contains("DUBLADO"))
            return StreamQuality.DUBLADO;
        return StreamQuality.DUBLADO;
    }
}
