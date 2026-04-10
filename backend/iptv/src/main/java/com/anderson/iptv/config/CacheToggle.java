package com.anderson.iptv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("cacheToggle")
public class CacheToggle {

    private final boolean cacheEnabled;
    private final boolean allowDevtools;

    public CacheToggle(@Value("${app.cache.enabled:true}") boolean cacheEnabled,
            @Value("${app.cache.allow-devtools:false}") boolean allowDevtools) {
        this.cacheEnabled = cacheEnabled;
        this.allowDevtools = allowDevtools;
    }

    public boolean enabled() {
        if (!cacheEnabled) {
            return false;
        }
        if (!allowDevtools && isDevtoolsPresent()) {
            return false;
        }
        return true;
    }

    private boolean isDevtoolsPresent() {
        try {
            Class.forName("org.springframework.boot.devtools.restart.Restarter");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
