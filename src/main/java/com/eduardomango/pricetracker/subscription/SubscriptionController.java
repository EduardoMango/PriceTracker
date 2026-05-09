package com.eduardomango.pricetracker.subscription;

import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionRequest;
import com.eduardomango.pricetracker.subscription.domain.dto.SubscriptionResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/subscriptions")
@AllArgsConstructor
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@RequestBody @Valid SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.create(request));
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> findById(@PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(subscriptionService.findById(subscriptionId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionResponse>> findAllByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(subscriptionService.findAllByUserId(userId));
    }

    @PutMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> update(@PathVariable UUID subscriptionId, @RequestBody @Valid SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.update(subscriptionId, request));
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> delete(@PathVariable UUID subscriptionId) {
        subscriptionService.delete(subscriptionId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{subscriptionId}/toggle")
    public ResponseEntity<SubscriptionResponse> toggleActive(@PathVariable UUID subscriptionId) {
        return ResponseEntity.ok(subscriptionService.toggleActive(subscriptionId));
    }
}
