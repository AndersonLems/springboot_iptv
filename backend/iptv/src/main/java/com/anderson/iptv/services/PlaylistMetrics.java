package com.anderson.iptv.services;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public class PlaylistMetrics {

    private final AtomicLong requestCount = new AtomicLong();
    private final AtomicLong parseCount = new AtomicLong();
    private final AtomicLong parseTimeMs = new AtomicLong();
    private final AtomicReference<Instant> lastParsedAt = new AtomicReference<>();

    public void recordRequest() {
        requestCount.incrementAndGet();
    }

    public void recordParse(long durationMs, Instant parsedAt) {
        parseCount.incrementAndGet();
        parseTimeMs.set(durationMs);
        lastParsedAt.set(parsedAt);
    }

    public long getRequestCount() {
        return requestCount.get();
    }

    public long getParseCount() {
        return parseCount.get();
    }

    public long getParseTimeMs() {
        return parseTimeMs.get();
    }

    public Instant getLastParsedAt() {
        return lastParsedAt.get();
    }

    public double getHitRatePercent() {
        long requests = requestCount.get();
        if (requests == 0) {
            return 0;
        }
        long misses = parseCount.get();
        long hits = Math.max(0, requests - misses);
        return (hits * 100.0) / requests;
    }
}
