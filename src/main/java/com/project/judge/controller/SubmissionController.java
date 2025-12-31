package com.project.judge.controller;

import com.project.judge.dto.request.SubmitCodeRequest;
import com.project.judge.dto.response.SubmissionDetailResponse;
import com.project.judge.dto.response.SubmissionResponse;
import com.project.judge.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/submit")
    public ResponseEntity<SubmissionResponse> submitCode(
            @Valid @RequestBody SubmitCodeRequest request,
            Authentication authentication){

        String studentId = authentication.getName();
        SubmissionResponse submissionResponse = submissionService.submitCode(request, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(submissionResponse);
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionDetailResponse> getSubmissionDetails(
            @PathVariable Long submissionId,
            Authentication authentication){

        String userId = authentication.getName();
        var response = submissionService.getSubmissionDetails(submissionId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/problem/{problemId}")
    public ResponseEntity<List<SubmissionResponse>> getProblemSubmissions(
            @PathVariable Long problemId,
            Authentication authentication){

        String userId = authentication.getName();
        var response = submissionService.getProblemSubmissions(problemId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/exam/{examId}/finish")
    public ResponseEntity<Void> finishExam(
            @PathVariable String examId,
            Authentication authentication){

        String studentId = authentication.getName();
        submissionService.finishExam(examId, studentId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
