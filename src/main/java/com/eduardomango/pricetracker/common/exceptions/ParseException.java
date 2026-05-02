package com.eduardomango.pricetracker.common.exceptions;

public class ParseException extends RuntimeException {
    public ParseException(String message, Exception e) {
        super(message);
    }
    public ParseException(String message) {
        super(message);
    }
}
