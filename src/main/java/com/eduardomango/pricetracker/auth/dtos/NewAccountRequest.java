package com.eduardomango.pricetracker.auth.dtos;

public record NewAccountRequest (String username,
                                 String password,
                                 String email){
}
