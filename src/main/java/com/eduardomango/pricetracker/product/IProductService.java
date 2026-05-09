package com.eduardomango.pricetracker.product;


import com.eduardomango.pricetracker.product.domain.dto.ProductRequest;
import com.eduardomango.pricetracker.product.domain.dto.ProductResponse;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IProductService {

    List<ProductResponse> getAllProducts(String name,
                                         String nameMatch,
                                         String description,
                                         BigDecimal minPrice,
                                         BigDecimal maxPrice);

    ProductResponse getProductById(UUID productId);

    Mono<ProductResponse> save(ProductRequest productRequest);

    void delete(UUID productId);
}
