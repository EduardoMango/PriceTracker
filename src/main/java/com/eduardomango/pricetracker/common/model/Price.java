package com.eduardomango.pricetracker.common.model;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Embeddable
public record Price(BigDecimal amount, String currency) {

    public Price {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Price cannot be zero or null");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }
}
