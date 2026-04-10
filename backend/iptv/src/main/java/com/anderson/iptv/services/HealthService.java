package com.anderson.iptv.services;

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

    public HealthService(PlaylistService playlistService,
            ChannelIndex channelIndex,
            PlaylistMetrics metrics,
            RedisConnectionFactory redisConnectionFactory) {
        this.playlistService = playlistService;
        this.channelIndex = channelIndex;
        this.metrics = metrics;
        this.redisConnectionFactory = redisConnectionFactory;
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
}
