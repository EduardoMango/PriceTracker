package com.eduardomango.pricetracker.pricehistory;

import com.eduardomango.pricetracker.common.architecture.scraping.ClientOrchestrator;
import com.eduardomango.pricetracker.product.ProductRepository;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Service
@AllArgsConstructor
public class PriceHistoryService {

    private final ProductRepository productRepository;
    private final ClientOrchestrator clientOrchestrator;
    private final PriceUpdateService priceUpdater;


    @Scheduled(cron = "0 0 9,17 * * *") 
    public void updatePriceHistory() {
        List<ProductEntity> products = productRepository.findAll();

        Flux.fromIterable(products)
                .flatMap(product -> clientOrchestrator.getPrice(URI.create(product.getUrl().value()))
                        .doOnNext(newPrice -> priceUpdater.updateSingleProduct(product, newPrice))
                        .onErrorResume(e -> {
                            System.err.println("Error actualizando producto " + product.getId() + ": " + e.getMessage());
                            return Mono.empty();
                        })
                )
                .subscribe();
    }
}
