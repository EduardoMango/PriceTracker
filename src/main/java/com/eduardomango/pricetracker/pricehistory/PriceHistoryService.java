package com.eduardomango.pricetracker.pricehistory;

import com.eduardomango.pricetracker.common.architecture.scraping.ClientOrchestrator;
import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.product.ProductRepository;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@AllArgsConstructor
public class PriceHistoryService {

    private final ProductRepository productRepository;
    private final ClientOrchestrator clientOrchestrator;
    private final PriceUpdateService priceUpdater;


    //@Scheduled(cron = "0 0 9,17 * * *")
    @Scheduled(fixedDelay = 60000)  
    public void updatePriceHistory() {
        List<ProductEntity> products = productRepository.findAll();

        for (ProductEntity product : products) {
            System.out.println("Updating product " + product.getId());
            try {
                // We get the price outside the transaction to avoid blocking the transaction
                Price newPrice = clientOrchestrator.getPrice(URI.create(product.getUrl().value()));

                // Persistance is done inside the transaction to ensure correct data integrity
                priceUpdater.updateSingleProduct(product, newPrice);

            } catch (Exception e) {
                // If it fails, we should log it and continue with the next product.
                // We are not within a controller context here, so we cannot use @ExceptionHandler.
                // And it will not be logged by the global exception handler.
                System.err.println("Error actualizando producto {}: {} " + product.getId() +" "+ e.getMessage());
            }
        }
    }

}
