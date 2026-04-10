package com.anderson.iptv.controllers;

import com.anderson.iptv.model.SearchResponse;
import com.anderson.iptv.services.GlobalSearchService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final GlobalSearchService searchService;

    @GetMapping
    public Mono<SearchResponse> search(
            @RequestParam("q") String query,
            @RequestParam(value = "types", required = false) String types) {
        return Mono.fromCallable(() -> searchService.search(query, types))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
