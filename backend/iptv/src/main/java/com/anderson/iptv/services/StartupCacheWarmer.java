package com.anderson.iptv.services;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.anderson.iptv.config.CacheToggle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.cache.warmup.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class StartupCacheWarmer implements ApplicationRunner {

    private final PlaylistService playlistService;
    private final ChannelIndex channelIndex;
    private final ApplicationContext applicationContext;
    private final CacheToggle cacheToggle;

    @Override
    public void run(ApplicationArguments args) {
        Mono.fromRunnable(this::warmup)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private void warmup() {
        if (applicationContext instanceof ConfigurableApplicationContext ctx && !ctx.isActive()) {
            return;
        }
        if (!cacheToggle.enabled()) {
            log.info("Cache warmup skipped: cache disabled for devtools");
            return;
        }
        try {
            log.info("Cache warmup: fetch start");
            var playlist = playlistService.getPlaylist();
            if (playlist == null) {
                playlist = playlistService.forceRefresh();
            }
            channelIndex.rebuild(playlist);
            log.info("Cache warmup: parse complete");

            List<CategoryGroupSnapshot> groups = playlistService.getGroupedCategories().stream()
                    .map(g -> new CategoryGroupSnapshot(g.getParent(), g.getSubcategories()))
                    .toList();

            for (CategoryGroupSnapshot group : groups) {
                playlistService.getChannelsByCategory(group.parent, null);
                for (String sub : group.subs) {
                    playlistService.getChannelsByCategory(group.parent, sub);
                }
            }

            log.info("Cache warmup: cache populated");
        } catch (ClassCastException e) {
            log.warn("Cache warmup retry due to classloader mismatch: {}", e.getMessage());
            try {
                var playlist = playlistService.forceRefresh();
                channelIndex.rebuild(playlist);
            } catch (Exception ex) {
                log.warn("Cache warmup skipped: {}", ex.getMessage());
            }
        } catch (Exception e) {
            log.warn("Cache warmup skipped: {}", e.getMessage());
        }
    }

    private record CategoryGroupSnapshot(String parent, List<String> subs) {}
}
