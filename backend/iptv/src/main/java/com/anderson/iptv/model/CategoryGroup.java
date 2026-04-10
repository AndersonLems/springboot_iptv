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
public class CategoryGroup {
    private String parent;
    private List<String> subcategories;
    private int totalChannels;

}
