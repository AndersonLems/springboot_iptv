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
public class SearchResponse {
    private String query;
    private List<Channel> movies;
    private List<Channel> series;
    private List<Channel> live;
    private int totalResults;
}
