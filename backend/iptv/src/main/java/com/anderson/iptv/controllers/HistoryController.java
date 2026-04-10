package com.anderson.iptv.controllers;

import com.anderson.iptv.model.WatchHistoryItem;
import com.anderson.iptv.services.HistoryService;

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
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @PostMapping("/{channelId}")
    public Mono<Void> record(@RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @PathVariable String channelId) {
        String id = requireDeviceId(deviceId);
        return Mono.fromRunnable(() -> historyService.recordWatch(id, channelId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @GetMapping
    public Mono<List<WatchHistoryItem>> list(@RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        String id = requireDeviceId(deviceId);
        return Mono.fromCallable(() -> historyService.getHistory(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @DeleteMapping
    public Mono<Void> clear(@RequestHeader(value = "X-Device-Id", required = false) String deviceId) {
        String id = requireDeviceId(deviceId);
        return Mono.fromRunnable(() -> historyService.clearHistory(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @DeleteMapping("/{channelId}")
    public Mono<Void> remove(@RequestHeader(value = "X-Device-Id", required = false) String deviceId,
            @PathVariable String channelId) {
        String id = requireDeviceId(deviceId);
        return Mono.fromRunnable(() -> historyService.removeFromHistory(id, channelId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private String requireDeviceId(String deviceId) {
        if (deviceId == null || deviceId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing X-Device-Id");
        }
        return deviceId;
    }
}
