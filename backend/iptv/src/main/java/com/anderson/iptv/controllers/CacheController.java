package com.anderson.iptv.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        cacheManager.getCacheNames().forEach(name -> {
            if (name.startsWith("iptv")) {
                cacheManager.getCache(name).clear();
            }
        });
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Caches limpos com sucesso"));
    }

}
