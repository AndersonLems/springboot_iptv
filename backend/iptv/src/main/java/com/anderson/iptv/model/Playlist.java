package com.anderson.iptv.model;

import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Playlist {
    private List<Channel> channels;
    private Instant fetchedAt;
    private int totalChannels;

}
