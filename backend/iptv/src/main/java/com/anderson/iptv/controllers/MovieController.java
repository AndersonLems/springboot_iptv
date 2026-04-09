package com.anderson.iptv.controllers;

import com.anderson.iptv.model.EnrichedMovie;
import com.anderson.iptv.services.MovieMatcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieMatcherService matcherService;

    @GetMapping("/trending")
    public Mono<List<EnrichedMovie>> trending() {
        return matcherService.trendingWithStreams();
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
}