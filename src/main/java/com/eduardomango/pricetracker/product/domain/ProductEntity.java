package com.eduardomango.pricetracker.product.domain;


import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.pricehistory.domain.PriceHistoryEntity;
import com.eduardomango.pricetracker.subscription.domain.SubscriptionEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@ToString
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, updatable = false)
    private UUID externalId;

    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "url", unique = true, nullable = false, length = 1024))
    private URL url;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "current_price")),
            @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Price currentPrice;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    private LocalDateTime lastChecked;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<PriceHistoryEntity> history;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<SubscriptionEntity> subscriptions;

    @PrePersist
    void onCreate() {
        if (externalId == null)
            externalId = UUID.randomUUID();
        if (status == null)
            status = ProductStatus.ACTIVE;
        lastChecked = LocalDateTime.now();
    }
}
