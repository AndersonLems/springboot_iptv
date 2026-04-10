package com.anderson.iptv.services;

import com.anderson.iptv.config.AppProperties;
import com.anderson.iptv.model.EpgProgram;
import com.anderson.iptv.parser.EpgParser;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class EpgService {

    private final WebClient webClient;
    private final AppProperties props;
    private final EpgParser parser;

    public EpgService(@Qualifier("genericWebClient") WebClient webClient, AppProperties props, EpgParser parser) {
        this.webClient = webClient;
        this.props = props;
        this.parser = parser;
    }

    @Cacheable(value = "iptv:epg", key = "'all'", condition = "@cacheToggle.enabled()")
    public Map<String, List<EpgProgram>> loadEpg() {
        String url = props.getEpg().getUrl();
        if (url == null || url.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "EPG not configured");
        }
        String xml = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if (xml == null || xml.isBlank()) {
            return Map.of();
        }
        return parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    public List<EpgProgram> getSchedule(String channelId) {
        Map<String, List<EpgProgram>> data = loadEpg();
        List<EpgProgram> schedule = data.getOrDefault(channelId, List.of());
        Instant now = Instant.now();
        return schedule.stream()
                .map(p -> p.toBuilder()
                        .isCurrentlyAiring(isNow(p, now))
                        .build())
                .sorted(Comparator.comparing(EpgProgram::getStartTime))
                .toList();
    }

    public EpgProgram getNow(String channelId) {
        return getSchedule(channelId).stream()
                .filter(EpgProgram::isCurrentlyAiring)
                .findFirst()
                .orElse(null);
    }

    public EpgProgram getNext(String channelId) {
        Instant now = Instant.now();
        return getSchedule(channelId).stream()
                .filter(p -> p.getStartTime() != null && p.getStartTime().isAfter(now))
                .min(Comparator.comparing(EpgProgram::getStartTime))
                .orElse(null);
    }

    private boolean isNow(EpgProgram program, Instant now) {
        if (program.getStartTime() == null || program.getEndTime() == null) {
            return false;
        }
        return !now.isBefore(program.getStartTime()) && now.isBefore(program.getEndTime());
    }
}
