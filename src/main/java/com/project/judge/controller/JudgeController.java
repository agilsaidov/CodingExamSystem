package com.project.judge.controller;

import com.project.judge.dto.request.JudgeSubmissionRequest;
import com.project.judge.dto.response.JudgeSubmissionResponse;
import com.project.judge.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/judge")
@RequiredArgsConstructor
public class JudgeController {

    private final JudgeService judgeService;

    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        boolean available = judgeService.isAvailable();

        response.put("available", available);
        response.put("status", available ? "UP" : "DOWN");

        if (available) {
            response.put("info", judgeService.getAbout());
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/submit")
    public ResponseEntity<JudgeSubmissionResponse> submit(
            @RequestBody JudgeSubmissionRequest request) {

        JudgeSubmissionResponse response = judgeService.submitAndWait(request);

        return ResponseEntity.ok(response);
    }
}
