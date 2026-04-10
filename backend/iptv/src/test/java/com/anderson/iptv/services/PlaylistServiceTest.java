package com.anderson.iptv.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.CategoryGroup;
import com.anderson.iptv.model.Playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

@SpringBootTest
class PlaylistServiceTest {

    @TestConfiguration
    @EnableCaching
    static class TestCacheConfig {
        @Bean
        @Primary
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    "iptv:movies/channels",
                    "iptv:playlist:categories",
                    "iptv:movies:trending",
                    "iptv:movies:top-rated",
                    "iptv:movies:popular",
                    "iptv:movies:all",
                    "iptv:series:trending",
                    "iptv:series:top-rated",
                    "iptv:series:popular",
                    "iptv:series:playlist",
                    "iptv:series:all",
                    "iptv:live",
                    "iptv:search");
        }

        @Bean
        @Primary
        PlaylistCacheService playlistCacheService() {
            return Mockito.mock(PlaylistCacheService.class);
        }

        @Bean
        @Primary
        PlaylistMetrics playlistMetrics() {
            return Mockito.mock(PlaylistMetrics.class);
        }
    }

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PlaylistCacheService playlistCacheService;

    @Autowired
    private PlaylistMetrics metrics;

    @Autowired
    private ChannelIndex channelIndex;

    private Playlist playlist;

    @BeforeEach
    void setup() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar").groupTitle("Filmes | Ação").streamUrl("s1").build(),
                Channel.builder().id("2").name("Avatar Fogo e Cinzas").groupTitle("Filmes | Ação").streamUrl("s2").build(),
                Channel.builder().id("3").name("Lost S01E01").groupTitle("Series | Drama").streamUrl("s3").build(),
                Channel.builder().id("4").name("GamePlay").groupTitle("GamePlay").streamUrl("s4").build()
        );
        playlist = Playlist.builder().channels(channels).totalChannels(channels.size()).build();
        when(playlistCacheService.getPlaylist()).thenReturn(playlist);
        when(playlistCacheService.forceRefresh()).thenReturn(playlist);
        channelIndex.rebuild(playlist);
    }

    @Test
    void testGetPlaylistUsesCacheOnSecondCall() {
        playlistService.getChannelsByCategory("Filmes", "Ação");
        playlistService.getChannelsByCategory("Filmes", "Ação");
        verify(playlistCacheService, times(1)).getPlaylist();
    }

    @Test
    void testCacheExpiredAfterTTL() {
        playlistService.getChannelsByCategory("Filmes", "Ação");
        cacheManager.getCache("iptv:movies/channels").clear();
        playlistService.getChannelsByCategory("Filmes", "Ação");
        verify(playlistCacheService, times(2)).getPlaylist();
    }

    @Test
    void testForceRefreshInvalidatesCache() {
        playlistService.getChannelsByCategory("Filmes", "Ação");
        playlistService.forceRefresh();
        playlistService.getChannelsByCategory("Filmes", "Ação");
        verify(playlistCacheService, times(2)).getPlaylist();
        verify(playlistCacheService, times(1)).forceRefresh();
    }

    @Test
    void testByGroupCaseInsensitive() {
        List<Channel> channels = playlistService.byGroup("filmes | ação");
        assertEquals(2, channels.size());
    }

    @Test
    void testSearchReturnsMatchingChannels() {
        List<Channel> channels = playlistService.search("avatar");
        assertEquals(2, channels.size());
    }

    @Test
    void testGetGroupedCategories() {
        List<CategoryGroup> groups = playlistService.getGroupedCategories();
        CategoryGroup filmes = groups.stream()
                .filter(g -> g.getParent().equals("Filmes"))
                .findFirst()
                .orElseThrow();
        assertTrue(filmes.getSubcategories().contains("Ação"));
    }

    @Test
    void testGetGroupedCategoriesOthers() {
        List<CategoryGroup> groups = playlistService.getGroupedCategories();
        CategoryGroup outros = groups.stream()
                .filter(g -> g.getParent().equals("Outros"))
                .findFirst()
                .orElseThrow();
        assertTrue(outros.getSubcategories().contains("GamePlay"));
    }

    @Test
    void testGetChannelsByCategory() {
        List<Channel> channels = playlistService.getChannelsByCategory("Filmes", "Ação");
        assertEquals(2, channels.size());
    }

    @Test
    void testSearchChannelsByCategory() {
        List<Channel> channels = playlistService.searchChannelsByCategory("Filmes", "avatar");
        assertEquals(2, channels.size());
    }
}
