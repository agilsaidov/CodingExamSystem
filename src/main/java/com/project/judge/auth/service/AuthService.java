package com.project.judge.auth.service;

import com.project.judge.auth.dto.reqeust.LoginRequest;
import com.project.judge.auth.dto.response.AuthResponse;
import com.project.judge.auth.dto.response.GroupListResponse;
import com.project.judge.exception.InvalidCredentialsException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.AppUser;
import com.project.judge.repository.UserRepo;
import com.project.judge.security.JwtService;
import com.project.judge.service.GroupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GroupService groupService;

    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        AppUser user = userRepo.findByUsernameAndPassword(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        ).orElseThrow(() -> new InvalidCredentialsException("Given credential(s) are not valid"));

        List<GroupListResponse> groups = groupService.getMyGroups(user.getUserId());

        AuthResponse response = AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .userId(user.getUserId())
                .username(user.getUsername())
                .role(user.getRole())
                .fullName(user.getFullName())
                .groups(groups)
                .build();

        return response;
    }
}
