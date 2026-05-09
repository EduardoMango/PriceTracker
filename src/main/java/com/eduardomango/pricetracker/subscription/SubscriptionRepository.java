package com.eduardomango.pricetracker.subscription;

import com.eduardomango.pricetracker.subscription.domain.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
    List<SubscriptionEntity> findAllByUserId(Long userId);
    Optional<SubscriptionEntity> findByUserIdAndProductId(Long userId, Long productId);
    Optional<SubscriptionEntity> findByExternalId(UUID externalId);
    boolean existsByExternalId(UUID externalId);
    List<SubscriptionEntity> findAllByProductIdAndActiveTrue(Long productId);
}
