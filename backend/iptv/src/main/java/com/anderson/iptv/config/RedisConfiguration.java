package com.anderson.iptv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

import com.anderson.iptv.model.Channel;
import com.anderson.iptv.model.EnrichedSeries;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@EnableCaching
@EnableScheduling
public class RedisConfiguration {
    public static final String NEWS_KEY = "iptv:news";
    public static final String MOVIES_CHANNELS_KEY = "iptv:movies/channels";
    public static final String SERIES_TRENDING_KEY = "iptv:series:trending";
    public static final String SERIES_TOP_RATED_KEY = "iptv:series:top-rated";
    public static final String SERIES_POPULAR_KEY = "iptv:series:popular";
    public static final String SERIES_PLAYLIST_KEY = "iptv:series:playlist";
    public static final String MOVIES_TRENDING_KEY = "iptv:movies:trending";
    public static final String MOVIES_TOP_RATED_KEY = "iptv:movies:top-rated";
    public static final String MOVIES_POPULAR_KEY = "iptv:movies:popular";
    public static final String MOVIES_ALL_KEY = "iptv:movies:all";
    public static final String SERIES_ALL_KEY = "iptv:series:all";
    public static final String LIVE_KEY = "iptv:live";
    public static final String SEARCH_KEY = "iptv:search";
    public static final String EPG_KEY = "iptv:epg";
    public static final String PLAYLIST_CATEGORIES_KEY = "iptv:playlist:categories";

    @Value("${cache.ttl.ms:300000}")
    private long cacheTtlMs;

    @CacheEvict(allEntries = true, value = NEWS_KEY)
    @Scheduled(fixedDelayString = "${cache.ttl.ms}")
    public void evictNewsCache() {
        log.info("Evicting news cache");
    }

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper channelMapper = new ObjectMapper();
        channelMapper.registerModule(new JavaTimeModule());
        RedisSerializer<List<Channel>> channelListSerializer =
                new ChannelListRedisSerializer(channelMapper);

        ObjectMapper seriesMapper = new ObjectMapper();
        seriesMapper.registerModule(new JavaTimeModule());
        RedisSerializer<List<EnrichedSeries>> seriesListSerializer =
                new EnrichedSeriesListRedisSerializer(seriesMapper);

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration())
                .withCacheConfiguration(MOVIES_CHANNELS_KEY,
                        cacheConfiguration()
                                .entryTtl(Duration.ofMillis(cacheTtlMs))
                                .serializeValuesWith(
                                        RedisSerializationContext.SerializationPair
                                                .fromSerializer(channelListSerializer)))
                .withCacheConfiguration(MOVIES_TRENDING_KEY,
                        cacheConfiguration().entryTtl(Duration.ofMillis(cacheTtlMs)))
                .withCacheConfiguration(MOVIES_TOP_RATED_KEY,
                        cacheConfiguration().entryTtl(Duration.ofMillis(cacheTtlMs)))
                .withCacheConfiguration(MOVIES_POPULAR_KEY,
                        cacheConfiguration().entryTtl(Duration.ofMillis(cacheTtlMs)))
                .withCacheConfiguration(MOVIES_ALL_KEY,
                        cacheConfiguration().entryTtl(Duration.ofMillis(cacheTtlMs)))
                .withCacheConfiguration(SERIES_TRENDING_KEY,
                        cacheConfiguration().serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(seriesListSerializer)))
                .withCacheConfiguration(SERIES_TOP_RATED_KEY,
                        cacheConfiguration().serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(seriesListSerializer)))
                .withCacheConfiguration(SERIES_POPULAR_KEY,
                        cacheConfiguration().serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(seriesListSerializer)))
                .withCacheConfiguration(SERIES_PLAYLIST_KEY,
                        cacheConfiguration().serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(seriesListSerializer)))
                .withCacheConfiguration(SERIES_ALL_KEY,
                        cacheConfiguration().entryTtl(Duration.ofMillis(cacheTtlMs)))
                .withCacheConfiguration(LIVE_KEY,
                        cacheConfiguration().entryTtl(Duration.ofMillis(cacheTtlMs)))
                .withCacheConfiguration(SEARCH_KEY,
                        cacheConfiguration().entryTtl(Duration.ofMinutes(2)))
                .withCacheConfiguration(EPG_KEY,
                        cacheConfiguration().entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration(PLAYLIST_CATEGORIES_KEY,
                        cacheConfiguration().entryTtl(Duration.ofMillis(cacheTtlMs)))
                .build();
    }

    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Erro ao ler cache '{}'. Evict da chave {}: {}",
                        cache != null ? cache.getName() : "desconhecido",
                        key,
                        exception.getMessage());
                if (cache != null && key != null) {
                    cache.evict(key);
                }
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Erro ao gravar cache '{}': {}",
                        cache != null ? cache.getName() : "desconhecido",
                        exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Erro ao evict cache '{}': {}",
                        cache != null ? cache.getName() : "desconhecido",
                        exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Erro ao limpar cache '{}': {}",
                        cache != null ? cache.getName() : "desconhecido",
                        exception.getMessage());
            }
        };
    }

    private static class ChannelListRedisSerializer implements RedisSerializer<List<Channel>> {
        private final ObjectMapper mapper;
        private final TypeReference<List<Channel>> typeRef = new TypeReference<>() {};

        private ChannelListRedisSerializer(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public byte[] serialize(List<Channel> value) throws SerializationException {
            if (value == null) {
                return new byte[0];
            }
            try {
                return mapper.writeValueAsBytes(value);
            } catch (Exception e) {
                throw new SerializationException("Erro ao serializar lista de canais", e);
            }
        }

        @Override
        public List<Channel> deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return mapper.readValue(bytes, typeRef);
            } catch (Exception e) {
                throw new SerializationException("Erro ao desserializar lista de canais", e);
            }
        }
    }

    private static class EnrichedSeriesListRedisSerializer implements RedisSerializer<List<EnrichedSeries>> {
        private final ObjectMapper mapper;
        private final TypeReference<List<EnrichedSeries>> typeRef = new TypeReference<>() {};

        private EnrichedSeriesListRedisSerializer(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public byte[] serialize(List<EnrichedSeries> value) throws SerializationException {
            if (value == null) {
                return new byte[0];
            }
            try {
                return mapper.writeValueAsBytes(value);
            } catch (Exception e) {
                throw new SerializationException("Erro ao serializar lista de series", e);
            }
        }

        @Override
        public List<EnrichedSeries> deserialize(byte[] bytes) throws SerializationException {
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            try {
                return mapper.readValue(bytes, typeRef);
            } catch (Exception e) {
                throw new SerializationException("Erro ao desserializar lista de series", e);
            }
        }
    }

}
