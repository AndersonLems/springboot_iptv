package com.anderson.iptv.services;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.LiveChannel;
import com.anderson.iptv.model.PaginatedResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class LiveChannelService {

    private static final String LIVE_PREFIX = "canais |";

    private final ChannelIndex channelIndex;
    private final PlaylistService playlistService;

    public LiveChannelService(ChannelIndex channelIndex, PlaylistService playlistService) {
        this.channelIndex = channelIndex;
        this.playlistService = playlistService;
    }

    @Cacheable(
            value = "iptv:live",
            key = "#page + ':' + #size + ':' + (#category == null ? '' : #category)",
            condition = "@cacheToggle.enabled()")
    public PaginatedResponse<LiveChannel> getLive(String category, int page, int size) {
        if (channelIndex.getIndexByGroup().isEmpty()) {
            playlistService.getPlaylist();
        }
        List<Channel> all = channelIndex.getAllChannels();
        List<Channel> liveCandidates = new ArrayList<>();
        channelIndex.getIndexByGroup().forEach((group, list) -> {
            if (group != null && group.startsWith(LIVE_PREFIX)) {
                liveCandidates.addAll(list);
            }
        });
        List<Channel> source = liveCandidates.isEmpty() ? all : liveCandidates;

        List<LiveChannel> live = source.stream()
                .filter(c -> c.getGroupTitle() != null
                        && c.getGroupTitle().toLowerCase(Locale.ROOT).startsWith(LIVE_PREFIX))
                .map(this::toLiveChannel)
                .filter(l -> category == null || category.isBlank()
                        || l.getCategory().equalsIgnoreCase(category))
                .toList();

        int safePage = Math.max(0, page);
        int safeSize = Math.min(200, Math.max(1, size));
        int total = live.size();
        int from = Math.min(safePage * safeSize, total);
        int to = Math.min(from + safeSize, total);

        return PaginatedResponse.<LiveChannel>builder()
                .content(live.subList(from, to))
                .page(safePage)
                .size(safeSize)
                .totalElements(total)
                .totalPages((int) Math.ceil(total / (double) safeSize))
                .sort("name")
                .order("asc")
                .build();
    }

    private LiveChannel toLiveChannel(Channel channel) {
        String category = extractCategory(channel.getGroupTitle());
        return LiveChannel.builder()
                .id(channel.getId())
                .name(channel.getName())
                .logoUrl(channel.getLogoUrl())
                .groupTitle(channel.getGroupTitle())
                .streamUrl(channel.getStreamUrl())
                .isLive(Boolean.TRUE)
                .category(category)
                .build();
    }

    private String extractCategory(String groupTitle) {
        if (groupTitle == null) {
            return "";
        }
        String[] parts = groupTitle.split("\\|");
        if (parts.length < 2) {
            return groupTitle.trim();
        }
        return parts[1].trim();
    }
}
