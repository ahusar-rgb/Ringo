package com.ringo.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
        String error = e.getMessage();
        log.warn(error);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalInsertException.class)
    public ResponseEntity<String> handleIllegalInsertException(IllegalInsertException e) {
        String error = e.getMessage();
        log.warn(error);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
