package com.anderson.iptv.controllers;

import com.anderson.iptv.services.FavoritesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
class FavoritesControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        FavoritesService favoritesService() {
            return Mockito.mock(FavoritesService.class);
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private FavoritesService favoritesService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
    }

    @Test
    void testMissingDeviceIdReturns400() {
        webTestClient.get().uri("/api/favorites")
                .exchange()
                .expectStatus().isBadRequest();
    }
}
