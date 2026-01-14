package com.hs.lab4.userservice.exceptions;

public class JWTNotValidException extends RuntimeException {

    public JWTNotValidException(String msg) {
        super(msg);
    }
}