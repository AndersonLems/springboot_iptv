package com.anderson.iptv.services;

import com.anderson.iptv.client.TmdbClient;
import com.anderson.iptv.model.tmdb.TmdbMovie;
import com.anderson.iptv.model.tmdb.TmdbPageResult;
import com.anderson.iptv.model.tmdb.TmdbSeries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmdbService {

    private final TmdbClient client;

    public TmdbPageResult<TmdbMovie> trendingMovies() {
        return client.trendingMovies();
    }

    public TmdbPageResult<TmdbMovie> topRatedMovies(int page) {
        return client.topRatedMovies(page);
    }

    public TmdbPageResult<TmdbMovie> popularMovies(int page) {
        return client.popularMovies(page);
    }

    public TmdbPageResult<TmdbSeries> trendingSeries() {
        return client.trendingSeries();
    }

    public TmdbPageResult<TmdbSeries> topRatedSeries(int page) {
        return client.topRatedSeries(page);
    }
}
