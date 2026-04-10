package com.anderson.iptv.services;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.CategoryGroup;
import com.anderson.iptv.model.Playlist;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import com.anderson.iptv.config.RedisConfiguration;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistService {

    private final PlaylistCacheService playlistCacheService;
    private final PlaylistMetrics metrics;
    private final ChannelIndex channelIndex;

    public PlaylistService(PlaylistCacheService playlistCacheService,
            PlaylistMetrics metrics,
            ChannelIndex channelIndex) {
        this.playlistCacheService = playlistCacheService;
        this.metrics = metrics;
        this.channelIndex = channelIndex;
    }

    public Playlist getPlaylist() {
        metrics.recordRequest();
        Playlist playlist;
        try {
            playlist = playlistCacheService.getPlaylist();
        } catch (ClassCastException e) {
            playlist = playlistCacheService.forceRefresh();
        }
        if (channelIndex.getIndexByGroup().isEmpty() && playlist.getChannels() != null
                && !playlist.getChannels().isEmpty()) {
            channelIndex.rebuild(playlist);
        }
        return playlist;
    }

    public List<String> getGroups() {
        return channelIndex.getIndexByGroup().keySet().stream()
                .filter(g -> g != null && !g.isBlank())
                .sorted()
                .toList();
    }

    public List<Channel> byGroup(String group) {
        return channelIndex.getByGroup(group);
    }

    public List<Channel> search(String query) {
        String q = query == null ? "" : query.toLowerCase();
        ensureIndexReady();
        return channelIndex.getAllChannels().stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(q))
                .toList();
    }

    @CacheEvict(cacheNames = {
            "iptv:movies:trending",
            "iptv:movies:top-rated",
            "iptv:movies:popular",
            "iptv:movies:all",
            "iptv:series:trending",
            "iptv:series:top-rated",
            "iptv:series:popular",
            "iptv:series:playlist",
            "iptv:series:all",
            "iptv:live",
            "iptv:search",
            "iptv:playlist:categories",
            "iptv:movies/channels"
    }, allEntries = true)
    public Playlist forceRefresh() {
        Playlist playlist = playlistCacheService.forceRefresh();
        channelIndex.rebuild(playlist);
        return playlist;
    }

    @Cacheable(value = "iptv:playlist:categories", condition = "@cacheToggle.enabled()")
    public List<CategoryGroup> getGroupedCategories() {
        ensureIndexReady();

        Map<String, List<Channel>> byParent = channelIndex.getIndexByParent();
        if (byParent.isEmpty()) {
            return List.of();
        }

        return byParent.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank())
                .map(entry -> {
                    String parent = channelIndex.getParentDisplayName(entry.getKey());
                    List<Channel> channels = entry.getValue();

                    List<String> subs = channels.stream()
                            .map(c -> extractSub(c.getGroupTitle()))
                            .filter(s -> s != null && !s.isBlank())
                            .distinct()
                            .sorted()
                            .toList();

                    return CategoryGroup.builder()
                            .parent(parent)
                            .subcategories(subs)
                            .totalChannels(channels.size())
                            .build();
                })
                .sorted(Comparator.comparing(CategoryGroup::getParent))
                .toList();
    }

    @Cacheable(
            value = RedisConfiguration.MOVIES_CHANNELS_KEY,
            key = "#sub == null || #sub.isBlank() ? #parent : #parent + ':' + #sub",
            condition = "@cacheToggle.enabled()")
    public List<Channel> getChannelsByCategory(String parent, String sub) {
        ensureIndexReady();

        if (parent == null || parent.isBlank()) {
            return List.of();
        }

        if (sub == null || sub.isBlank()) {
            return channelIndex.getByParent(parent);
        }

        String fullGroup = parent + " | " + sub;
        return channelIndex.getByGroup(fullGroup);
    }

    public List<Channel> searchChannelsByCategory(String parent, String query) {
        ensureIndexReady();

        String q = query == null ? "" : query.toLowerCase();
        return channelIndex.getByParent(parent).stream()
                .filter(c -> c.getName() != null && c.getName().toLowerCase().contains(q))
                .toList();
    }

    private void ensureIndexReady() {
        if (channelIndex.getIndexByGroup().isEmpty()) {
            Playlist playlist = playlistCacheService.getPlaylist();
            if (playlist != null && playlist.getChannels() != null && !playlist.getChannels().isEmpty()) {
                channelIndex.rebuild(playlist);
            }
        }
    }

    private String extractSub(String group) {
        if (group.contains("|")) {
            return group.substring(group.indexOf("|") + 1).trim();
        }
        return group.trim();
    }

}
