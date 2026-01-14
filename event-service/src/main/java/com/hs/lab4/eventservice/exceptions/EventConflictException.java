package com.hs.lab4.eventservice.exceptions;

public class EventConflictException extends RuntimeException {
    public EventConflictException(String message) {
        super(message);
    }
}
