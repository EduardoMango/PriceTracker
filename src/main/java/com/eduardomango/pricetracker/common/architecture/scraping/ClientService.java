package com.eduardomango.pricetracker.common.architecture.scraping;

import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.product.domain.ProductEntity;

import reactor.core.publisher.Mono;

import java.net.URI;

public interface ClientService {

    boolean supports(URI url);

    Mono<ProductEntity> getProduct(URI url);

    Mono<Price> getPrice(URI url);
}
