package com.anderson.iptv.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class M3uParser {

    private static final Pattern ATTR = Pattern.compile("([\\w-]+)=[\"']([^\"']*)[\"']");
    private static final Pattern EXTINF = Pattern.compile("#EXTINF:([\\d.+-]+)([^,]*),(.*)");

    private static final String EXTINF_PREFIX = "#EXTINF:";
    private static final String EXTM3U_PREFIX = "#EXTM3U";

    public Playlist parse(Path file) throws IOException {
        List<Channel> channels = new ArrayList<>();
        Channel.ChannelBuilder pending = null;

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String rawLine;

            while ((rawLine = reader.readLine()) != null) {
                String line = rawLine.trim();

                if (line.isBlank() || line.startsWith(EXTM3U_PREFIX)) {
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
        }

        log.info("Parsed {} channels from M3U playlist", channels.size());

        return Playlist.builder()
                .channels(channels)
                .fetchedAt(Instant.now())
                .totalChannels(channels.size())
                .build();
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
}