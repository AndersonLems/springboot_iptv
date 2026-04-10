package com.anderson.iptv.filters;

import java.time.Duration;
import java.util.Optional;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import org.springframework.data.redis.core.StringRedisTemplate;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class RateLimitFilter implements WebFilter {

    private final StringRedisTemplate redisTemplate;

    public RateLimitFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        HttpMethod method = exchange.getRequest().getMethod();

        RateLimitRule rule = matchRule(path, method);
        if (rule == null) {
            return chain.filter(exchange);
        }

        String ip = resolveIp(exchange);
        String key = "ratelimit:" + rule.key + ":" + ip;

        return Mono.fromCallable(() -> {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(rule.windowSeconds));
            }
            Long ttl = redisTemplate.getExpire(key);
            long retry = (ttl == null || ttl < 0) ? rule.windowSeconds : ttl;
            return new RateLimitResult(count != null ? count : 0, retry);
        }).subscribeOn(Schedulers.boundedElastic()).flatMap(result -> {
            if (result.count > rule.maxRequests) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().add("Retry-After", String.valueOf(result.retryAfterSeconds));
                return response.setComplete();
            }
            return chain.filter(exchange);
        });
    }

    private RateLimitRule matchRule(String path, HttpMethod method) {
        if (HttpMethod.POST.equals(method) && "/api/playlist/refresh".equals(path)) {
            return new RateLimitRule("playlist-refresh", 5, 60);
        }
        if (HttpMethod.POST.equals(method) && path.startsWith("/api/history")) {
            return new RateLimitRule("history", 60, 60);
        }
        return null;
    }

    private String resolveIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
    }

    private record RateLimitRule(String key, long maxRequests, long windowSeconds) {}

    private record RateLimitResult(long count, long retryAfterSeconds) {}
}
