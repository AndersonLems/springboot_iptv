package com.anderson.iptv.services;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.util.TextNormalizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ChannelIndex {

    private volatile Map<String, List<Channel>> indexByNormalizedName = Map.of();
    private volatile Map<String, List<Channel>> indexByGroup = Map.of();
    private volatile Map<String, Channel> indexById = Map.of();
    private volatile Map<String, List<Channel>> indexByParent = Map.of();
    private volatile Map<String, String> parentDisplayNameByKey = Map.of();
    private volatile List<Channel> allChannels = List.of();
    private volatile String playlistEtag = null;

    public void rebuild(Playlist playlist) {
        if (playlist == null || playlist.getChannels() == null) {
            indexByNormalizedName = Map.of();
            indexByGroup = Map.of();
            indexById = Map.of();
            indexByParent = Map.of();
            parentDisplayNameByKey = Map.of();
            allChannels = List.of();
            playlistEtag = null;
            return;
        }

        Map<String, List<Channel>> nameIndex = new ConcurrentHashMap<>();
        Map<String, List<Channel>> groupIndex = new ConcurrentHashMap<>();
        Map<String, Channel> idIndex = new ConcurrentHashMap<>();
        Map<String, List<Channel>> parentIndex = new ConcurrentHashMap<>();
        Map<String, String> parentDisplay = new ConcurrentHashMap<>();
        List<Channel> channelsSnapshot = new ArrayList<>();

        for (Channel c : playlist.getChannels()) {
            if (c == null) {
                continue;
            }

            channelsSnapshot.add(c);

            if (c.getName() != null) {
                String key = TextNormalizer.normalizeTitle(c.getName());
                if (!key.isBlank()) {
                    nameIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(c);
                }
            }
            if (c.getGroupTitle() != null && !c.getGroupTitle().isBlank()) {
                String groupKey = c.getGroupTitle().toLowerCase().trim();
                groupIndex.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(c);

                String parentRaw = extractParent(c.getGroupTitle()).trim();
                String parentKey = parentRaw.toLowerCase().trim();
                if (!parentKey.isBlank()) {
                    parentIndex.computeIfAbsent(parentKey, k -> new ArrayList<>()).add(c);
                    parentDisplay.putIfAbsent(parentKey, parentRaw);
                }
            }
            if (c.getId() != null && !c.getId().isBlank()) {
                idIndex.putIfAbsent(c.getId(), c);
            }
        }

        indexByNormalizedName = freeze(nameIndex);
        indexByGroup = freeze(groupIndex);
        indexById = Map.copyOf(idIndex);
        indexByParent = freeze(parentIndex);
        parentDisplayNameByKey = Map.copyOf(parentDisplay);
        allChannels = Collections.unmodifiableList(channelsSnapshot);
        playlistEtag = buildPlaylistEtag(playlist);
    }

    public String getPlaylistEtag() {
        return playlistEtag;
    }

    private String buildPlaylistEtag(Playlist playlist) {
        long ts = playlist.getFetchedAt() != null ? playlist.getFetchedAt().toEpochMilli() : 0L;
        return "\"playlist-" + ts + "-" + playlist.getTotalChannels() + "\"";
    }

    public Map<String, List<Channel>> getIndexByNormalizedName() {
        return indexByNormalizedName;
    }

    public Map<String, List<Channel>> getIndexByGroup() {
        return indexByGroup;
    }

    public Map<String, List<Channel>> getIndexByParent() {
        return indexByParent;
    }

    public String getParentDisplayName(String parentKeyOrRaw) {
        if (parentKeyOrRaw == null) {
            return null;
        }
        String key = parentKeyOrRaw.toLowerCase().trim();
        return parentDisplayNameByKey.getOrDefault(key, parentKeyOrRaw);
    }

    public List<Channel> getAllChannels() {
        return allChannels;
    }

    public List<Channel> getByParent(String parent) {
        if (parent == null) {
            return List.of();
        }
        return indexByParent.getOrDefault(parent.toLowerCase().trim(), List.of());
    }

    public Channel getById(String id) {
        if (id == null) {
            return null;
        }
        return indexById.get(id);
    }

    public List<Channel> getByGroup(String groupTitle) {
        if (groupTitle == null) {
            return List.of();
        }
        return indexByGroup.getOrDefault(groupTitle.toLowerCase().trim(), List.of());
    }

    private String extractParent(String group) {
        if (group == null) {
            return "";
        }
        int pipe = group.indexOf('|');
        if (pipe >= 0) {
            return group.substring(0, pipe).trim();
        }
        return group.trim();
    }

    private Map<String, List<Channel>> freeze(Map<String, List<Channel>> input) {
        Map<String, List<Channel>> frozen = new ConcurrentHashMap<>();
        input.forEach((key, list) -> frozen.put(key, Collections.unmodifiableList(list)));
        return Map.copyOf(frozen);
    }
}
