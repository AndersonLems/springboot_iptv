package com.anderson.iptv.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.WatchHistoryItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

class HistoryServiceTest {

    private StringRedisTemplate redisTemplate;
    private ZSetOperations<String, String> zsetOps;
    private TreeMap<Double, String> store;
    private ChannelIndex channelIndex;
    private HistoryService historyService;

    @BeforeEach
    void setup() {
        redisTemplate = Mockito.mock(StringRedisTemplate.class);
        zsetOps = Mockito.mock(ZSetOperations.class);
        store = new TreeMap<>();
        channelIndex = new ChannelIndex();
        historyService = new HistoryService(redisTemplate, channelIndex);

        Mockito.when(redisTemplate.opsForZSet()).thenReturn(zsetOps);
        Mockito.doAnswer(invocation -> {
            String value = invocation.getArgument(1);
            Double score = invocation.getArgument(2);
            store.put(score, value);
            return true;
        }).when(zsetOps).add(Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble());
        Mockito.doAnswer(invocation -> {
            String value = invocation.getArgument(1);
            store.values().removeIf(v -> v.equals(value));
            return null;
        }).when(zsetOps).remove(Mockito.anyString(), Mockito.anyString());
        Mockito.when(zsetOps.reverseRangeWithScores(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong()))
                .thenAnswer(invocation -> {
                    List<ZSetOperations.TypedTuple<String>> tuples = new ArrayList<>();
                    store.descendingMap().forEach((score, value) -> {
                        tuples.add(new SimpleTuple(value, score));
                    });
                    return tuples.stream().limit(50).collect(Collectors.toSet());
                });

        channelIndex.rebuild(com.anderson.iptv.model.Playlist.builder().channels(List.of(
                Channel.builder().id("1").name("Avatar").streamUrl("s1").build(),
                Channel.builder().id("2").name("Lost").streamUrl("s2").build()
        )).totalChannels(2).build());
    }

    @Test
    void testRecordWatch() {
        historyService.recordWatch("dev1", "1");
        assertEquals(1, store.size());
    }

    @Test
    void testGetHistoryReturnsLast50OrderedByRecent() {
        store.put(1.0, "1");
        store.put(2.0, "2");
        List<WatchHistoryItem> history = historyService.getHistory("dev1");
        assertEquals(2, history.size());
        assertEquals("2", history.get(0).getChannel().getId());
    }

    @Test
    void testClearHistory() {
        store.put(1.0, "1");
        historyService.clearHistory("dev1");
        Mockito.verify(redisTemplate).delete("history:dev1");
    }

    @Test
    void testWatchedAtIsIso8601Format() {
        store.put((double) Instant.now().toEpochMilli(), "1");
        List<WatchHistoryItem> history = historyService.getHistory("dev1");
        String iso = history.get(0).getWatchedAt().toString();
        assertTrue(iso.contains("T"));
    }

    private static class SimpleTuple implements ZSetOperations.TypedTuple<String> {
        private final String value;
        private final Double score;

        SimpleTuple(String value, Double score) {
            this.value = value;
            this.score = score;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public Double getScore() {
            return score;
        }

        @Override
        public int compareTo(ZSetOperations.TypedTuple<String> o) {
            return Comparator.comparingDouble((ZSetOperations.TypedTuple<String> t) -> t.getScore()).compare(this, o);
        }
    }
}
