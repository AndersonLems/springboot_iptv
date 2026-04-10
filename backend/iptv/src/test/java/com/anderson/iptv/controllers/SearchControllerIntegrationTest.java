package com.anderson.iptv.controllers;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.SearchResponse;
import com.anderson.iptv.services.GlobalSearchService;

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
class SearchControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        GlobalSearchService globalSearchService() {
            return Mockito.mock(GlobalSearchService.class);
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GlobalSearchService globalSearchService;

    private WebTestClient webTestClient;

    @Test
    void testSearchEndpoint() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        SearchResponse response = SearchResponse.builder()
                .query("avatar")
                .movies(List.of(Channel.builder().id("1").name("Avatar").streamUrl("s1").build()))
                .series(List.of())
                .live(List.of())
                .totalResults(1)
                .build();
        Mockito.when(globalSearchService.search("avatar", null)).thenReturn(response);

        webTestClient.get().uri("/api/search?q=avatar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalResults").isEqualTo(1)
                .jsonPath("$.movies[0].name").isEqualTo("Avatar");
    }
}
