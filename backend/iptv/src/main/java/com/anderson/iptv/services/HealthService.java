package com.anderson.iptv.services;

import com.anderson.iptv.config.AppProperties;
import com.anderson.iptv.model.HealthResponse;

import java.util.Locale;
import java.util.Map;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final PlaylistService playlistService;
    private final ChannelIndex channelIndex;
    private final PlaylistMetrics metrics;
    private final RedisConnectionFactory redisConnectionFactory;
    private final AppProperties props;

    public HealthService(PlaylistService playlistService,
            ChannelIndex channelIndex,
            PlaylistMetrics metrics,
            RedisConnectionFactory redisConnectionFactory,
            AppProperties props) {
        this.playlistService = playlistService;
        this.channelIndex = channelIndex;
        this.metrics = metrics;
        this.redisConnectionFactory = redisConnectionFactory;
        this.props = props;
    }

    public HealthResponse getHealth() {
        var playlist = playlistService.getPlaylist();
        int totalChannels = playlist.getTotalChannels();

        Map<String, java.util.List<com.anderson.iptv.model.Channel>> byGroup = channelIndex.getIndexByGroup();
        int totalMovies = countByPrefix(byGroup, "filmes");
        int totalSeries = countByPrefix(byGroup, "series");
        int totalLive = countByPrefix(byGroup, "canais |");

        String redisStatus = redisUp() ? "UP" : "DOWN";

        return HealthResponse.builder()
                .status("UP")
                .redis(redisStatus)
                .playlistLoaded(totalChannels > 0)
                .totalChannels(totalChannels)
                .totalMovies(totalMovies)
                .totalSeries(totalSeries)
                .totalLive(totalLive)
                .lastParsedAt(metrics.getLastParsedAt())
                .parseTimeMs(metrics.getParseTimeMs())
                .cacheHitRate(String.format("%.0f%%", metrics.getHitRatePercent()))
                .m3uHost(safeValue(props.getM3u().getHost()))
                .m3uUsernameMasked(maskUsername(props.getM3u().getUsername()))
                .m3uPasswordSet(hasValue(props.getM3u().getPassword()))
                .build();
    }

    private int countByPrefix(Map<String, java.util.List<com.anderson.iptv.model.Channel>> byGroup, String prefix) {
        return byGroup.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && entry.getKey().toLowerCase(Locale.ROOT).startsWith(prefix))
                .mapToInt(entry -> entry.getValue().size())
                .sum();
    }

    private boolean redisUp() {
        try (var conn = redisConnectionFactory.getConnection()) {
            String pong = conn.ping();
            return pong != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }

    private String safeValue(String value) {
        return hasValue(value) ? value : "(vazio)";
    }

    private String maskUsername(String value) {
        if (!hasValue(value)) {
            return "(vazio)";
        }
        if (value.length() <= 3) {
            return value.charAt(0) + "***";
        }
        return value.substring(0, 3) + "***";
    }
}
