package com.project.judge.auth.controller;

import com.project.judge.auth.dto.request.RegistrationRequest;
import com.project.judge.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class InstructorRegistrationController {

    private final AuthService authService;

    @PostMapping("/register/instructor")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> registerInstructor(@Valid @RequestBody RegistrationRequest request) {
        authService.registerInstructor(request);
        return ResponseEntity.ok().build();
    }
}
