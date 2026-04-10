package com.anderson.iptv.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anderson.iptv.model.Channel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class FavoritesServiceTest {

    private StringRedisTemplate redisTemplate;
    private SetOperations<String, String> setOps;
    private Set<String> store;
    private ChannelIndex channelIndex;
    private FavoritesService favoritesService;

    @BeforeEach
    void setup() {
        redisTemplate = Mockito.mock(StringRedisTemplate.class);
        setOps = Mockito.mock(SetOperations.class);
        store = new HashSet<>();
        channelIndex = new ChannelIndex();
        favoritesService = new FavoritesService(redisTemplate, channelIndex);

        Mockito.when(redisTemplate.opsForSet()).thenReturn(setOps);
        Mockito.doAnswer(invocation -> {
            store.add(invocation.getArgument(1));
            return null;
        }).when(setOps).add(Mockito.anyString(), Mockito.anyString());
        Mockito.doAnswer(invocation -> {
            store.remove(invocation.getArgument(1));
            return null;
        }).when(setOps).remove(Mockito.anyString(), Mockito.anyString());
        Mockito.when(setOps.members(Mockito.anyString())).thenAnswer(invocation -> new HashSet<>(store));

        channelIndex.rebuild(com.anderson.iptv.model.Playlist.builder().channels(List.of(
                Channel.builder().id("1").name("Avatar").streamUrl("s1").build()
        )).totalChannels(1).build());
    }

    @Test
    void testAddFavorite() {
        favoritesService.addFavorite("dev1", "1");
        assertEquals(1, store.size());
    }

    @Test
    void testRemoveFavorite() {
        favoritesService.addFavorite("dev1", "1");
        favoritesService.removeFavorite("dev1", "1");
        assertEquals(0, store.size());
    }

    @Test
    void testGetFavoritesReturnsAllChannels() {
        favoritesService.addFavorite("dev1", "1");
        List<Channel> favorites = favoritesService.getFavorites("dev1");
        assertEquals(1, favorites.size());
        assertEquals("Avatar", favorites.get(0).getName());
    }
}
