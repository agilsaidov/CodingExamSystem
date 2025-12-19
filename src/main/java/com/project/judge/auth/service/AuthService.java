package com.project.judge.auth.service;

import com.project.judge.auth.dto.reqeust.LoginRequest;
import com.project.judge.auth.dto.response.AuthResponse;
import com.project.judge.exception.InvalidCredentialsException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.AppUser;
import com.project.judge.repository.UserRepo;
import com.project.judge.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse loginStudent(LoginRequest loginRequest) {

        Optional<AppUser> user = userRepo.findByUsername(loginRequest.getUsername());

        if (user.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            throw new InvalidCredentialsException("Given credential(s) are not valid");
        }

        AuthResponse response = AuthResponse.builder()
                .token(jwtService.generateToken(user.get()))
                .userId(user.get().getUserId())
                .username(user.get().getUsername())
                .role(user.get().getRole())
                .fullName(user.get().getFullName())
                .build();

        return response;
    }
}
