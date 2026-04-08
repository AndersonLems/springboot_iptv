package com.anderson.iptv.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Channel {
    private String id;
    private String name;
    private String logoUrl;
    private String groupTitle;
    private String streamUrl;
    private double duration;
}
