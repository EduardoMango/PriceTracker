package com.eduardomango.pricetracker.subscription;

import com.eduardomango.pricetracker.common.exceptions.EntityNotFoundException;
import com.eduardomango.pricetracker.product.ProductRepository;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.subscription.domain.SubscriptionEntity;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionRequest;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionResponse;
import com.eduardomango.pricetracker.subscription.domain.mapper.SubscriptionMapper;
import com.eduardomango.pricetracker.user.UserRepository;
import com.eduardomango.pricetracker.user.domain.UserEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SubscriptionService implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SubscriptionMapper subscriptionMapper;

    @Override
    @Transactional
    public SubscriptionResponse create(SubscriptionRequest request) {
        UserEntity user = userRepository.findByExternalId(request.userId())
                .orElseThrow(() -> new EntityNotFoundException("User", "User not found", "userId", request.userId().toString()));

        ProductEntity product = productRepository.findByExternalId(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product", "Product not found", "productId", request.productId().toString()));

        return subscriptionRepository.findByUserIdAndProductId(user.getId(), product.getId())
                .map(existing -> {
                    existing.setAlertCondition(subscriptionMapper.mapAlertCondition(request));
                    existing.setActive(true);
                    return subscriptionMapper.toDTO(subscriptionRepository.save(existing));
                })
                .orElseGet(() -> {
                    SubscriptionEntity newSubscription = subscriptionMapper.toEntity(request);
                    newSubscription.setUser(user);
                    newSubscription.setProduct(product);
                    return subscriptionMapper.toDTO(subscriptionRepository.save(newSubscription));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse findById(UUID id) {
        return subscriptionRepository.findByExternalId(id)
                .map(subscriptionMapper::toDTO)
                .orElseThrow(() -> new EntityNotFoundException("Subscription", "Subscription not found", "id", id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> findAllByUserId(UUID userId) {
        UserEntity user = userRepository.findByExternalId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", "User not found", "userId", userId.toString()));
        
        return subscriptionRepository.findAllByUserId(user.getId()).stream()
                .map(subscriptionMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public SubscriptionResponse update(UUID id, SubscriptionRequest request) {
        SubscriptionEntity subscription = subscriptionRepository.findByExternalId(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription", "Subscription not found", "id", id.toString()));

        subscription.setAlertCondition(subscriptionMapper.mapAlertCondition(request));
        return subscriptionMapper.toDTO(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        SubscriptionEntity subscription = subscriptionRepository.findByExternalId(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription", "Subscription not found", "id", id.toString()));
        subscriptionRepository.delete(subscription);
    }

    @Override
    @Transactional
    public SubscriptionResponse toggleActive(UUID id) {
        SubscriptionEntity subscription = subscriptionRepository.findByExternalId(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription", "Subscription not found", "id", id.toString()));

        subscription.setActive(!subscription.getActive());
        return subscriptionMapper.toDTO(subscriptionRepository.save(subscription));
    }
}
