package com.hs.lab4.userservice.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionResolver {
    @ExceptionHandler(JWTNotValidException.class)
    public ResponseEntity<ErrorMessage> jWTNotValidException(JWTNotValidException exception) {
        log.warn("JWTNotValidException: ", exception.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorMessage(exception.getMessage()));
    }

}
