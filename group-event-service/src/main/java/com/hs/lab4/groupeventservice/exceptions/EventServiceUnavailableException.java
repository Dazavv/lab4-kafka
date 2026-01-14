package com.hs.lab4.groupeventservice.exceptions;

public class EventServiceUnavailableException extends RuntimeException{
    public EventServiceUnavailableException(String s) {
        super(s);
    }
}
