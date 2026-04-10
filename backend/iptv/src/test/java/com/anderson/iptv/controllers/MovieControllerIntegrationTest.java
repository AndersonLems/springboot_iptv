package com.anderson.iptv.controllers;

import com.anderson.iptv.model.EnrichedMovie;
import com.anderson.iptv.model.EnrichedMovieListCache;
import com.anderson.iptv.model.PaginatedResponse;
import com.anderson.iptv.services.BrowseCatalogService;
import com.anderson.iptv.services.MovieMatcherService;

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

import java.util.List;

@SpringBootTest
class MovieControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        MovieMatcherService movieMatcherService() {
            return Mockito.mock(MovieMatcherService.class);
        }

        @Bean
        @Primary
        BrowseCatalogService browseCatalogService() {
            return Mockito.mock(BrowseCatalogService.class);
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MovieMatcherService movieMatcherService;

    @Autowired
    private BrowseCatalogService browseCatalogService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        EnrichedMovie movie = EnrichedMovie.builder().tmdbId(1L).title("Avatar").available(true).build();
        Mockito.when(movieMatcherService.trendingWithStreams())
                .thenReturn(reactor.core.publisher.Mono.just(new EnrichedMovieListCache(List.of(movie))));
        Mockito.when(movieMatcherService.topRatedWithStreams(1))
                .thenReturn(reactor.core.publisher.Mono.just(List.of(movie)));
        Mockito.when(movieMatcherService.popularWithStreams(1))
                .thenReturn(reactor.core.publisher.Mono.just(List.of(movie)));
        PaginatedResponse<EnrichedMovie> page = PaginatedResponse.<EnrichedMovie>builder()
                .content(List.of(movie))
                .page(0)
                .size(50)
                .totalElements(1)
                .totalPages(1)
                .sort("name")
                .order("asc")
                .build();
        Mockito.when(browseCatalogService.moviesAll(0, 50, "name", "asc", null)).thenReturn(page);
    }

    @Test
    void testTrending() {
        webTestClient.get().uri("/api/movies/trending")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Avatar");
    }

    @Test
    void testTopRated() {
        webTestClient.get().uri("/api/movies/top-rated?page=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Avatar");
    }

    @Test
    void testPopular() {
        webTestClient.get().uri("/api/movies/popular?page=1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Avatar");
    }

    @Test
    void testAll() {
        webTestClient.get().uri("/api/movies/all?page=0&size=50&sort=name&order=asc")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].title").isEqualTo("Avatar");
    }
}
