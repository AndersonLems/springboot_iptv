package com.anderson.iptv.services;

import com.anderson.iptv.model.Channel;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class FavoritesService {

    private final StringRedisTemplate redisTemplate;
    private final ChannelIndex channelIndex;

    public FavoritesService(StringRedisTemplate redisTemplate, ChannelIndex channelIndex) {
        this.redisTemplate = redisTemplate;
        this.channelIndex = channelIndex;
    }

    public void addFavorite(String deviceId, String channelId) {
        redisTemplate.opsForSet().add(key(deviceId), channelId);
    }

    public void removeFavorite(String deviceId, String channelId) {
        redisTemplate.opsForSet().remove(key(deviceId), channelId);
    }

    public List<Channel> getFavorites(String deviceId) {
        Set<String> ids = redisTemplate.opsForSet().members(key(deviceId));
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .map(channelIndex::getById)
                .filter(c -> c != null)
                .toList();
    }

    private String key(String deviceId) {
        return "favorites:" + deviceId;
    }
}
