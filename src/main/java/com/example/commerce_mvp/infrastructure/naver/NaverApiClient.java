package com.example.commerce_mvp.infrastructure.naver;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class NaverApiClient {
    @Value("${naver.api.client-id}")
    private String clientId;
    @Value("${naver.api.client-secret}")
    private String clientSecret;
    private final RestTemplate restTemplate = new RestTemplate();

    public NaverSearchResponseDto search(String query){
        URI uri = UriComponentsBuilder.fromUriString("https://openapi.naver.com").path("/v1/search/shop.json")
                .queryParam("query", query)
                .queryParam("display", 100)
                .encode().build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(uri, HttpMethod.GET, entity, NaverSearchResponseDto.class).getBody();

    }
}
