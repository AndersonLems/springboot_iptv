package com.anderson.iptv.services;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.EnrichedSeries;
import com.anderson.iptv.model.EnrichedSeries.StreamOption;
import com.anderson.iptv.model.EnrichedSeries.StreamQuality;
import com.anderson.iptv.model.tmdb.TmdbSeries;
import com.anderson.iptv.model.tmdb.TmdbPageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.anderson.iptv.util.TextNormalizer;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeriesMatcherService {

    private final TmdbService tmdbService;
    private final PlaylistService playlistService;
    private final ChannelIndex channelIndex;

    private static final String SERIES_PARENT = "series";
    private static final Pattern SEASON_EPISODE = Pattern.compile("(?i)\\bS(\\d{1,2})E(\\d{1,2})\\b");

    @Cacheable(value = "iptv:series:trending", key = "#root.methodName", condition = "@cacheToggle.enabled()")
    public Mono<List<EnrichedSeries>> trendingWithStreams() {
        return Mono.fromCallable(() -> {
            TmdbPageResult<TmdbSeries> tmdb = tmdbService.trendingSeries();
            List<Channel> seriesChannels = filterSeriesChannels();
            return enrich(tmdb.getResults(), seriesChannels);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Cacheable(value = "iptv:series:top-rated", key = "#root.methodName + ':' + #page", condition = "@cacheToggle.enabled()")
    public Mono<List<EnrichedSeries>> topRatedWithStreams(int page) {
        return Mono.fromCallable(() -> {
            TmdbPageResult<TmdbSeries> tmdb = tmdbService.topRatedSeries(page);
            List<Channel> seriesChannels = filterSeriesChannels();
            return enrich(tmdb.getResults(), seriesChannels);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Cacheable(value = "iptv:series:popular", key = "#root.methodName + ':' + #page", condition = "@cacheToggle.enabled()")
    public Mono<List<EnrichedSeries>> popularWithStreams(int page) {
        return Mono.fromCallable(() -> {
            TmdbPageResult<TmdbSeries> tmdb = tmdbService.popularSeries(page);
            List<Channel> seriesChannels = filterSeriesChannels();
            return enrich(tmdb.getResults(), seriesChannels);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Cacheable(value = "iptv:series:playlist", condition = "@cacheToggle.enabled()")
    public Mono<List<EnrichedSeries>> playlistGrouped() {
        return Mono.fromCallable(() -> {
            List<Channel> seriesChannels = filterSeriesChannels();
            return groupBySeries(seriesChannels);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private List<EnrichedSeries> enrich(List<TmdbSeries> series, List<Channel> channels) {
        Map<String, List<Channel>> index = indexByNormalizedTitle(channels);
        return series.stream()
                .map(item -> {
                    List<StreamOption> streams = findStreams(item.getName(), index);
                    return toEnriched(item, streams);
                })
                .toList();
    }

    private List<EnrichedSeries> groupBySeries(List<Channel> channels) {
        Map<String, List<Channel>> byName = indexByNormalizedTitle(channels);

        return byName.values().stream()
                .map(this::toEnrichedFromPlaylist)
                .sorted(Comparator.comparing(EnrichedSeries::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private List<StreamOption> findStreams(String tmdbName, Map<String, List<Channel>> index) {
        String normalizedTitle = normalizeSeriesTitle(tmdbName);
        if (normalizedTitle.length() < 3) {
            log.warn("Título muito curto após normalização, pulando match: '{}'", tmdbName);
            return List.of();
        }
        return index.getOrDefault(normalizedTitle, List.of()).stream()
                .map(this::toStreamOption)
                .toList();
    }

    private Map<String, List<Channel>> indexByNormalizedTitle(List<Channel> channels) {
        return channels.stream()
                .filter(c -> c.getName() != null)
                .collect(Collectors.groupingBy(c -> normalizeSeriesTitle(c.getName())))
                .entrySet().stream()
                .filter(entry -> !entry.getKey().isBlank())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private EnrichedSeries toEnriched(TmdbSeries series, List<StreamOption> streams) {
        return EnrichedSeries.builder()
                .tmdbId(series.getId())
                .name(series.getName())
                .overview(series.getOverview())
                .firstAirDate(series.getFirstAirDate())
                .voteAverage(series.getVoteAverage())
                .voteCount(series.getVoteCount())
                .popularity(series.getPopularity())
                .posterPath(series.getPosterPath())
                .genreIds(series.getGenreIds())
                .streams(streams)
                .available(!streams.isEmpty())
                .build();
    }

    private EnrichedSeries toEnrichedFromPlaylist(List<Channel> group) {
        Channel base = group.get(0);
        List<StreamOption> streams = group.stream()
                .map(this::toStreamOption)
                .toList();

        return EnrichedSeries.builder()
                .tmdbId(null)
                .name(stripEpisodeInfo(base.getName()))
                .overview(null)
                .firstAirDate(null)
                .voteAverage(0)
                .voteCount(0)
                .popularity(0)
                .posterPath(null)
                .genreIds(List.of())
                .streams(streams)
                .available(!streams.isEmpty())
                .build();
    }

    private StreamOption toStreamOption(Channel c) {
        SeasonEpisode se = parseSeasonEpisode(c.getName());
        return StreamOption.builder()
                .name(c.getName())
                .streamUrl(c.getStreamUrl())
                .groupTitle(c.getGroupTitle())
                .logoUrl(c.getLogoUrl())
                .quality(inferQuality(c.getName()))
                .season(se.season)
                .episode(se.episode)
                .build();
    }

    private List<Channel> filterSeriesChannels() {
        Map<String, List<Channel>> byGroup = channelIndex.getIndexByGroup();
        List<Channel> result = new java.util.ArrayList<>();
        byGroup.forEach((group, list) -> {
            if (group != null && group.startsWith(SERIES_PARENT)) {
                result.addAll(list);
            }
        });
        if (!result.isEmpty()) {
            return result;
        }
        return playlistService.getPlaylist().getChannels();
    }

    private String normalizeSeriesTitle(String input) {
        return TextNormalizer.normalizeTitle(stripEpisodeInfo(input));
    }

    private String stripEpisodeInfo(String input) {
        if (input == null) {
            return "";
        }
        String stripped = SEASON_EPISODE.matcher(input).replaceAll("");
        stripped = stripped.replaceAll("(?i)\\bEP\\s*\\d{1,3}\\b", "");
        stripped = stripped.replaceAll("\\s+", " ").trim();
        return stripped;
    }

    private SeasonEpisode parseSeasonEpisode(String input) {
        if (input == null) {
            return new SeasonEpisode(null, null);
        }
        Matcher matcher = SEASON_EPISODE.matcher(input);
        if (!matcher.find()) {
            return new SeasonEpisode(null, null);
        }
        Integer season = Integer.valueOf(matcher.group(1));
        Integer episode = Integer.valueOf(matcher.group(2));
        return new SeasonEpisode(season, episode);
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
        return StreamQuality.OUTRO;
    }

    private record SeasonEpisode(Integer season, Integer episode) {}
}
