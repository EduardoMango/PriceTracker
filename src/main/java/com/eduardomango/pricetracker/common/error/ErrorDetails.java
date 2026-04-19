package com.eduardomango.pricetracker.common.error;

import java.time.Instant;
import java.util.List;

public record ErrorDetails(Instant timestamp,
                           int status,
                           String error,
                           String code,
                           String message,
                           String path,
                           String method,
                           List<FieldError> errors) {}
