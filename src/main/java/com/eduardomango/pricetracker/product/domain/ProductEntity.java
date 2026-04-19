package com.eduardomango.pricetracker.product.domain;


import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.pricehistory.domain.PriceHistory;
import com.eduardomango.pricetracker.subscription.domain.SubscriptionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, updatable = false)
    private UUID externalId = UUID.randomUUID();

    private String name;

    @Column(unique = true, nullable = false)
    private String url;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "current_price")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Price currentPrice;

    private LocalDateTime lastChecked;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PriceHistory> history;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<SubscriptionEntity> subscriptions;
}
