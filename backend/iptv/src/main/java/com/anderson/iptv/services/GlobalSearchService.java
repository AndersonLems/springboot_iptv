package com.anderson.iptv.services;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.SearchResponse;
import com.anderson.iptv.util.TextNormalizer;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GlobalSearchService {

    private static final int MAX_PER_TYPE = 20;

    private final ChannelIndex channelIndex;

    public GlobalSearchService(ChannelIndex channelIndex) {
        this.channelIndex = channelIndex;
    }

    @Cacheable(value = "iptv:search", key = "#query + ':' + (#types == null ? 'all' : #types)", condition = "@cacheToggle.enabled()")
    public SearchResponse search(String query, String types) {
        if (query == null || query.trim().length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query too short");
        }

        String normalizedQuery = TextNormalizer.normalizeTitle(query);
        EnumSet<SearchType> typeSet = SearchType.parse(types);

        List<Channel> movies = new ArrayList<>();
        List<Channel> series = new ArrayList<>();
        List<Channel> live = new ArrayList<>();

        Map<String, List<Channel>> index = channelIndex.getIndexByNormalizedName();
        for (Map.Entry<String, List<Channel>> entry : index.entrySet()) {
            if (!entry.getKey().contains(normalizedQuery)) {
                continue;
            }
            for (Channel c : entry.getValue()) {
                if (typeSet.contains(SearchType.MOVIES) && isMovie(c) && movies.size() < MAX_PER_TYPE) {
                    movies.add(c);
                }
                if (typeSet.contains(SearchType.SERIES) && isSeries(c) && series.size() < MAX_PER_TYPE) {
                    series.add(c);
                }
                if (typeSet.contains(SearchType.LIVE) && isLive(c) && live.size() < MAX_PER_TYPE) {
                    live.add(c);
                }
            }
            if (movies.size() >= MAX_PER_TYPE && series.size() >= MAX_PER_TYPE && live.size() >= MAX_PER_TYPE) {
                break;
            }
        }

        int total = movies.size() + series.size() + live.size();
        return SearchResponse.builder()
                .query(query)
                .movies(movies)
                .series(series)
                .live(live)
                .totalResults(total)
                .build();
    }

    private boolean isMovie(Channel c) {
        return c.getGroupTitle() != null && c.getGroupTitle().toLowerCase(Locale.ROOT).startsWith("filmes");
    }

    private boolean isSeries(Channel c) {
        return c.getGroupTitle() != null && c.getGroupTitle().toLowerCase(Locale.ROOT).startsWith("series");
    }

    private boolean isLive(Channel c) {
        return c.getGroupTitle() != null && c.getGroupTitle().toLowerCase(Locale.ROOT).startsWith("canais |");
    }

    private enum SearchType {
        MOVIES, SERIES, LIVE;

        static EnumSet<SearchType> parse(String raw) {
            if (raw == null || raw.isBlank()) {
                return EnumSet.allOf(SearchType.class);
            }
            EnumSet<SearchType> set = EnumSet.noneOf(SearchType.class);
            for (String part : raw.split(",")) {
                String key = part.trim().toLowerCase(Locale.ROOT);
                switch (key) {
                    case "movies" -> set.add(MOVIES);
                    case "series" -> set.add(SERIES);
                    case "live" -> set.add(LIVE);
                    default -> {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid search type");
                    }
                }
            }
            return set.isEmpty() ? EnumSet.allOf(SearchType.class) : set;
        }
    }
}
