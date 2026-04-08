package com.anderson.iptv.client;

import com.anderson.iptv.config.AppProperties;
import com.anderson.iptv.model.tmdb.TmdbMovie;
import com.anderson.iptv.model.tmdb.TmdbPageResult;
import com.anderson.iptv.model.tmdb.TmdbSeries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class TmdbClient {

        private final WebClient webClient;
        private final AppProperties props;

        @Autowired
        public TmdbClient(@Qualifier("tmdbWebClient") WebClient webClient,
                        AppProperties props) {
                this.webClient = webClient;
                this.props = props;
        }

        public TmdbPageResult<TmdbMovie> trendingMovies() {
                return fetch("/trending/movie/week", new ParameterizedTypeReference<>() {
                }, 1);
        }

        public TmdbPageResult<TmdbMovie> topRatedMovies(int page) {
                return fetch("/movie/top_rated", new ParameterizedTypeReference<>() {
                }, page);
        }

        public TmdbPageResult<TmdbMovie> popularMovies(int page) {
                return fetch("/movie/popular", new ParameterizedTypeReference<>() {
                }, page);
        }

        public TmdbPageResult<TmdbSeries> trendingSeries() {
                return fetch("/trending/tv/week", new ParameterizedTypeReference<>() {
                }, 1);
        }

        public TmdbPageResult<TmdbSeries> topRatedSeries(int page) {
                return fetch("/tv/top_rated", new ParameterizedTypeReference<>() {
                }, page);
        }

        private <T> TmdbPageResult<T> fetch(String path,
                        ParameterizedTypeReference<TmdbPageResult<T>> ref, int page) {
                return webClient.get()
                                .uri(u -> u.path(path)
                                                .queryParam("language", props.getTmdb().getLanguage())
                                                .queryParam("region", props.getTmdb().getRegion())
                                                .queryParam("page", page)
                                                .build())
                                .retrieve()
                                .bodyToMono(ref)
                                .block();
        }
}