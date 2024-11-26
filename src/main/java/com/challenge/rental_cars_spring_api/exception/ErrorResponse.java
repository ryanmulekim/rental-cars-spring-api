package com.challenge.rental_cars_spring_api.exception;


import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse extends RuntimeException {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String path;
    private final String message;

    public ErrorResponse(int status, String error, String path, String message) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.path = path;
        this.message = message;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // Override to avoid filling in stack trace
        return this;
    }
}
