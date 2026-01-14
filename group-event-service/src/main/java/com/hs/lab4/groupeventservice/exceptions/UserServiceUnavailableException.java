package com.hs.lab4.groupeventservice.exceptions;

public class UserServiceUnavailableException extends RuntimeException{
    public UserServiceUnavailableException(String s) {
        super(s);
    }
}
