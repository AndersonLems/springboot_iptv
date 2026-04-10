package com.anderson.iptv.services;

import com.anderson.iptv.config.AppProperties;
import com.anderson.iptv.exception.PlaylistFetchException;
import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.parser.M3uParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class PlaylistCacheService {

    private final WebClient webClient;
    private final M3uParser m3uParser;
    private final AppProperties props;
    private final PlaylistMetrics metrics;

    @Autowired
    public PlaylistCacheService(@Qualifier("genericWebClient") WebClient webClient,
            M3uParser m3uParser,
            AppProperties props,
            PlaylistMetrics metrics) {
        this.webClient = webClient;
        this.m3uParser = m3uParser;
        this.props = props;
        this.metrics = metrics;
    }

    @Cacheable(value = "iptv:playlist", condition = "@cacheToggle.enabled()")
    public Playlist getPlaylist() throws PlaylistFetchException {
        return fetchPlaylist();
    }

    @CachePut(value = "iptv:playlist", condition = "@cacheToggle.enabled()")
    public Playlist forceRefresh() throws PlaylistFetchException {
        return fetchPlaylist();
    }

    private Playlist fetchPlaylist() throws PlaylistFetchException {
        if (!isM3uConfigured()) {
            log.warn(
                    "M3U config incompleta. Host='{}', Username='{}'. Verifique APP_M3U_HOST, APP_M3U_USERNAME e APP_M3U_PASSWORD.",
                    safeValue(props.getM3u().getHost()),
                    maskUsername(props.getM3u().getUsername()));
            throw new PlaylistFetchException(
                    "Configuração M3U incompleta. Verifique APP_M3U_HOST, APP_M3U_USERNAME e APP_M3U_PASSWORD.",
                    null);
        }

        String url = props.getM3u().buildUrl();
        Path tempFile = null;
        long startNs = System.nanoTime();

        log.info("Buscando playlist M3U");

        try {
            tempFile = Files.createTempFile("playlist-", ".m3u");

            DataBufferUtils.write(
                    webClient.get()
                            .uri(url)
                            .retrieve()
                            .bodyToFlux(DataBuffer.class),
                    tempFile).block();

            Playlist playlist = m3uParser.parse(tempFile);
            long durationMs = (System.nanoTime() - startNs) / 1_000_000L;
            metrics.recordParse(durationMs, playlist.getFetchedAt());
            return playlist;

        } catch (IOException | RuntimeException e) {
            if (isInterrupted(e)) {
                Thread.currentThread().interrupt();
                log.warn("Busca da playlist foi interrompida");
                throw new PlaylistFetchException(
                        "Busca da playlist foi interrompida", e);
            }

            log.error("Erro ao buscar playlist", e);
            throw new PlaylistFetchException(
                    "Não foi possível buscar a playlist: " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ex) {
                    log.warn("Não foi possível remover arquivo temporário: {}", ex.getMessage());
                }
            }
        }
    }

    private boolean isInterrupted(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof InterruptedException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isM3uConfigured() {
        var m3u = props.getM3u();
        return StringUtils.hasText(m3u.getHost())
                && StringUtils.hasText(m3u.getUsername())
                && StringUtils.hasText(m3u.getPassword());
    }

    private String maskUsername(String value) {
        if (!StringUtils.hasText(value)) {
            return "(vazio)";
        }
        if (value.length() <= 3) {
            return value.charAt(0) + "***";
        }
        return value.substring(0, 3) + "***";
    }

    private String safeValue(String value) {
        return StringUtils.hasText(value) ? value : "(vazio)";
    }
}
