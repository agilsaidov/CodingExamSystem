package com.project.judge.controller;

import com.project.judge.dto.request.CreateProblemRequest;
import com.project.judge.dto.request.CreateTestCaseRequest;
import com.project.judge.dto.response.ProblemResponse;
import com.project.judge.model.Problem;
import com.project.judge.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping("/create")
    public ResponseEntity<ProblemResponse> createProblem(
            @Valid @RequestBody CreateProblemRequest request,
            Authentication authentication){

        ProblemResponse response = problemService.createProblem(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{problemId/test-cases}")
    public ResponseEntity<Void> addTestCase(
            @PathVariable Long problemId,
            @Valid @RequestBody CreateTestCaseRequest request,
            Authentication authentication){

        problemService.addTestCase(problemId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/delete/{problemId}")
    public ResponseEntity<Void> deleteProblem(
            @PathVariable Long problemId,
            Authentication authentication){

        problemService.deleteProblem(problemId, authentication.getName());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
