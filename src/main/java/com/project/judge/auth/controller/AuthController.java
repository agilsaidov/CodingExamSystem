package com.project.judge.auth.controller;

import com.project.judge.auth.dto.reqeust.LoginRequest;
import com.project.judge.auth.dto.response.AuthResponse;
import com.project.judge.auth.service.AuthService;
import com.project.judge.model.AppUser;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/students/login")
    public ResponseEntity<AuthResponse> loginStudent(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.loginStudent(loginRequest);
        return ResponseEntity.ok(response);
    }
}
