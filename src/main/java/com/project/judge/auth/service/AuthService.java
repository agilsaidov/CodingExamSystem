package com.project.judge.auth.service;

import com.project.judge.auth.dto.reqeust.LoginRequest;
import com.project.judge.auth.dto.reqeust.RegistrationRequest;
import com.project.judge.auth.dto.response.AuthResponse;
import com.project.judge.auth.dto.response.GroupListResponse;
import com.project.judge.exception.AuthException;
import com.project.judge.exception.BadRequestException;
import com.project.judge.exception.InvalidCredentialsException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.AppUser;
import com.project.judge.model.Role;
import com.project.judge.repository.UserRepo;
import com.project.judge.security.JwtService;
import com.project.judge.service.GroupService;
import com.project.judge.utils.IdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GroupService groupService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "blisted_token:";
    private static final int BLACKLIST_EXPIRATION = 180;


    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        AppUser user = userRepo.findByUsernameAndPassword(
                loginRequest.getUsername(),
                passwordEncoder.encode(loginRequest.getPassword())
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


    public void logout(String token) {

        if(token == null || !token.startsWith("Bearer ")) {
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "Given token is not valid or malformed"
            );
        }
        log.info("Logout attempt for token: {}", token);

        String bareToken = token.substring(7);

        redisTemplate.opsForValue().set(
                TOKEN_BLACKLIST_PREFIX + bareToken,
                "blacklisted",
                BLACKLIST_EXPIRATION,
                TimeUnit.MINUTES
        );

        log.info("Logout successful - token blacklisted");
    }


    public void registerStudent(String instructorId, RegistrationRequest request){
        log.info("Instructor {} registering student: {}", instructorId, request.getUsername());

        if(userRepo.existsByUsername(request.getUsername())){
            throw new BadRequestException("Username is already in use");
        }

        String studentId = IdGenerator.generateId("ST", 5);

        AppUser student = AppUser.builder()
                .userId(studentId)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .fullName(request.getFullName())
                .build();

        userRepo.save(student);
        log.info("Student {} registered successfully", student.getUsername());
    }


    public void registerInstructor(RegistrationRequest request){
        log.info("Registering instructor: {}", request.getUsername());

        if(userRepo.existsByUsername(request.getUsername())){
            throw new BadRequestException("Username is already in use");
        }

        String instructorId = IdGenerator.generateId("IN", 5);

        AppUser instructor = AppUser.builder()
                .userId(instructorId)
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.INSTRUCTOR)
                .build();

        userRepo.save(instructor);
        log.info("Instructor {} registered successfully", instructor.getUsername());
    }
}
