package com.project.judge.auth.controller;

import com.project.judge.auth.dto.reqeust.RegistrationRequest;
import com.project.judge.auth.service.StudentRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentRegistrationController {

    private final StudentRegistrationService studentRegistrationService;

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody RegistrationRequest request,
                                             Authentication authentication) {
        String instructorId = authentication.getName();
        studentRegistrationService.registerStudent(instructorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }
}
