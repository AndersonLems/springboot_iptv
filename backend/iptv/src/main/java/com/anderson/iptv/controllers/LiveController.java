package com.anderson.iptv.controllers;

import com.anderson.iptv.model.LiveChannel;
import com.anderson.iptv.model.PaginatedResponse;
import com.anderson.iptv.services.LiveChannelService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/live")
@RequiredArgsConstructor
public class LiveController {

    private final LiveChannelService liveChannelService;

    @GetMapping
    public Mono<PaginatedResponse<LiveChannel>> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Mono.fromCallable(() -> liveChannelService.getLive(category, page, size))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
