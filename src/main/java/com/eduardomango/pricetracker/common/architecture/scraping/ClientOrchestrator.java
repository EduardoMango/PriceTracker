package com.eduardomango.pricetracker.common.architecture.scraping;

import com.eduardomango.pricetracker.common.exceptions.UnsuportedWebsite;
import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
public class ClientOrchestrator {

    private final List<ClientService> clients;

    public ClientOrchestrator(List<ClientService> clients) {
        this.clients = clients;
    }


    public ProductEntity getProduct(URI url) {

        ProductEntity product = null;

        for (ClientService clientService : clients) {
            if (clientService.supports(url)) {
                product = clientService.getProduct(url);
                break;
            }
        }

        if (product == null) {
            throw new UnsuportedWebsite(url.getHost());
        }

        return product;
    }

    public Price getPrice(URI url) {

        return clients.stream()
                .filter(client -> client.supports(url)) //Filter for supporting client
                .findFirst()
                .orElseThrow()
                .getPrice(url);
    }
}
