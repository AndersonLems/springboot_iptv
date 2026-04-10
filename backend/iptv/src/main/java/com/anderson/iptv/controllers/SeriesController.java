package com.anderson.iptv.controllers;

import com.anderson.iptv.model.EnrichedSeries;
import com.anderson.iptv.model.PaginatedResponse;
import com.anderson.iptv.services.BrowseCatalogService;
import com.anderson.iptv.services.SeriesMatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesMatcherService matcherService;
    private final BrowseCatalogService browseService;

    @GetMapping("/trending")
    public Mono<List<EnrichedSeries>> trending() {
        return matcherService.trendingWithStreams();
    }

    @GetMapping("/top-rated")
    public Mono<List<EnrichedSeries>> topRated(
            @RequestParam(defaultValue = "1") int page) {
        return matcherService.topRatedWithStreams(page);
    }

    @GetMapping("/popular")
    public Mono<List<EnrichedSeries>> popular(
            @RequestParam(defaultValue = "1") int page) {
        return matcherService.popularWithStreams(page);
    }

    @GetMapping("/playlist")
    public Mono<List<EnrichedSeries>> playlist() {
        return matcherService.playlistGrouped();
    }

    @GetMapping("/all")
    public Mono<PaginatedResponse<EnrichedSeries>> all(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(required = false) String group) {
        return Mono.fromCallable(() -> browseService.seriesAll(page, size, sort, order, group))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
