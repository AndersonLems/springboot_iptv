package com.anderson.iptv.controllers;

import com.anderson.iptv.model.EpgProgram;
import com.anderson.iptv.services.EpgService;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/epg")
@RequiredArgsConstructor
public class EpgController {

    private final EpgService epgService;

    @GetMapping("/channel/{channelId}/schedule")
    public Mono<List<EpgProgram>> schedule(@PathVariable String channelId) {
        return Mono.fromCallable(() -> epgService.getSchedule(channelId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/channel/{channelId}/now")
    public Mono<EpgProgram> now(@PathVariable String channelId) {
        return Mono.fromCallable(() -> epgService.getNow(channelId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/channel/{channelId}/next")
    public Mono<EpgProgram> next(@PathVariable String channelId) {
        return Mono.fromCallable(() -> epgService.getNext(channelId))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
