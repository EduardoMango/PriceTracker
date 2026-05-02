package com.eduardomango.pricetracker.common.config;

import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;


@Configuration
public class ClientConfig {

    @Bean
    public WebClientCustomizer webClientCustomizer() {
        return builder -> builder
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (PriceTracker/1.0)")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);
    }

}
