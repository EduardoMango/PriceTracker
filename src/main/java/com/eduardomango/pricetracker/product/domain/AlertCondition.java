package com.eduardomango.pricetracker.product.domain;

import com.eduardomango.pricetracker.common.model.Price;
import jakarta.persistence.Embeddable;

@Embeddable
public record AlertCondition(Price targetPrice, ComparisonType comparisonType) {



    public AlertCondition {
        if (targetPrice == null || comparisonType == null) {
            throw new IllegalArgumentException("Alert condition cannot be null");
        }
    }

    // Verifies if the current price is within the target price range
    public boolean isSatisfiedBy(Price currentPrice) {
        int comparison = currentPrice.amount().compareTo(targetPrice.amount());
        return switch (comparisonType) {
            case LOWER_THAN -> comparison < 0;
            case HIGHER_THAN -> comparison > 0;
            case EQUALS -> comparison == 0;
        };
    }
}
