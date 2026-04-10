package com.anderson.iptv.services;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.EnrichedMovie;
import com.anderson.iptv.model.EnrichedSeries;
import com.anderson.iptv.model.PaginatedResponse;
import com.anderson.iptv.util.StreamQualityResolver;
import com.anderson.iptv.util.TextNormalizer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BrowseCatalogService {

    private static final String MOVIES_PARENT = "filmes";
    private static final String SERIES_PARENT = "series";
    private static final Pattern SEASON_EPISODE = Pattern.compile("(?i)\\bS(\\d{1,2})E(\\d{1,2})\\b");

    private final ChannelIndex channelIndex;
    private final PlaylistService playlistService;

    public BrowseCatalogService(ChannelIndex channelIndex, PlaylistService playlistService) {
        this.channelIndex = channelIndex;
        this.playlistService = playlistService;
    }

    @Cacheable(value = "iptv:movies:all",
            key = "#page + ':' + #size + ':' + #sort + ':' + #order + ':' + (#group == null ? '' : #group)",
            condition = "@cacheToggle.enabled()")
    public PaginatedResponse<EnrichedMovie> moviesAll(int page, int size, String sort, String order, String group) {
        List<Channel> channels = resolveByGroupOrParent(group, MOVIES_PARENT);
        List<EnrichedMovie> all = groupMovies(channels);
        return paginateMovies(all, page, size, sort, order);
    }

    @Cacheable(value = "iptv:series:all",
            key = "#page + ':' + #size + ':' + #sort + ':' + #order + ':' + (#group == null ? '' : #group)",
            condition = "@cacheToggle.enabled()")
    public PaginatedResponse<EnrichedSeries> seriesAll(int page, int size, String sort, String order, String group) {
        List<Channel> channels = resolveByGroupOrParent(group, SERIES_PARENT);
        List<EnrichedSeries> all = groupSeries(channels);
        return paginateSeries(all, page, size, sort, order);
    }

    private List<Channel> resolveByGroupOrParent(String group, String parentPrefix) {
        if (group != null && !group.isBlank()) {
            return channelIndex.getByGroup(group);
        }
        Map<String, List<Channel>> byGroup = channelIndex.getIndexByGroup();
        List<Channel> result = new ArrayList<>();
        byGroup.forEach((key, list) -> {
            if (key != null && key.startsWith(parentPrefix)) {
                result.addAll(list);
            }
        });
        if (!result.isEmpty()) {
            return result;
        }
        return playlistService.getPlaylist().getChannels();
    }

    private PaginatedResponse<EnrichedMovie> paginateMovies(List<EnrichedMovie> items, int page, int size, String sort, String order) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(200, Math.max(1, size));
        SortField sortField = SortField.from(sort);
        SortOrder sortOrder = SortOrder.from(order);

        Comparator<EnrichedMovie> comparator = switch (sortField) {
            case NAME -> Comparator.comparing(EnrichedMovie::getTitle, String.CASE_INSENSITIVE_ORDER);
            case YEAR -> Comparator.comparing(m -> yearFromDate(m.getReleaseDate()));
            case VOTE_AVERAGE -> Comparator.comparing(EnrichedMovie::getVoteAverage);
            case POPULARITY -> Comparator.comparing(EnrichedMovie::getPopularity);
        };
        if (sortOrder == SortOrder.DESC) {
            comparator = comparator.reversed();
        }

        List<EnrichedMovie> sorted = items.stream().sorted(comparator).toList();
        int total = sorted.size();
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<EnrichedMovie> pageItems = sorted.subList(from, to);

        return PaginatedResponse.<EnrichedMovie>builder()
                .content(pageItems)
                .page(safePage)
                .size(safeSize)
                .totalElements(total)
                .totalPages((int) Math.ceil(total / (double) safeSize))
                .sort(sortField.value)
                .order(sortOrder.value)
                .build();
    }

    private PaginatedResponse<EnrichedSeries> paginateSeries(List<EnrichedSeries> items, int page, int size, String sort, String order) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(200, Math.max(1, size));
        SortField sortField = SortField.from(sort);
        SortOrder sortOrder = SortOrder.from(order);

        Comparator<EnrichedSeries> comparator = switch (sortField) {
            case NAME -> Comparator.comparing(EnrichedSeries::getName, String.CASE_INSENSITIVE_ORDER);
            case YEAR -> Comparator.comparing(s -> yearFromDate(s.getFirstAirDate()));
            case VOTE_AVERAGE -> Comparator.comparing(EnrichedSeries::getVoteAverage);
            case POPULARITY -> Comparator.comparing(EnrichedSeries::getPopularity);
        };
        if (sortOrder == SortOrder.DESC) {
            comparator = comparator.reversed();
        }

        List<EnrichedSeries> sorted = items.stream().sorted(comparator).toList();
        int total = sorted.size();
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);
        List<EnrichedSeries> pageItems = sorted.subList(from, to);

        return PaginatedResponse.<EnrichedSeries>builder()
                .content(pageItems)
                .page(safePage)
                .size(safeSize)
                .totalElements(total)
                .totalPages((int) Math.ceil(total / (double) safeSize))
                .sort(sortField.value)
                .order(sortOrder.value)
                .build();
    }

    private List<EnrichedMovie> groupMovies(List<Channel> channels) {
        Map<String, List<Channel>> byName = channels.stream()
                .filter(c -> c.getName() != null)
                .collect(Collectors.groupingBy(c -> TextNormalizer.normalizeTitle(c.getName())));

        return byName.entrySet().stream()
                .filter(entry -> !entry.getKey().isBlank())
                .map(Map.Entry::getValue)
                .map(group -> {
                    Channel base = group.get(0);
                    List<EnrichedMovie.StreamOption> streams = group.stream()
                            .map(c -> EnrichedMovie.StreamOption.builder()
                                    .name(c.getName())
                                    .streamUrl(c.getStreamUrl())
                                    .groupTitle(c.getGroupTitle())
                                    .logoUrl(c.getLogoUrl())
                                    .quality(StreamQualityResolver.movieQuality(c.getName()))
                                    .build())
                            .toList();
                    return EnrichedMovie.builder()
                            .tmdbId(null)
                            .title(base.getName())
                            .overview(null)
                            .releaseDate(null)
                            .voteAverage(0)
                            .voteCount(0)
                            .popularity(0)
                            .posterPath(null)
                            .backdropPath(null)
                            .genreIds(List.of())
                            .streams(streams)
                            .available(!streams.isEmpty())
                            .build();
                })
                .toList();
    }

    private List<EnrichedSeries> groupSeries(List<Channel> channels) {
        Map<String, List<Channel>> byName = channels.stream()
                .filter(c -> c.getName() != null)
                .collect(Collectors.groupingBy(c -> TextNormalizer.normalizeTitle(stripEpisodeInfo(c.getName()))));

        return byName.entrySet().stream()
                .filter(entry -> !entry.getKey().isBlank())
                .map(Map.Entry::getValue)
                .map(group -> {
                    Channel base = group.get(0);
                    List<EnrichedSeries.StreamOption> streams = group.stream()
                            .map(c -> {
                                SeasonEpisode se = parseSeasonEpisode(c.getName());
                                return EnrichedSeries.StreamOption.builder()
                                        .name(c.getName())
                                        .streamUrl(c.getStreamUrl())
                                        .groupTitle(c.getGroupTitle())
                                        .logoUrl(c.getLogoUrl())
                                        .quality(StreamQualityResolver.seriesQuality(c.getName()))
                                        .season(se.season)
                                        .episode(se.episode)
                                        .build();
                            })
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
                })
                .toList();
    }

    private int yearFromDate(String date) {
        if (date == null || date.length() < 4) {
            return 0;
        }
        try {
            return Integer.parseInt(date.substring(0, 4));
        } catch (Exception e) {
            return 0;
        }
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

    private enum SortField {
        NAME("name"),
        YEAR("year"),
        VOTE_AVERAGE("voteAverage"),
        POPULARITY("popularity");

        private final String value;

        SortField(String value) {
            this.value = value;
        }

        static SortField from(String raw) {
            if (raw == null || raw.isBlank()) {
                return NAME;
            }
            String key = raw.toLowerCase(Locale.ROOT);
            return switch (key) {
                case "name" -> NAME;
                case "year" -> YEAR;
                case "voteaverage" -> VOTE_AVERAGE;
                case "popularity" -> POPULARITY;
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort field");
            };
        }
    }

    private enum SortOrder {
        ASC("asc"),
        DESC("desc");

        private final String value;

        SortOrder(String value) {
            this.value = value;
        }

        static SortOrder from(String raw) {
            if (raw == null || raw.isBlank()) {
                return ASC;
            }
            String key = raw.toLowerCase(Locale.ROOT);
            return switch (key) {
                case "asc" -> ASC;
                case "desc" -> DESC;
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort order");
            };
        }
    }

    private record SeasonEpisode(Integer season, Integer episode) {}
}
