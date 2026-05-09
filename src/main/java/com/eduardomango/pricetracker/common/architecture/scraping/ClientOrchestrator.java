package com.eduardomango.pricetracker.common.architecture.scraping;

import com.eduardomango.pricetracker.common.exceptions.UnsuportedWebsite;
import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Service
public class ClientOrchestrator {

    private final List<ClientService> clients;

    public ClientOrchestrator(List<ClientService> clients) {
        this.clients = clients;
    }


    public Mono<ProductEntity> getProduct(URI url) {

        return clients.stream()
                .filter(client -> client.supports(url))
                .findFirst()
                .map(client -> client.getProduct(url))
                .orElseGet(() -> Mono.error(new UnsuportedWebsite(url.getHost())));
    }

    public Mono<Price> getPrice(URI url) {

        return clients.stream()
                .filter(client -> client.supports(url))
                .findFirst()
                .map(client -> client.getPrice(url))
                .orElseGet(() -> Mono.error(new UnsuportedWebsite(url.getHost())));
    }
}
