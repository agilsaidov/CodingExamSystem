package com.project.judge.exception;

import com.project.judge.dto.response.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JudgeException.class)
    public ResponseEntity<ExceptionResponse> handleJudgeException(JudgeException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "JUDGE_SERVICE_EXCEPTION",
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(exceptionResponse,  HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST_EXCEPTION",
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(exceptionResponse,  HttpStatus.BAD_REQUEST);
    }
}
