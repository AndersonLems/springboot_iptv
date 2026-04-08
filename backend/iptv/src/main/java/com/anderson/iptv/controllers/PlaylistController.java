package com.anderson.iptv.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.services.PlaylistService;

import lombok.RequiredArgsConstructor;
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
    public Mono<List<Channel>> channels() {
        return Mono.fromCallable(() -> service.getPlaylist().getChannels())
                .subscribeOn(Schedulers.boundedElastic());
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
}
