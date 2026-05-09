package com.eduardomango.pricetracker.subscription;

import com.eduardomango.pricetracker.product.domain.ComparisonType;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionRequest;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ISubscriptionService subscriptionService;

    @Test
    void create_ReturnsOk_WhenValidRequest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();

        SubscriptionRequest request = new SubscriptionRequest(
                userId, productId, new BigDecimal("50.00"), "USD", ComparisonType.LOWER_THAN
        );

        SubscriptionResponse response = new SubscriptionResponse(
                subscriptionId, userId, productId, "Test", null, ComparisonType.LOWER_THAN, true
        );

        when(subscriptionService.create(any(SubscriptionRequest.class))).thenReturn(response);

        mockMvc.perform(post("/subscriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(subscriptionId.toString()));
    }

    @Test
    void findById_ReturnsOk_WhenExists() throws Exception {
        UUID subscriptionId = UUID.randomUUID();
        SubscriptionResponse response = new SubscriptionResponse(
                subscriptionId, UUID.randomUUID(), UUID.randomUUID(), "Test", null, ComparisonType.LOWER_THAN, true
        );

        when(subscriptionService.findById(subscriptionId)).thenReturn(response);

        mockMvc.perform(get("/subscriptions/{id}", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(subscriptionId.toString()));
    }
}
