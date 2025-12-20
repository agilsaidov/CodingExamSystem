package com.project.judge.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthException extends RuntimeException {

    private final String errorName;
    private final HttpStatus status;

    public AuthException(HttpStatus status, String errorName,String message) {
        super(message);
        this.status = status;
        this.errorName = errorName;
    }
}
