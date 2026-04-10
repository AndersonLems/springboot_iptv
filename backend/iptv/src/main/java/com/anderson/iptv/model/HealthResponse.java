package com.anderson.iptv.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {
    private String status;
    private String redis;
    private boolean playlistLoaded;
    private int totalChannels;
    private int totalMovies;
    private int totalSeries;
    private int totalLive;
    private Instant lastParsedAt;
    private long parseTimeMs;
    private String cacheHitRate;
    private String m3uHost;
    private String m3uUsernameMasked;
    private boolean m3uPasswordSet;
}
