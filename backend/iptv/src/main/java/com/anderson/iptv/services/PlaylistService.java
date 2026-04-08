package com.anderson.iptv.services;

import com.anderson.iptv.config.AppProperties;
import com.anderson.iptv.exception.PlaylistFetchException;
import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.parser.M3uParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class PlaylistService {

    private final WebClient webClient;
    private final M3uParser m3uParser;
    private final AppProperties props;

    @Autowired
    public PlaylistService(@Qualifier("genericWebClient") WebClient webClient,
            M3uParser m3uParser,
            AppProperties props) {
        this.webClient = webClient;
        this.m3uParser = m3uParser;
        this.props = props;
    }

    private volatile Playlist cache = null;

    public Playlist getPlaylist() {
        if (cacheValid())
            return cache;
        return fetchAndCache();
    }

    public List<String> getGroups() {
        return getPlaylist().getChannels().stream()
                .map(Channel::getGroupTitle)
                .filter(g -> g != null && !g.isBlank())
                .distinct().sorted().toList();
    }

    public List<Channel> byGroup(String group) {
        return getPlaylist().getChannels().stream()
                .filter(c -> group.equalsIgnoreCase(c.getGroupTitle()))
                .toList();
    }

    public List<Channel> search(String query) {
        String q = query.toLowerCase();
        return getPlaylist().getChannels().stream()
                .filter(c -> c.getName() != null
                        && c.getName().toLowerCase().contains(q))
                .toList();
    }

    public Playlist forceRefresh() {
        cache = null;
        return fetchAndCache();
    }

    private boolean cacheValid() {
        if (cache == null)
            return false;
        long elapsed = Instant.now().toEpochMilli()
                - cache.getFetchedAt().toEpochMilli();
        return elapsed < props.getM3u().getCacheTtlMS();
    }

    private Playlist fetchAndCache() {
        String url = props.getM3u().buildUrl();
        log.info("Buscando playlist: {}", url);
        try {
            String raw = webClient.get().uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            cache = m3uParser.parse(raw);
            return cache;
        } catch (Exception e) {
            log.error("Erro ao buscar playlist: {}", e.getMessage());
            throw new PlaylistFetchException(
                    "Não foi possível buscar a playlist: " + e.getMessage(), e);
        }
    }
}
