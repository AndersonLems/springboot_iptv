package com.anderson.iptv.controllers;

import com.anderson.iptv.model.HealthResponse;
import com.anderson.iptv.services.HealthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;

@SpringBootTest
class HealthControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        HealthService healthService() {
            return Mockito.mock(HealthService.class);
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private HealthService healthService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Test
    void testHealthReturnsUp() {
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .redis("UP")
                .playlistLoaded(true)
                .totalChannels(10)
                .totalMovies(5)
                .totalSeries(3)
                .totalLive(2)
                .lastParsedAt(Instant.now())
                .parseTimeMs(100)
                .cacheHitRate("80%")
                .build();
        Mockito.when(healthService.getHealth()).thenReturn(response);

        webTestClient.get().uri("/api/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.redis").isEqualTo("UP");
    }

    @Test
    void testHealthWhenRedisDown() {
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .redis("DOWN")
                .playlistLoaded(true)
                .totalChannels(10)
                .totalMovies(5)
                .totalSeries(3)
                .totalLive(2)
                .lastParsedAt(Instant.now())
                .parseTimeMs(100)
                .cacheHitRate("80%")
                .build();
        Mockito.when(healthService.getHealth()).thenReturn(response);

        webTestClient.get().uri("/api/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.redis").isEqualTo("DOWN");
    }

    @Test
    void testTotalChannelCountCorrect() {
        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .redis("UP")
                .playlistLoaded(true)
                .totalChannels(423000)
                .totalMovies(12400)
                .totalSeries(8200)
                .totalLive(950)
                .lastParsedAt(Instant.now())
                .parseTimeMs(4200)
                .cacheHitRate("87%")
                .build();
        Mockito.when(healthService.getHealth()).thenReturn(response);

        webTestClient.get().uri("/api/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalChannels").isEqualTo(423000);
    }
}
