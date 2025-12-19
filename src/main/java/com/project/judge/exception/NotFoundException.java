package com.project.judge.exception;

public class NotFoundException extends RuntimeException {

    private final String errorName;

    public NotFoundException(String errorName, String message) {
        super(message);
        this.errorName = errorName;
    }
}
