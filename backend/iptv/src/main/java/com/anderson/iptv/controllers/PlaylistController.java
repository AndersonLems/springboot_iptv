package com.anderson.iptv.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.server.reactive.ServerHttpResponse;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.services.PlaylistService;
import com.anderson.iptv.model.CategoryGroup;


import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/playlist")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService service;

    @GetMapping
    public Mono<Playlist> full() {
        return Mono.fromCallable(service::getPlaylist)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/channels")
    public Flux<Channel> channels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            ServerHttpResponse response) {
        return Flux.defer(() -> Mono.fromCallable(() -> service.getPlaylist().getChannels())
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(list -> {
                    int total = list.size();
                    int safeSize = Math.max(1, size);
                    int from = Math.min(page * safeSize, total);
                    int to = Math.min(from + safeSize, total);
                    response.getHeaders().add("X-Total-Count", String.valueOf(total));
                    response.getHeaders().add("X-Page", String.valueOf(page));
                    response.getHeaders().add("X-Page-Size", String.valueOf(safeSize));
                    return Flux.fromIterable(list.subList(from, to));
                }));
    }

    @GetMapping("/channels/all")
    public Flux<Channel> allChannels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            ServerHttpResponse response) {
        return channels(page, size, response);
    }

    @GetMapping("/groups")
    public Mono<List<String>> groups() {
        return Mono.fromCallable(service::getGroups)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/channels/group/{group}")
    public Mono<List<Channel>> byGroup(@PathVariable String group) {
        return Mono.fromCallable(() -> service.byGroup(group))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/channels/search")
    public Mono<List<Channel>> search(@RequestParam("q") String query) {
        return Mono.fromCallable(() -> service.search(query))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/refresh")
    public Mono<Playlist> refresh() {
        return Mono.fromCallable(service::forceRefresh)
                .subscribeOn(Schedulers.boundedElastic());
    }
    @GetMapping("/categories")
    public Mono<List<CategoryGroup>> categories() {
    return Mono.fromCallable(service::getGroupedCategories)
            .subscribeOn(Schedulers.boundedElastic());
}

    @GetMapping("/categories/{parent}")
    public Mono<List<Channel>> byParentCategory(@PathVariable String parent) {
        return Mono.fromCallable(() -> service.getChannelsByCategory(parent, null))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/categories/{parent}/{sub}")
    public Mono<List<Channel>> bySubCategory(@PathVariable String parent,
                                              @PathVariable String sub) {
        return Mono.fromCallable(() -> service.getChannelsByCategory(parent, sub))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/categories/{parent}/search")
    public Mono<List<Channel>> searchInCategory(@PathVariable String parent,
                                               @RequestParam("q") String query) {
        return Mono.fromCallable(() -> service.searchChannelsByCategory(parent, query))
                .subscribeOn(Schedulers.boundedElastic());
    }

}
