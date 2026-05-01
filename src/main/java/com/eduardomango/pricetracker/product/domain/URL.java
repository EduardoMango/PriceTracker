package com.eduardomango.pricetracker.product.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Embeddable;

import java.net.URI;
import java.net.URISyntaxException;

@Embeddable
public record URL(String value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public URL {
        try {
            URI uri = new URI(value);
            if (uri.getScheme() == null || !uri.getScheme().startsWith("http")) {
                throw new IllegalArgumentException("Provided URL must start with http or https");
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Provided URL is not valid: " + value);
        }
    }

    @JsonValue
    public String value() {
        return value;
    }
}
