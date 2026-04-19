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
    private UUID externalId = UUID.randomUUID();

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email_address", unique = true))
    private Email email;

    @OneToMany(mappedBy = "user")
    private List<SubscriptionEntity> subscriptions;

    /// Valores basicos, el resto se agregaran con Spring Security
}
