package com.eduardomango.pricetracker.subscription;

import com.eduardomango.pricetracker.common.model.Price;
import com.eduardomango.pricetracker.product.ProductRepository;
import com.eduardomango.pricetracker.product.domain.AlertCondition;
import com.eduardomango.pricetracker.product.domain.ComparisonType;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.product.domain.URL;
import com.eduardomango.pricetracker.subscription.domain.SubscriptionEntity;
import com.eduardomango.pricetracker.user.UserRepository;
import com.eduardomango.pricetracker.user.domain.UserEntity;
import com.eduardomango.pricetracker.common.model.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SubscriptionRepositoryTest {

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void findAllByProductIdAndActiveTrue_ReturnsOnlyActiveSubscriptionsForProduct() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setEmail(new Email("test@example.com"));
        user = userRepository.save(user);

        ProductEntity product = new ProductEntity();
        product.setName("Test Product");
        product.setUrl(new URL("https://example.com"));
        product = productRepository.save(product);

        AlertCondition condition = new AlertCondition(new Price(new BigDecimal("10"), "USD"),
                ComparisonType.LOWER_THAN);

        SubscriptionEntity activeSub = new SubscriptionEntity();
        activeSub.setUser(user);
        activeSub.setProduct(product);
        activeSub.setAlertCondition(condition);
        activeSub.setActive(true);
        subscriptionRepository.save(activeSub);

        SubscriptionEntity inactiveSub = new SubscriptionEntity();
        inactiveSub.setUser(user);
        inactiveSub.setProduct(product);
        inactiveSub.setAlertCondition(condition);
        inactiveSub.setActive(false);
        subscriptionRepository.save(inactiveSub);

        // Act
        List<SubscriptionEntity> results = subscriptionRepository.findAllByProductIdAndActiveTrue(product.getId());

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getActive()).isTrue();
    }
}
