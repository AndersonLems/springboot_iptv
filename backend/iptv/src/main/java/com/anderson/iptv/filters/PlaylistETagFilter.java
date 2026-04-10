package com.anderson.iptv.filters;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.anderson.iptv.model.Playlist;
import com.anderson.iptv.services.ChannelIndex;
import com.anderson.iptv.services.PlaylistService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
public class PlaylistETagFilter implements WebFilter {

    private final PlaylistService playlistService;
    private final ChannelIndex channelIndex;

    public PlaylistETagFilter(PlaylistService playlistService, ChannelIndex channelIndex) {
        this.playlistService = playlistService;
        this.channelIndex = channelIndex;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (!path.startsWith("/api/playlist")) {
            return chain.filter(exchange);
        }

        String cachedEtag = channelIndex.getPlaylistEtag();
        if (cachedEtag != null) {
            exchange.getResponse().getHeaders().setETag(cachedEtag);
            String ifNoneMatch = exchange.getRequest().getHeaders().getFirst("If-None-Match");
            if (ifNoneMatch != null && ifNoneMatch.equals(cachedEtag)) {
                exchange.getResponse().setStatusCode(HttpStatus.NOT_MODIFIED);
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        }

        return Mono.fromCallable(playlistService::getPlaylist)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(playlist -> {
                    String etag = buildEtag(playlist);
                    exchange.getResponse().getHeaders().setETag(etag);

                    String ifNoneMatch = exchange.getRequest().getHeaders().getFirst("If-None-Match");
                    if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
                        exchange.getResponse().setStatusCode(HttpStatus.NOT_MODIFIED);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                });
    }

    private String buildEtag(Playlist playlist) {
        long ts = playlist.getFetchedAt() != null ? playlist.getFetchedAt().toEpochMilli() : 0L;
        return "\"playlist-" + ts + "-" + playlist.getTotalChannels() + "\"";
    }
}
