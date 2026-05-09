package com.eduardomango.pricetracker.subscription.domain.dto;

import com.eduardomango.pricetracker.common.model.PriceDTO;
import com.eduardomango.pricetracker.product.domain.ComparisonType;

import java.util.UUID;

public record SubscriptionResponse(
        UUID subscriptionId,
        UUID userId,
        UUID productId,
        String productName,
        PriceDTO targetPrice,
        ComparisonType comparisonType,
        Boolean active
) {
}
