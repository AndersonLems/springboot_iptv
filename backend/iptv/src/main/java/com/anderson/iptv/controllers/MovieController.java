package com.anderson.iptv.controllers;

import com.anderson.iptv.model.EnrichedMovie;
import com.anderson.iptv.model.EnrichedMovieListCache;
import com.anderson.iptv.model.PaginatedResponse;
import com.anderson.iptv.services.BrowseCatalogService;
import com.anderson.iptv.services.MovieMatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieMatcherService matcherService;
    private final BrowseCatalogService browseService;

    @GetMapping("/trending")
    public Mono<List<EnrichedMovie>> trending() {
        return matcherService.trendingWithStreams()
                .map(EnrichedMovieListCache::getMovies);
    }

    @GetMapping("/top-rated")
    public Mono<List<EnrichedMovie>> topRated(
            @RequestParam(defaultValue = "1") int page) {
        return matcherService.topRatedWithStreams(page);
    }

    @GetMapping("/popular")
    public Mono<List<EnrichedMovie>> popular(
            @RequestParam(defaultValue = "1") int page) {
        return matcherService.popularWithStreams(page);
    }

    @GetMapping("/all")
    public Mono<PaginatedResponse<EnrichedMovie>> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String group) {
        return Mono.fromCallable(() -> browseService.moviesAll(page, size, sort, order, group))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
