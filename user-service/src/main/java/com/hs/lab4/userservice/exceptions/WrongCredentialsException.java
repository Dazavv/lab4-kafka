package com.hs.lab4.userservice.exceptions;

public class WrongCredentialsException extends RuntimeException {

    public WrongCredentialsException(String msg) {
        super(msg);
    }
}