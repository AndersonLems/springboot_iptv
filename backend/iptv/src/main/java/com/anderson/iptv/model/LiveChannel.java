package com.anderson.iptv.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveChannel {
    private String id;
    private String name;
    private String logoUrl;
    private String groupTitle;
    private String streamUrl;
    private Boolean isLive;
    private String category;
}
