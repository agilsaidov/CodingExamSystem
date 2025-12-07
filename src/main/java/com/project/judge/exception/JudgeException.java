package com.project.judge.exception;

import lombok.Getter;

@Getter
public class JudgeException extends RuntimeException {
    private final String errorName;

    public JudgeException(String errorName, String message) {
        super(message);
        this.errorName = errorName;
    }
}
