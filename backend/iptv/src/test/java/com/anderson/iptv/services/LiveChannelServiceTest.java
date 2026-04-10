package com.anderson.iptv.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.LiveChannel;
import com.anderson.iptv.model.PaginatedResponse;
import com.anderson.iptv.model.Playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class LiveChannelServiceTest {

    private ChannelIndex channelIndex;
    private PlaylistService playlistService;
    private LiveChannelService service;

    @BeforeEach
    void setup() {
        channelIndex = new ChannelIndex();
        playlistService = Mockito.mock(PlaylistService.class);
        service = new LiveChannelService(channelIndex, playlistService);
    }

    @Test
    void testFilterByCanaisPrefix() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("ESPN").groupTitle("Canais | Esportes").streamUrl("s1").build(),
                Channel.builder().id("2").name("Netflix").groupTitle("Filmes").streamUrl("s2").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(2).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        PaginatedResponse<LiveChannel> response = service.getLive(null, 0, 50);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void testCategoryFilter() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("ESPN").groupTitle("Canais | Esportes").streamUrl("s1").build(),
                Channel.builder().id("2").name("CNN").groupTitle("Canais | Noticias").streamUrl("s2").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(2).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        PaginatedResponse<LiveChannel> response = service.getLive("Esportes", 0, 50);
        assertEquals(1, response.getContent().size());
        assertEquals("Esportes", response.getContent().get(0).getCategory());
    }

    @Test
    void testIsLiveFieldIsTrue() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("ESPN").groupTitle("Canais | Esportes").streamUrl("s1").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(1).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        PaginatedResponse<LiveChannel> response = service.getLive(null, 0, 50);
        assertTrue(Boolean.TRUE.equals(response.getContent().get(0).getIsLive()));
    }

    @Test
    void testPaginationCorrect() {
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("A").groupTitle("Canais | Esportes").streamUrl("s1").build(),
                Channel.builder().id("2").name("B").groupTitle("Canais | Esportes").streamUrl("s2").build(),
                Channel.builder().id("3").name("C").groupTitle("Canais | Esportes").streamUrl("s3").build()
        );
        Playlist playlist = Playlist.builder().channels(channels).totalChannels(3).build();
        when(playlistService.getPlaylist()).thenReturn(playlist);
        channelIndex.rebuild(playlist);

        PaginatedResponse<LiveChannel> response = service.getLive(null, 0, 2);
        assertEquals(2, response.getContent().size());
    }
}
