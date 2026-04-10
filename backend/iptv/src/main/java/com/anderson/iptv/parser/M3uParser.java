package com.anderson.iptv.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class M3uParser {

    private static final int BUFFER_SIZE = 8 * 1024 * 1024;
    private static final int CHUNK_SIZE = 10_000;

    private static final Pattern ATTR = Pattern.compile("([\\w-]+)=[\"']([^\"']*)[\"']");
    private static final Pattern EXTINF = Pattern.compile("#EXTINF:([\\d.+-]+)([^,]*),(.*)");

    private static final String EXTINF_PREFIX = "#EXTINF:";
    private static final String EXTM3U_PREFIX = "#EXTM3U";

    public Playlist parse(Path file) throws IOException {
        List<List<String>> chunks = new ArrayList<>();
        List<String> current = new ArrayList<>(CHUNK_SIZE);
        boolean lastWasExtInf = false;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8),
                BUFFER_SIZE)) {
            String rawLine;

            while ((rawLine = reader.readLine()) != null) {
                String line = rawLine.trim();
                if (line.isBlank()) {
                    continue;
                }

                current.add(line);
                lastWasExtInf = line.startsWith(EXTINF_PREFIX);

                if (current.size() >= CHUNK_SIZE && !lastWasExtInf) {
                    chunks.add(current);
                    current = new ArrayList<>(CHUNK_SIZE);
                }
            }
        }

        if (!current.isEmpty()) {
            chunks.add(current);
        }

        ForkJoinPool pool = new ForkJoinPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
        List<Channel> channels = new ArrayList<>();
        try {
            List<ChunkResult> results = pool.submit(() ->
                    IntStream.range(0, chunks.size())
                            .parallel()
                            .mapToObj(i -> new ChunkResult(i, parseChunk(chunks.get(i))))
                            .toList()
            ).get();

            results.stream()
                    .sorted(Comparator.comparingInt(ChunkResult::index))
                    .forEach(result -> channels.addAll(result.channels()));
        } catch (Exception e) {
            throw new IOException("Erro ao processar playlist M3U em paralelo", e);
        } finally {
            pool.shutdown();
        }

        log.info("Parsed {} channels from M3U playlist", channels.size());

        return Playlist.builder()
                .channels(channels)
                .fetchedAt(Instant.now())
                .totalChannels(channels.size())
                .build();
    }

    private List<Channel> parseChunk(List<String> lines) {
        List<Channel> channels = new ArrayList<>();
        Channel.ChannelBuilder pending = null;

        for (String line : lines) {
            if (line.startsWith(EXTM3U_PREFIX)) {
                continue;
            }
            if (line.startsWith(EXTINF_PREFIX)) {
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
        return channels;
    }

    private Channel.ChannelBuilder parseExtinf(String line) {
        Channel.ChannelBuilder b = Channel.builder();
        Matcher em = EXTINF.matcher(line);

        if (em.find()) {
            b.duration(toDouble(em.group(1)));
            String nomeFallback = em.group(3).trim();
            b.name(nomeFallback.isBlank() ? null : nomeFallback);
        }

        Matcher am = ATTR.matcher(line);
        while (am.find()) {
            String key = am.group(1).toLowerCase();
            String value = am.group(2).trim();

            if (value.isBlank()) {
                continue;
            }

            switch (key) {
                case "tvg-id" -> b.id(value);
                case "tvg-name" -> b.name(value);
                case "tvg-logo" -> b.logoUrl(value);
                case "group-title" -> b.groupTitle(value);
            }
        }

        return b;
    }

    private double toDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return -1;
        }
    }

    private record ChunkResult(int index, List<Channel> channels) {}
}
