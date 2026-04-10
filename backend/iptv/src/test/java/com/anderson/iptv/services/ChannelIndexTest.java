package com.anderson.iptv.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;

import org.junit.jupiter.api.Test;

import java.util.List;

class ChannelIndexTest {

    @Test
    void testIndexBuiltCorrectly() {
        ChannelIndex index = new ChannelIndex();
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Avatar").groupTitle("Filmes").streamUrl("s1").build(),
                Channel.builder().id("2").name("Avatar 4K").groupTitle("Filmes").streamUrl("s2").build()
        );
        index.rebuild(Playlist.builder().channels(channels).totalChannels(2).build());
        assertEquals(2, index.getIndexByNormalizedName().get("avatar").size());
    }

    @Test
    void testIndexByGroupReturnsCorrectChannels() {
        ChannelIndex index = new ChannelIndex();
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("A").groupTitle("Filmes").streamUrl("s1").build(),
                Channel.builder().id("2").name("B").groupTitle("Series").streamUrl("s2").build()
        );
        index.rebuild(Playlist.builder().channels(channels).totalChannels(2).build());
        assertEquals(1, index.getByGroup("Filmes").size());
    }

    @Test
    void testRebuildClearsOldIndex() {
        ChannelIndex index = new ChannelIndex();
        List<Channel> channels1 = List.of(
                Channel.builder().id("1").name("Avatar").groupTitle("Filmes").streamUrl("s1").build()
        );
        index.rebuild(Playlist.builder().channels(channels1).totalChannels(1).build());
        List<Channel> channels2 = List.of(
                Channel.builder().id("2").name("Lost").groupTitle("Series").streamUrl("s2").build()
        );
        index.rebuild(Playlist.builder().channels(channels2).totalChannels(1).build());
        assertTrue(index.getIndexByNormalizedName().get("avatar") == null);
        assertEquals(1, index.getIndexByNormalizedName().get("lost").size());
    }

    @Test
    void testNormalizedKeyLookup() {
        ChannelIndex index = new ChannelIndex();
        List<Channel> channels = List.of(
                Channel.builder().id("1").name("Coração Valente").groupTitle("Filmes").streamUrl("s1").build()
        );
        index.rebuild(Playlist.builder().channels(channels).totalChannels(1).build());
        assertEquals(1, index.getIndexByNormalizedName().get("coracao valente").size());
    }
}
