package com.anderson.iptv.controllers;

import com.anderson.iptv.model.CategoryGroup;
import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.services.PlaylistService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
class PlaylistControllerIntegrationTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        PlaylistService playlistService() {
            return Mockito.mock(PlaylistService.class);
        }

        @Bean
        @Primary
        StringRedisTemplate stringRedisTemplate() {
            return Mockito.mock(StringRedisTemplate.class);
        }

        @Bean
        @Primary
        ValueOperations<String, String> valueOperations() {
            return Mockito.mock(ValueOperations.class);
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ValueOperations<String, String> valueOperations;

    private WebTestClient webTestClient;
    private Playlist playlist;
    private final AtomicLong rateCounter = new AtomicLong();

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar").groupTitle("Filmes | Ação").streamUrl("s1").build(),
                Channel.builder().id("2").name("Lost").groupTitle("Series | Drama").streamUrl("s2").build()
        );
        playlist = Playlist.builder()
                .channels(channels)
                .totalChannels(channels.size())
                .fetchedAt(Instant.now())
                .build();
        Mockito.when(playlistService.getPlaylist()).thenReturn(playlist);
        Mockito.when(playlistService.getGroups()).thenReturn(List.of("Filmes | Ação", "Series | Drama"));
        Mockito.when(playlistService.byGroup("Filmes")).thenReturn(List.of(channels.get(0)));
        Mockito.when(playlistService.search("avatar")).thenReturn(List.of(channels.get(0)));
        Mockito.when(playlistService.forceRefresh()).thenReturn(playlist);
        Mockito.when(playlistService.getGroupedCategories()).thenReturn(List.of(
                CategoryGroup.builder().parent("Filmes").subcategories(List.of("Ação")).totalChannels(1).build()
        ));
        Mockito.when(playlistService.getChannelsByCategory("Filmes", null)).thenReturn(List.of(channels.get(0)));
        Mockito.when(playlistService.getChannelsByCategory("Filmes", "Ação")).thenReturn(List.of(channels.get(0)));
        Mockito.when(playlistService.searchChannelsByCategory("Filmes", "avatar")).thenReturn(List.of(channels.get(0)));

        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.increment(Mockito.anyString()))
                .thenAnswer(invocation -> rateCounter.incrementAndGet());
        Mockito.when(redisTemplate.getExpire(Mockito.anyString())).thenReturn(30L);
        Mockito.when(redisTemplate.expire(Mockito.anyString(), Mockito.any())).thenReturn(true);
    }

    @Test
    void testGetPlaylist() {
        webTestClient.get().uri("/api/playlist")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalChannels").isEqualTo(2);
    }

    @Test
    void testGetChannelsWithHeaders() {
        webTestClient.get().uri("/api/playlist/channels?page=0&size=1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Total-Count", "2")
                .expectHeader().valueEquals("X-Page", "0")
                .expectHeader().valueEquals("X-Page-Size", "1");
    }

    @Test
    void testGetGroups() {
        webTestClient.get().uri("/api/playlist/groups")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0]").isEqualTo("Filmes | Ação");
    }

    @Test
    void testByGroup() {
        webTestClient.get().uri("/api/playlist/channels/group/Filmes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Avatar");
    }

    @Test
    void testSearch() {
        webTestClient.get().uri("/api/playlist/channels/search?q=avatar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Avatar");
    }

    @Test
    void testRefresh() {
        webTestClient.post().uri("/api/playlist/refresh")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.totalChannels").isEqualTo(2);
    }

    @Test
    void testCategories() {
        webTestClient.get().uri("/api/playlist/categories")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].parent").isEqualTo("Filmes");
    }

    @Test
    void testByParentCategory() {
        webTestClient.get().uri("/api/playlist/categories/Filmes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Avatar");
    }

    @Test
    void testBySubCategory() {
        webTestClient.get().uri("/api/playlist/categories/Filmes/Ação")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Avatar");
    }

    @Test
    void testSearchInCategory() {
        webTestClient.get().uri("/api/playlist/categories/Filmes/search?q=avatar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Avatar");
    }
}
