package com.anderson.iptv.model;

import com.anderson.iptv.model.tmdb.TmdbMovie;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class EnrichedMovie {

    private Long tmdbId;
    private String title;
    private String overview;
    private String releaseDate;
    private double voteAverage;
    private int voteCount;
    private double popularity;
    private String posterPath;
    private String backdropPath;
    private List<Integer> genreIds;
    private List<StreamOption> streams;
    private boolean available;

    @Data
    @Builder
    public static class StreamOption {
        private String name;
        private String streamUrl;
        private String groupTitle;
        private String logoUrl;
        private StreamQuality quality;
    }

    public enum StreamQuality {
        UHD_4K,
        HDR,
        LEGENDADO,
        DUBLADO,
        OUTRO
    }
}