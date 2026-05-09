package com.eduardomango.pricetracker.common.model;

import java.math.BigDecimal;


public record PriceDTO(
        BigDecimal amount,
        String currency,
        String formattedPrice
) {}
