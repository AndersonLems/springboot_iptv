package com.anderson.iptv.controllers;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.services.FavoritesService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoritesController {

    private final FavoritesService favoritesService;

    @PostMapping("/{channelId}")
    public Mono<Void> add(@RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @PathVariable String channelId) {
        String id = requireDeviceId(deviceId);
        return Mono.fromRunnable(() -> favoritesService.addFavorite(id, channelId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @DeleteMapping("/{channelId}")
    public Mono<Void> remove(@RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @PathVariable String channelId) {
        String id = requireDeviceId(deviceId);
        return Mono.fromRunnable(() -> favoritesService.removeFavorite(id, channelId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @GetMapping
    public Mono<List<Channel>> list(@RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        String id = requireDeviceId(deviceId);
        return Mono.fromCallable(() -> favoritesService.getFavorites(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private String requireDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing X-Device-Id");
        }
        return deviceId;
    }
}
