package com.eduardomango.pricetracker.user.domain.dto;

import com.eduardomango.pricetracker.common.model.Email;

import java.util.UUID;

public record UserDTO(UUID userId, Email email) {
}
