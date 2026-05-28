package com.eduardomango.pricetracker.auth.dto;

public record NewAccountRequest(String username, String password, String email) {
}
