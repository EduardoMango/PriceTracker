package com.eduardomango.pricetracker.product.domain.dto;


import com.eduardomango.pricetracker.common.model.PriceDTO;
import com.eduardomango.pricetracker.product.domain.ProductStatus;
import com.eduardomango.pricetracker.product.domain.URL;

import java.util.UUID;

public record ProductResponse(UUID productId, String name, URL url, ProductStatus status, PriceDTO price) {
}
