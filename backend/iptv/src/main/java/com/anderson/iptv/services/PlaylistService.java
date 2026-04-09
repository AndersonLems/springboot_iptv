package com.anderson.iptv.services;

import com.anderson.iptv.config.AppProperties;
import com.anderson.iptv.exception.PlaylistFetchException;
import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.parser.M3uParser;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import com.anderson.iptv.model.CategoryGroup;

@Slf4j
@Service
public class PlaylistService {

    private final WebClient webClient;
    private final M3uParser m3uParser;
    private final AppProperties props;

    private volatile Playlist cache = null;

    @Autowired
    public PlaylistService(@Qualifier("genericWebClient") WebClient webClient,
                           M3uParser m3uParser,
                           AppProperties props) {
        this.webClient = webClient;
        this.m3uParser = m3uParser;
        this.props = props;
    }

    public Playlist getPlaylist() {
        if (cacheValid()) {
            return cache;
        }
        return fetchAndCache();
    }

    public List<String> getGroups() {
        return getPlaylist().getChannels().stream()
                .map(Channel::getGroupTitle)
                .filter(g -> g != null && !g.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public List<Channel> byGroup(String group) {
        return getPlaylist().getChannels().stream()
                .filter(c -> group.equalsIgnoreCase(c.getGroupTitle()))
                .toList();
    }

    public List<Channel> search(String query) {
        String q = query.toLowerCase();
        return getPlaylist().getChannels().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(q))
                .toList();
    }

    public Playlist forceRefresh() {
        cache = null;
        return fetchAndCache();
    }

    public List<CategoryGroup> getGroupedCategories() {
        // Agrupa os canais por categoria-pai
        Map<String, List<Channel>> byParent = getPlaylist().getChannels().stream()
                .filter(c -> c.getGroupTitle() != null && !c.getGroupTitle().isBlank())
                .collect(Collectors.groupingBy(c -> extractParent(c.getGroupTitle())));

        return byParent.entrySet().stream()
                .map(entry -> {
                    String parent = entry.getKey();
                    List<Channel> channels = entry.getValue();

                    // Subcategorias únicas e ordenadas dentro deste pai
                    List<String> subs = channels.stream()
                            .map(c -> extractSub(c.getGroupTitle()))
                            .distinct()
                            .sorted()
                            .toList();

                    return CategoryGroup.builder()
                            .parent(parent)
                            .subcategories(subs)
                            .totalChannels(channels.size())
                            .build();
                })
                .sorted(Comparator.comparing(CategoryGroup::getParent))
                .toList();
    }

    public List<Channel> getChannelsByCategory(String parent, String sub) {
        String fullGroup = sub == null || sub.isBlank()
                ? parent
                : parent + " | " + sub;

        return getPlaylist().getChannels().stream()
                .filter(c -> c.getGroupTitle() != null &&
                             normalizeGroup(c.getGroupTitle()).equals(normalizeGroup(fullGroup)))
                .toList();
    }

    public List<Channel> searchChannelsByCategory(String parent, String query){
        String normalizedParent = normalizeGroup(parent);
        String q = query.toLowerCase();
        
        return getPlaylist().getChannels().stream()
                .filter(c -> c.getGroupTitle() != null
                        && extractParent(c.getGroupTitle()).toLowerCase().equals(normalizedParent)
                        && c.getName() != null
                        && c.getName().toLowerCase().contains(q))
                .toList();
    }

    private String extractParent(String group) {
        if (group.contains("|")) {
            return group.substring(0, group.indexOf("|")).trim();
        }
        return "Outros";
    }

    private String extractSub(String group) {
        if (group.contains("|")) {
            return group.substring(group.indexOf("|") + 1).trim();
        }
        return group.trim();
    }

    private String normalizeGroup(String group) {
        return group.toLowerCase().trim();
    }


    private boolean cacheValid() {
        if (cache == null || cache.getFetchedAt() == null) {
            return false;
        }

        long elapsed = Instant.now().toEpochMilli() - cache.getFetchedAt().toEpochMilli();
        return elapsed < props.getM3u().getCacheTtlMS();
    }

    private Playlist fetchAndCache() {
        String url = props.getM3u().buildUrl();
        Path tempFile = null;

        log.info("Buscando playlist M3U");

        try {
            tempFile = Files.createTempFile("playlist-", ".m3u");

            DataBufferUtils.write(
                    webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToFlux(DataBuffer.class),
                    tempFile
            ).block();

            cache = m3uParser.parse(tempFile);
            return cache;

        } catch (Exception e) {
            log.error("Erro ao buscar playlist", e);
            throw new PlaylistFetchException(
                    "Não foi possível buscar a playlist: " + e.getMessage(), e
            );
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception ex) {
                    log.warn("Não foi possível remover arquivo temporário: {}", ex.getMessage());
                }
            }
        }
    }
}