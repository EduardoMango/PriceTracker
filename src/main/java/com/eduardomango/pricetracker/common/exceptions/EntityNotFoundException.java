package com.eduardomango.pricetracker.common.exceptions;

import lombok.Getter;

import java.time.Instant;
import java.util.NoSuchElementException;

@Getter
public class EntityNotFoundException extends NoSuchElementException {

    private final String entity;
    private final String field;
    private final Object value;
    private final Instant timestamp;

    public EntityNotFoundException(String entity, String message, String field, Object value) {
        super(message);
        this.entity = entity;
        this.field = field;
        this.value = value;
        this.timestamp = Instant.now();
    }
}
