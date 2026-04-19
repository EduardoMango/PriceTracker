package com.eduardomango.pricetracker.product.domain;

import jakarta.persistence.Embeddable;

import java.net.URI;
import java.net.URISyntaxException;

@Embeddable
public record ProductURL(String value) {

    public ProductURL {
        try {
            URI uri = new URI(value);
            if (uri.getScheme() == null || !uri.getScheme().startsWith("http")) {
                throw new IllegalArgumentException("Provided URL must start with http or https");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Provided URL is not valid: " + value);
        }
    }
}
