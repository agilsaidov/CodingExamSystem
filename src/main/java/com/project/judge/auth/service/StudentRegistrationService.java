package com.project.judge.auth.service;

import com.project.judge.auth.dto.reqeust.RegistrationRequest;
import com.project.judge.exception.BadRequestException;
import com.project.judge.model.AppUser;
import com.project.judge.model.Role;
import com.project.judge.repository.UserRepo;
import com.project.judge.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentRegistrationService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;


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
        log.info("Student {} registered successfully", studentId);
    }
}
