package com.eduardomango.pricetracker.common.model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public record PriceDTO(
        BigDecimal amount,
        String currency,
        String formattedPrice
) {}
