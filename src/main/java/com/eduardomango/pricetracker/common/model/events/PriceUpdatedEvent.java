package com.eduardomango.pricetracker.common.model.events;

import com.eduardomango.pricetracker.common.model.Price;

public record PriceUpdatedEvent(Long productId, String productName, Price currentPrice) {
}
