package com.anderson.iptv.client;

import com.anderson.iptv.config.AppProperties;
import com.anderson.iptv.model.tmdb.TmdbMovie;
import com.anderson.iptv.model.tmdb.TmdbPageResult;
import com.anderson.iptv.model.tmdb.TmdbSeries;
import com.fasterxml.jackson.databind.JsonNode;
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

        public TmdbPageResult<TmdbSeries> popularSeries(int page) {
                return fetch("/tv/popular", new ParameterizedTypeReference<>() {
                }, page);
        }

        public TmdbPageResult<TmdbSeries> topRatedSeries(int page) {
                return fetch("/tv/top_rated", new ParameterizedTypeReference<>() {
                }, page);
        }

        public JsonNode movieDetails(long id) {
                return fetchNode("/movie/" + id);
        }

        public JsonNode movieCredits(long id) {
                return fetchNode("/movie/" + id + "/credits");
        }

        public JsonNode movieVideos(long id) {
                return fetchNode("/movie/" + id + "/videos");
        }

        public JsonNode seriesDetails(long id) {
                return fetchNode("/tv/" + id);
        }

        public JsonNode seriesCredits(long id) {
                return fetchNode("/tv/" + id + "/credits");
        }

        public JsonNode seriesVideos(long id) {
                return fetchNode("/tv/" + id + "/videos");
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

        private JsonNode fetchNode(String path) {
                return webClient.get()
                                .uri(u -> u.path(path)
                                                .queryParam("language", props.getTmdb().getLanguage())
                                                .queryParam("region", props.getTmdb().getRegion())
                                                .build())
                                .retrieve()
                                .bodyToMono(JsonNode.class)
                                .block();
        }
}
