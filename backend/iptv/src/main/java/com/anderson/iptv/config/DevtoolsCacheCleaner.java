package com.anderson.iptv.config;

import java.lang.reflect.Method;

import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DevtoolsCacheCleaner {

    private final CacheManager cacheManager;

    public DevtoolsCacheCleaner(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        if (!isDevtoolsRestartActive()) {
            return;
        }
        try {
            for (String name : cacheManager.getCacheNames()) {
                var cache = cacheManager.getCache(name);
                if (cache != null) {
                    cache.clear();
                }
            }
            log.info("Devtools detected: cleared caches to avoid classloader mismatches");
        } catch (Exception e) {
            log.warn("Devtools cache clear failed: {}", e.getMessage());
        }
    }

    private boolean isDevtoolsRestartActive() {
        try {
            Class<?> restarterClass = Class.forName("org.springframework.boot.devtools.restart.Restarter");
            Method getInstance = restarterClass.getMethod("getInstance");
            Object restarter = getInstance.invoke(null);
            if (restarter == null) {
                return false;
            }
            Method isRestartEnabled = restarterClass.getMethod("isRestartEnabled");
            Object enabled = isRestartEnabled.invoke(restarter);
            return enabled instanceof Boolean && (Boolean) enabled;
        } catch (Exception e) {
            return false;
        }
    }
}
