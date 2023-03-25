package com.ringo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalInsertException extends RuntimeException {
    public IllegalInsertException(String message) {
        super(message);
    }
}
