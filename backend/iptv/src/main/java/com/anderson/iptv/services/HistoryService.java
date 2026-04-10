package com.anderson.iptv.services;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.WatchHistoryItem;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
public class HistoryService {

    private final StringRedisTemplate redisTemplate;
    private final ChannelIndex channelIndex;

    public HistoryService(StringRedisTemplate redisTemplate, ChannelIndex channelIndex) {
        this.redisTemplate = redisTemplate;
        this.channelIndex = channelIndex;
    }

    public void recordWatch(String deviceId, String channelId) {
        long ts = Instant.now().toEpochMilli();
        redisTemplate.opsForZSet().add(key(deviceId), channelId, ts);
    }

    public List<WatchHistoryItem> getHistory(String deviceId) {
        Set<TypedTuple<String>> items = redisTemplate.opsForZSet()
                .reverseRangeWithScores(key(deviceId), 0, 49);
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        List<WatchHistoryItem> result = new ArrayList<>();
        for (TypedTuple<String> item : items) {
            String id = item.getValue();
            if (id == null) {
                continue;
            }
            Channel channel = channelIndex.getById(id);
            if (channel == null) {
                continue;
            }
            Instant watchedAt = item.getScore() != null
                    ? Instant.ofEpochMilli(item.getScore().longValue())
                    : Instant.now();
            result.add(WatchHistoryItem.builder()
                    .channel(channel)
                    .watchedAt(watchedAt)
                    .build());
        }
        return result;
    }

    public void clearHistory(String deviceId) {
        redisTemplate.delete(key(deviceId));
    }

    public void removeFromHistory(String deviceId, String channelId) {
        redisTemplate.opsForZSet().remove(key(deviceId), channelId);
    }

    private String key(String deviceId) {
        return "history:" + deviceId;
    }
}
