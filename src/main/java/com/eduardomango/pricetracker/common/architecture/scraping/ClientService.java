package com.eduardomango.pricetracker.common.architecture.scraping;

import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.product.domain.ProductEntity;

import java.net.URI;

public interface ClientService {

    boolean supports(URI url);

    ProductEntity getProduct(URI url);

    Price getPrice(URI url);
}
