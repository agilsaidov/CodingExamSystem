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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.NOT_FOUND.value(),
                e.getErrorName(),
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(exceptionResponse,  HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleInvalidCredentialException(InvalidCredentialsException e){
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "INVALID_CREDENTIALS",
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ExceptionResponse> handleAuthException(AuthException e) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                e.getStatus().value(),
                e.getErrorName(),
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(exceptionResponse,  e.getStatus());
    }
}
