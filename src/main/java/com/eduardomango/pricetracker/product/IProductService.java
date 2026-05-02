package com.eduardomango.pricetracker.product;


import com.eduardomango.pricetracker.product.domain.dto.ProductRequest;
import com.eduardomango.pricetracker.product.domain.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface IProductService {

    List<ProductResponse> getAllProducts();
    ProductResponse getProductById(UUID productId);

    ProductResponse save(ProductRequest productRequest);

    void delete(UUID productId);
}
