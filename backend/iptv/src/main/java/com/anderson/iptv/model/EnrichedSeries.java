package com.anderson.iptv.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnrichedSeries {

    private Long tmdbId;
    private String name;
    private String overview;
    private String firstAirDate;
    private double voteAverage;
    private int voteCount;
    private double popularity;
    private String posterPath;
    private List<Integer> genreIds;
    private List<StreamOption> streams;
    private boolean available;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamOption {
        private String name;
        private String streamUrl;
        private String groupTitle;
        private String logoUrl;
        private StreamQuality quality;
        private Integer season;
        private Integer episode;
    }

    public enum StreamQuality {
        UHD_4K,
        HDR,
        LEGENDADO,
        DUBLADO,
        OUTRO
    }
}