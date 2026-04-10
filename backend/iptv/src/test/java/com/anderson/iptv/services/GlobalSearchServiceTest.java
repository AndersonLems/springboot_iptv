package com.anderson.iptv.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

class GlobalSearchServiceTest {

    private ChannelIndex channelIndex;
    private GlobalSearchService searchService;

    @BeforeEach
    void setup() {
        channelIndex = new ChannelIndex();
        searchService = new GlobalSearchService(channelIndex);

        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar").groupTitle("Filmes").streamUrl("s1").build(),
                Channel.builder().id("2").name("Avatar Serie").groupTitle("Series").streamUrl("s2").build(),
                Channel.builder().id("3").name("Avatar News").groupTitle("Canais | Noticias").streamUrl("s3").build()
        );
        channelIndex.rebuild(Playlist.builder().channels(channels).totalChannels(3).build());
    }

    @Test
    void testSearchAcrossAllTypes() {
        var response = searchService.search("Avatar", null);
        assertEquals(3, response.getTotalResults());
        assertEquals(1, response.getMovies().size());
        assertEquals(1, response.getSeries().size());
        assertEquals(1, response.getLive().size());
    }

    @Test
    void testSearchWithTypeFilter() {
        var response = searchService.search("Avatar", "movies");
        assertEquals(1, response.getTotalResults());
        assertEquals(1, response.getMovies().size());
        assertEquals(0, response.getSeries().size());
        assertEquals(0, response.getLive().size());
    }

    @Test
    void testSearchMinLength() {
        assertThrows(ResponseStatusException.class, () -> searchService.search("a", null));
    }

    @Test
    void testResultsCappedAt20PerCategory() {
        List<Channel> many = java.util.stream.IntStream.range(0, 50)
                .mapToObj(i -> Channel.builder().id(String.valueOf(i)).name("Avatar " + i)
                        .groupTitle("Filmes").streamUrl("s" + i).build())
                .toList();
        channelIndex.rebuild(Playlist.builder().channels(many).totalChannels(50).build());
        var response = searchService.search("Avatar", "movies");
        assertEquals(20, response.getMovies().size());
    }
}
