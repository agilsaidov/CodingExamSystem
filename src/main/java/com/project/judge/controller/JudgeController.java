package com.project.judge.controller;

import com.project.judge.dto.request.CodeExecutionRequest;
import com.project.judge.dto.response.CodeExecutionResponse;
import com.project.judge.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/judge")
@RequiredArgsConstructor
public class JudgeController {

    private final JudgeService judgeService;

    @PostMapping("/execute")
    public ResponseEntity<CodeExecutionResponse> execute(@RequestBody CodeExecutionRequest request) {
        return ResponseEntity.ok().body(judgeService.executeCode(request.getCode()));
    }

}