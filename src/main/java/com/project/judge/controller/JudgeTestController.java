package com.project.judge.controller;


import com.project.judge.dto.request.SimpleSubmissionRequest;
import com.project.judge.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class JudgeTestController {

    private final JudgeService judgeService;

    @GetMapping("/connection")
    public String testConnection() {
        return judgeService.checkConnection();
    }

    @PostMapping("/submit")
    public Map<String, Object> testSubmit(@RequestBody SimpleSubmissionRequest request) throws Exception {
        return judgeService.submitCode(request);
    }
}
