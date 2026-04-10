package com.anderson.iptv.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedChannel {
    private String name;
    private String logoUrl;
    private String groupTitle;
    private List<StreamOption> streams;
    private boolean available;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamOption {
        private String name;
        private String streamUrl;
        private String groupTitle;
        private String logoUrl;
        private StreamQuality quality;
    }

    public enum StreamQuality {
        UHD_4K,
        HDR,
        LEGENDADO,
        DUBLADO,
        OUTRO
    }
}
