package com.anderson.iptv.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class M3uParserTest {

    @TempDir
    Path tempDir;

    @Test
    void testParseValidM3u() throws Exception {
        String m3u = """
                #EXTM3U
                #EXTINF:-1 tvg-id="1" tvg-name="Canal 1" tvg-logo="logo1" group-title="Filmes",Canal 1
                http://stream/1
                #EXTINF:-1 tvg-id="2" tvg-name="Canal 2" tvg-logo="logo2" group-title="Series",Canal 2
                http://stream/2
                """;

        Path file = writeTemp(m3u);
        M3uParser parser = new M3uParser();
        Playlist playlist = parser.parse(file);

        assertEquals(2, playlist.getTotalChannels());
        assertEquals(2, playlist.getChannels().size());
        Channel c1 = playlist.getChannels().get(0);
        assertEquals("1", c1.getId());
        assertEquals("Canal 1", c1.getName());
        assertEquals("logo1", c1.getLogoUrl());
        assertEquals("Filmes", c1.getGroupTitle());
        assertEquals("http://stream/1", c1.getStreamUrl());
    }

    @Test
    void testParseIgnoresUnknownTags() throws Exception {
        String m3u = """
                #EXTM3U
                #EXT-X-SESSION-DATA:DATA-ID="foo",VALUE="bar"
                #EXTINF:-1 tvg-id="1" tvg-name="Canal 1",Canal 1
                http://stream/1
                """;
        Path file = writeTemp(m3u);
        M3uParser parser = new M3uParser();
        Playlist playlist = parser.parse(file);
        assertEquals(1, playlist.getTotalChannels());
    }

    @Test
    void testParseEmptyValueBecomesNull() throws Exception {
        String m3u = """
                #EXTM3U
                #EXTINF:-1 tvg-id="" tvg-name="Canal 1",Canal 1
                http://stream/1
                """;
        Path file = writeTemp(m3u);
        M3uParser parser = new M3uParser();
        Playlist playlist = parser.parse(file);
        Channel channel = playlist.getChannels().get(0);
        assertEquals(null, channel.getId());
    }

    @Test
    void testParseGroupTitle() throws Exception {
        String m3u = """
                #EXTM3U
                #EXTINF:-1 tvg-id="1" group-title="Filmes | Terror",Canal 1
                http://stream/1
                """;
        Path file = writeTemp(m3u);
        M3uParser parser = new M3uParser();
        Playlist playlist = parser.parse(file);
        assertEquals("Filmes | Terror", playlist.getChannels().get(0).getGroupTitle());
    }

    @Test
    void testParseStreamUrl() throws Exception {
        String m3u = """
                #EXTM3U
                #EXTINF:-1 tvg-id="1" tvg-name="Canal 1",Canal 1
                http://stream/1
                """;
        Path file = writeTemp(m3u);
        M3uParser parser = new M3uParser();
        Playlist playlist = parser.parse(file);
        assertEquals("http://stream/1", playlist.getChannels().get(0).getStreamUrl());
    }

    @Test
    void testParseEmptyFile() throws Exception {
        Path file = writeTemp("");
        M3uParser parser = new M3uParser();
        Playlist playlist = parser.parse(file);
        assertEquals(0, playlist.getTotalChannels());
        assertTrue(playlist.getChannels().isEmpty());
    }

    @Test
    void testParallelParseProducesSameResultAsSequential() throws Exception {
        StringBuilder sb = new StringBuilder("#EXTM3U\n");
        int totalLines = 50_000;
        int entries = (totalLines - 1) / 2;
        for (int i = 0; i < entries; i++) {
            sb.append("#EXTINF:-1 tvg-id=\"").append(i).append("\" tvg-name=\"Canal ")
                    .append(i).append("\" group-title=\"Filmes\",Canal ").append(i).append("\n");
            sb.append("http://stream/").append(i).append("\n");
        }
        Path file = writeTemp(sb.toString());
        M3uParser parser = new M3uParser();
        Playlist parallel = parser.parse(file);
        Playlist sequential = parseSequential(file);

        assertEquals(sequential.getTotalChannels(), parallel.getTotalChannels());
        assertEquals(sequential.getChannels().size(), parallel.getChannels().size());
        assertNotNull(parallel.getFetchedAt());
    }

    private Path writeTemp(String content) throws IOException {
        Path file = tempDir.resolve("playlist.m3u");
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    private Playlist parseSequential(Path file) throws IOException {
        List<Channel> channels = new ArrayList<>();
        Channel.ChannelBuilder pending = null;
        try (var reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String rawLine;
            while ((rawLine = reader.readLine()) != null) {
                String line = rawLine.trim();
                if (line.isBlank() || line.startsWith("#EXTM3U")) {
                    continue;
                }
                if (line.startsWith("#EXTINF:")) {
                    pending = parseExtinf(line);
                    continue;
                }
                if (line.startsWith("#")) {
                    continue;
                }
                if (pending != null) {
                    channels.add(pending.streamUrl(line).build());
                    pending = null;
                }
            }
        }
        return Playlist.builder()
                .channels(channels)
                .totalChannels(channels.size())
                .fetchedAt(null)
                .build();
    }

    private Channel.ChannelBuilder parseExtinf(String line) {
        Channel.ChannelBuilder b = Channel.builder();
        int comma = line.indexOf(",");
        if (comma >= 0) {
            String name = line.substring(comma + 1).trim();
            if (!name.isBlank()) {
                b.name(name);
            }
        }
        if (line.contains("tvg-id=\"")) {
            String id = extractAttr(line, "tvg-id");
            if (id != null && !id.isBlank()) {
                b.id(id);
            }
        }
        if (line.contains("tvg-name=\"")) {
            String name = extractAttr(line, "tvg-name");
            if (name != null && !name.isBlank()) {
                b.name(name);
            }
        }
        if (line.contains("tvg-logo=\"")) {
            String logo = extractAttr(line, "tvg-logo");
            if (logo != null && !logo.isBlank()) {
                b.logoUrl(logo);
            }
        }
        if (line.contains("group-title=\"")) {
            String group = extractAttr(line, "group-title");
            if (group != null && !group.isBlank()) {
                b.groupTitle(group);
            }
        }
        return b;
    }

    private String extractAttr(String line, String key) {
        String marker = key + "=\"";
        int idx = line.indexOf(marker);
        if (idx < 0) {
            return null;
        }
        int start = idx + marker.length();
        int end = line.indexOf("\"", start);
        if (end < 0) {
            return null;
        }
        return line.substring(start, end);
    }
}
