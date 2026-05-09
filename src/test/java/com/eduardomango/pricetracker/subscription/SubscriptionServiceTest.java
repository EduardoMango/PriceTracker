package com.eduardomango.pricetracker.subscription;

import com.eduardomango.pricetracker.common.exceptions.EntityNotFoundException;
import com.eduardomango.pricetracker.product.ProductRepository;
import com.eduardomango.pricetracker.product.domain.ComparisonType;
import com.eduardomango.pricetracker.product.domain.ProductEntity;
import com.eduardomango.pricetracker.subscription.domain.SubscriptionEntity;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionRequest;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionResponse;
import com.eduardomango.pricetracker.subscription.domain.mapper.SubscriptionMapper;
import com.eduardomango.pricetracker.user.UserRepository;
import com.eduardomango.pricetracker.user.domain.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private UUID userId;
    private UUID productId;
    private SubscriptionRequest request;
    private UserEntity userEntity;
    private ProductEntity productEntity;
    private SubscriptionEntity subscriptionEntity;
    private SubscriptionResponse response;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();
        
        request = new SubscriptionRequest(
                userId, 
                productId, 
                new BigDecimal("100.00"), 
                "USD", 
                ComparisonType.LOWER_THAN
        );

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setExternalId(userId);

        productEntity = new ProductEntity();
        productEntity.setId(1L);
        productEntity.setExternalId(productId);

        subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setId(1L);
        
        response = new SubscriptionResponse(
                UUID.randomUUID(), userId, productId, "Test Product", null, ComparisonType.LOWER_THAN, true
        );
    }

    @Test
    void create_WhenUserAndProductExist_CreatesSubscription() {
        // Arrange
        when(userRepository.findByExternalId(userId)).thenReturn(Optional.of(userEntity));
        when(productRepository.findByExternalId(productId)).thenReturn(Optional.of(productEntity));
        when(subscriptionRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(subscriptionMapper.toEntity(request)).thenReturn(subscriptionEntity);
        when(subscriptionRepository.save(any(SubscriptionEntity.class))).thenReturn(subscriptionEntity);
        when(subscriptionMapper.toDTO(subscriptionEntity)).thenReturn(response);

        // Act
        SubscriptionResponse result = subscriptionService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.userId());
        verify(subscriptionRepository).save(subscriptionEntity);
    }

    @Test
    void create_WhenUserDoesNotExist_ThrowsException() {
        // Arrange
        when(userRepository.findByExternalId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> subscriptionService.create(request));
        verify(productRepository, never()).findByExternalId(any());
        verify(subscriptionRepository, never()).save(any());
    }
}
