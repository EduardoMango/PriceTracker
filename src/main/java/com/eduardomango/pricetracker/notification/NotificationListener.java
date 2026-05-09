package com.eduardomango.pricetracker.notification;

import com.eduardomango.pricetracker.common.model.events.PriceUpdatedEvent;
import com.eduardomango.pricetracker.subscription.SubscriptionRepository;
import com.eduardomango.pricetracker.subscription.domain.SubscriptionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    @Async
    @EventListener
    @Transactional(readOnly = true)
    public void handlePriceUpdatedEvent(PriceUpdatedEvent event) {
        log.info("Received PriceUpdatedEvent for product '{}' (ID: {}). Evaluating subscriptions...", event.productName(), event.productId());

        List<SubscriptionEntity> activeSubscriptions = subscriptionRepository.findAllByProductIdAndActiveTrue(event.productId());

        for (SubscriptionEntity subscription : activeSubscriptions) {
            if (subscription.getAlertCondition().isSatisfiedBy(event.currentPrice())) {
                log.info("Alert condition met for user {}. Sending email.", subscription.getUser().getEmail().value());
                emailService.sendPriceAlertEmail(
                        subscription.getUser().getEmail().value(),
                        event.productName(),
                        subscription.getAlertCondition().targetPrice(),
                        event.currentPrice()
                );
            }
        }
    }
}
