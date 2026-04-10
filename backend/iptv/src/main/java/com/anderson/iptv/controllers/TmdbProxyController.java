package com.anderson.iptv.controllers;

import com.anderson.iptv.client.TmdbClient;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/tmdb")
@RequiredArgsConstructor
public class TmdbProxyController {

    private final TmdbClient tmdbClient;

    @GetMapping("/movie/{id}")
    public Mono<JsonNode> movie(@PathVariable long id) {
        return Mono.fromCallable(() -> tmdbClient.movieDetails(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/movie/{id}/credits")
    public Mono<JsonNode> movieCredits(@PathVariable long id) {
        return Mono.fromCallable(() -> tmdbClient.movieCredits(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/movie/{id}/videos")
    public Mono<JsonNode> movieVideos(@PathVariable long id) {
        return Mono.fromCallable(() -> tmdbClient.movieVideos(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/series/{id}")
    public Mono<JsonNode> series(@PathVariable long id) {
        return Mono.fromCallable(() -> tmdbClient.seriesDetails(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/series/{id}/credits")
    public Mono<JsonNode> seriesCredits(@PathVariable long id) {
        return Mono.fromCallable(() -> tmdbClient.seriesCredits(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/series/{id}/videos")
    public Mono<JsonNode> seriesVideos(@PathVariable long id) {
        return Mono.fromCallable(() -> tmdbClient.seriesVideos(id))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
