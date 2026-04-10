package com.anderson.iptv.model.tmdb;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TmdbSeries {
    private Long id;
    private String name;
    private String overview;

    @JsonProperty("first_air_date")
    private String firstAirDate;
    @JsonProperty("vote_average")
    private double voteAverage;
    @JsonProperty("vote_count")
    private int voteCount;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    @JsonProperty("popularity")
    private double popularity;
}