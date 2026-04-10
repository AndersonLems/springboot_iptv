package com.anderson.iptv.util;

import com.anderson.iptv.model.EnrichedMovie;
import com.anderson.iptv.model.EnrichedSeries;

public final class StreamQualityResolver {

    private StreamQualityResolver() {}

    public static EnrichedMovie.StreamQuality movieQuality(String name) {
        if (name == null) {
            return EnrichedMovie.StreamQuality.OUTRO;
        }
        String n = name.toUpperCase();
        if (n.contains("4K")) {
            return EnrichedMovie.StreamQuality.UHD_4K;
        }
        if (n.contains("HDR")) {
            return EnrichedMovie.StreamQuality.HDR;
        }
        if (n.contains("[L]") || n.contains("LEGENDADO")) {
            return EnrichedMovie.StreamQuality.LEGENDADO;
        }
        if (n.contains("DUBLADO")) {
            return EnrichedMovie.StreamQuality.DUBLADO;
        }
        return EnrichedMovie.StreamQuality.DUBLADO;
    }

    public static EnrichedSeries.StreamQuality seriesQuality(String name) {
        if (name == null) {
            return EnrichedSeries.StreamQuality.OUTRO;
        }
        String n = name.toUpperCase();
        if (n.contains("4K")) {
            return EnrichedSeries.StreamQuality.UHD_4K;
        }
        if (n.contains("HDR")) {
            return EnrichedSeries.StreamQuality.HDR;
        }
        if (n.contains("[L]") || n.contains("LEGENDADO")) {
            return EnrichedSeries.StreamQuality.LEGENDADO;
        }
        if (n.contains("DUBLADO")) {
            return EnrichedSeries.StreamQuality.DUBLADO;
        }
        return EnrichedSeries.StreamQuality.OUTRO;
    }
}
