package com.eduardomango.pricetracker.subscription;

import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionRequest;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionResponse;

import java.util.List;
import java.util.UUID;

public interface ISubscriptionService {
    SubscriptionResponse create(SubscriptionRequest request);
    SubscriptionResponse findById(UUID id);
    List<SubscriptionResponse> findAllByUserId(UUID userId);
    SubscriptionResponse update(UUID id, SubscriptionRequest request);
    void delete(UUID id);
    SubscriptionResponse toggleActive(UUID id);
}
