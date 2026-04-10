package com.anderson.iptv.filters;

import static org.mockito.ArgumentMatchers.anyString;

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
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.services.PlaylistService;

@SpringBootTest
class RateLimitFilterTest {

    @TestConfiguration
    static class TestConfig {
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

        @Bean
        @Primary
        PlaylistService playlistService() {
            return Mockito.mock(PlaylistService.class);
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ValueOperations<String, String> valueOperations;

    @Autowired
    private PlaylistService playlistService;

    private WebTestClient webTestClient;
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext).build();
        Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.increment(anyString())).thenAnswer(invocation -> {
            String key = invocation.getArgument(0);
            return counters.computeIfAbsent(key, k -> new AtomicLong()).incrementAndGet();
        });
        Mockito.when(redisTemplate.getExpire(anyString())).thenReturn(30L);
        Mockito.when(redisTemplate.expire(anyString(), Mockito.any())).thenReturn(true);
        Mockito.when(playlistService.forceRefresh()).thenReturn(Playlist.builder().totalChannels(0).build());
    }

    @Test
    void testFirstRequestAllowed() {
        webTestClient.post().uri("/api/playlist/refresh")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testRequestUnderLimitAllowed() {
        for (int i = 0; i < 5; i++) {
            webTestClient.post().uri("/api/playlist/refresh")
                    .exchange()
                    .expectStatus().isOk();
        }
    }

    @Test
    void testRequestOverLimitReturns429() {
        for (int i = 0; i < 6; i++) {
            webTestClient.post().uri("/api/playlist/refresh")
                    .exchange();
        }
        webTestClient.post().uri("/api/playlist/refresh")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectHeader().exists("Retry-After");
    }

    @Test
    void testRetryAfterHeaderPresent() {
        for (int i = 0; i < 7; i++) {
            webTestClient.post().uri("/api/playlist/refresh")
                    .exchange();
        }
        webTestClient.post().uri("/api/playlist/refresh")
                .exchange()
                .expectHeader().exists("Retry-After");
    }

    @Test
    void testDifferentIPsHaveSeparateLimits() {
        for (int i = 0; i < 5; i++) {
            webTestClient.post().uri("/api/playlist/refresh")
                    .header("X-Forwarded-For", "1.1.1.1")
                    .exchange()
                    .expectStatus().isOk();
        }
        webTestClient.post().uri("/api/playlist/refresh")
                .header("X-Forwarded-For", "2.2.2.2")
                .exchange()
                .expectStatus().isOk();
    }

}
