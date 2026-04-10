package com.anderson.iptv.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private M3u m3u = new M3u();
    private Tmdb tmdb = new Tmdb();
    private Epg epg = new Epg();

    public M3u getM3u() {
        return m3u;
    }

    public Tmdb getTmdb() {
        return tmdb;
    }

    public Epg getEpg() {
        return epg;
    }

    @Data
    public static class M3u {
        private String host;
        private String username;
        private String password;
        private long cacheTtlMS = 300_000L;

        public String buildUrl() {
            return host + "/get.php"
                    + "?username=" + username
                    + "&password=" + password
                    + "&type=m3u_plus"
                    + "&output=m3u8";
        }

    }

    @Data
    public static class Tmdb {
        private String baseUrl;
        private String apiKey;
        private String language = "pt-BR";
        private String region = "BR";
    }

    @Data
    public static class Epg {
        private String url;
    }
}
