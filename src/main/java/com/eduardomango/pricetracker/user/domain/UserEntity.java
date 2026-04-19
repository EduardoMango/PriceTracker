package com.eduardomango.pricetracker.user.domain;

import com.eduardomango.pricetracker.common.model.Email;
import com.eduardomango.pricetracker.subscription.domain.SubscriptionEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", nullable = false, unique = true, updatable = false)
    private UUID externalId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email_address", unique = true, nullable = false))
    private Email email;

    @OneToMany(mappedBy = "user")
    private List<SubscriptionEntity> subscriptions;

    @PrePersist
    void onCreate() {
        if (externalId == null)
            externalId = UUID.randomUUID();
    }
    /// Valores basicos, el resto se agregaran con Spring Security
}
