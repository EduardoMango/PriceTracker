package com.eduardomango.pricetracker.pricehistory;

import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.pricehistory.domain.PriceHistoryEntity;
import com.eduardomango.pricetracker.product.ProductRepository;
import com.eduardomango.pricetracker.product.domain.ProductEntity;

import lombok.AllArgsConstructor;
import com.eduardomango.pricetracker.common.model.events.PriceUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PriceUpdateService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    //Create a new transaction for each product.
    //This way, if one product fails, the others will not be affected.
    //propagation = Propagation.REQUIRES_NEW means that a new transaction will be created for each product.
    // It needs to be a new class because of how Spring handles proxies

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSingleProduct(ProductEntity product, Price newPrice) {
        //Save price history
        PriceHistoryEntity history = new PriceHistoryEntity();
        history.setProduct(product);
        history.setPrice(newPrice);

        priceHistoryRepository.save(history);

        // Update product price
        product.setCurrentPrice(newPrice);
        product.setLastChecked(LocalDateTime.now());
        productRepository.save(product);

        // Publish event
        eventPublisher.publishEvent(new PriceUpdatedEvent(product.getId(), product.getName(), newPrice));
    }
}
