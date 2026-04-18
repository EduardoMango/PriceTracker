package com.eduardomango.pricetracker.subscription;

import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.product.domain.AlertCondition;
import com.eduardomango.pricetracker.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "subscriptions")
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class SubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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


    private Boolean active = true;
}