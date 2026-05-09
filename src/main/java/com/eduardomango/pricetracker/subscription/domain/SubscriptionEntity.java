package com.eduardomango.pricetracker.subscription.domain;

import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.product.domain.AlertCondition;
import com.eduardomango.pricetracker.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, updatable = false)
    private UUID externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductEntity product;

    @Embedded
    @AttributeOverrides({

        @AttributeOverride(name = "targetPrice.amount",
                column = @Column(name = "target_price", precision = 19, scale = 2)),

        @AttributeOverride(name = "targetPrice.currency",
                column = @Column(name = "target_currency", length = 3)),

        @AttributeOverride(name = "comparisonType",
                column = @Column(name = "comparison_type"))
    })
    private AlertCondition alertCondition;


    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean active;

    @PrePersist
    void onCreate() {
        if (externalId == null)
            externalId = UUID.randomUUID();
    }
}