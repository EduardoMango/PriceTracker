package com.eduardomango.pricetracker.common.exceptions;

import lombok.Getter;

import java.time.Instant;

@Getter
public class UnsuportedWebsite extends RuntimeException {

    private final String website;
    private final Instant timestamp;


    public UnsuportedWebsite(String website) {
        this.website = website;
        this.timestamp = Instant.now();
    }
}
