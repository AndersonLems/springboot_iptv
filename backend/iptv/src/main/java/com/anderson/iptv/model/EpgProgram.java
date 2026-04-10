package com.anderson.iptv.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EpgProgram {
    private String channelId;
    private String title;
    private String description;
    private Instant startTime;
    private Instant endTime;
    private boolean isCurrentlyAiring;
}
