package com.anderson.iptv.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryGroup {
private String parent;
private List<String> subcategories;
private int totalChannels;


}
