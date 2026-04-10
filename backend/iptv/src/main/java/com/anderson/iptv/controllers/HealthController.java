package com.anderson.iptv.controllers;

import com.anderson.iptv.model.HealthResponse;
import com.anderson.iptv.services.HealthService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final HealthService healthService;

    @GetMapping
    public Mono<HealthResponse> health() {
        return Mono.fromCallable(healthService::getHealth)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
