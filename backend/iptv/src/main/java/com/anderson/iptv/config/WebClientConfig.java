package com.anderson.iptv.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    @Qualifier("tmdbWebClient")
    public WebClient tmdbWebClient(AppProperties props) {
        return WebClient.builder()
                .baseUrl(props.getTmdb().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.getTmdb().getApiKey())
                .defaultHeader("Accept", "application/json")
                .build();

    }

    @Bean
    @Qualifier("genericWebClient")
    public WebClient genericWebClient() {
        return WebClient.builder()
                .codecs(cfg -> cfg.defaultCodecs()
                        .maxInMemorySize(200 * 1024 * 1024))
                .build();
    }

}
