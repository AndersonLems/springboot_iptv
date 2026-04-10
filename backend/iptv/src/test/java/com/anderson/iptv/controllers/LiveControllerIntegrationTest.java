package com.anderson.iptv.controllers;

import com.anderson.iptv.model.LiveChannel;
import com.anderson.iptv.model.PaginatedResponse;
import com.anderson.iptv.services.LiveChannelService;

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
class LiveControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        LiveChannelService liveChannelService() {
            return Mockito.mock(LiveChannelService.class);
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private LiveChannelService liveChannelService;

    private WebTestClient webTestClient;

    @Test
    void testLiveEndpoint() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        LiveChannel live = LiveChannel.builder().id("1").name("ESPN").category("Esportes").isLive(true).build();
        PaginatedResponse<LiveChannel> page = PaginatedResponse.<LiveChannel>builder()
                .content(List.of(live))
                .page(0)
                .size(50)
                .totalElements(1)
                .totalPages(1)
                .sort("name")
                .order("asc")
                .build();
        Mockito.when(liveChannelService.getLive(null, 0, 50)).thenReturn(page);

        webTestClient.get().uri("/api/live")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].name").isEqualTo("ESPN")
                .jsonPath("$.content[0].isLive").isEqualTo(true);
    }
}
