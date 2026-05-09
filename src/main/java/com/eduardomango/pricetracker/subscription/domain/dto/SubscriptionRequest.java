package com.eduardomango.pricetracker.subscription.domain.dto;

import com.eduardomango.pricetracker.product.domain.ComparisonType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record SubscriptionRequest(
        @NotNull UUID userId,
        @NotNull UUID productId,
        @NotNull @Positive BigDecimal targetPrice,
        @NotNull String currency,
        @NotNull ComparisonType comparisonType
) {
}
