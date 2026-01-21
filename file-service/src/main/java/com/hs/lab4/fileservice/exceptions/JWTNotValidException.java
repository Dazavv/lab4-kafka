package com.hs.lab4.fileservice.exceptions;

public class JWTNotValidException extends RuntimeException {

    public JWTNotValidException(String msg) {
        super(msg);
    }
}