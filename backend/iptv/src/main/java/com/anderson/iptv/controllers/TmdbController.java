package com.anderson.iptv.controllers;

import com.anderson.iptv.model.tmdb.TmdbMovie;
import com.anderson.iptv.model.tmdb.TmdbPageResult;
import com.anderson.iptv.model.tmdb.TmdbSeries;
import com.anderson.iptv.services.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/tmdb")
@RequiredArgsConstructor
public class TmdbController {

    private final TmdbService service;

    @GetMapping("/movies/trending")
    public Mono<TmdbPageResult<TmdbMovie>> moviesTrending() {
        return Mono.fromCallable(service::trendingMovies)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/movies/top-rated")
    public Mono<TmdbPageResult<TmdbMovie>> moviesTopRated(
            @RequestParam(defaultValue = "1") int page) {
        return Mono.fromCallable(() -> service.topRatedMovies(page))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/movies/popular")
    public Mono<TmdbPageResult<TmdbMovie>> moviesPopular(
            @RequestParam(defaultValue = "1") int page) {
        return Mono.fromCallable(() -> service.popularMovies(page))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/series/trending")
    public Mono<TmdbPageResult<TmdbSeries>> seriesTrending() {
        return Mono.fromCallable(service::trendingSeries)
                .subscribeOn(Schedulers.boundedElastic());
    }

        @GetMapping("/series/popular")
        public Mono<TmdbPageResult<TmdbSeries>> seriesPopular(
                        @RequestParam(defaultValue = "1") int page) {
                return Mono.fromCallable(() -> service.popularSeries(page))
                                .subscribeOn(Schedulers.boundedElastic());
        }

    @GetMapping("/series/top-rated")
    public Mono<TmdbPageResult<TmdbSeries>> seriesTopRated(
            @RequestParam(defaultValue = "1") int page) {
        return Mono.fromCallable(() -> service.topRatedSeries(page))
                .subscribeOn(Schedulers.boundedElastic());
    }
}