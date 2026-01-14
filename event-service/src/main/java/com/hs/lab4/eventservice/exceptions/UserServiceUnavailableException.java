package com.hs.lab4.eventservice.exceptions;

public class UserServiceUnavailableException extends RuntimeException{
    public UserServiceUnavailableException(String s) {
        super(s);
    }
}
