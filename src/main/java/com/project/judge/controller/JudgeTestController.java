package com.project.judge.controller;


import com.project.judge.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class JudgeTestController {

    private final JudgeService judgeService;

    @GetMapping("/connection")
    public String testConnection() {
        return judgeService.checkConnection();
    }
}
