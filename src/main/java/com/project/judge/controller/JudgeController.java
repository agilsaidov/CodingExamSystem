package com.project.judge.controller;

import com.project.judge.service.JudgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/judge")
@RequiredArgsConstructor
public class JudgeController {

    private final JudgeService judgeService;

    @PostMapping(value = "/execute", consumes = "text/plain")
    public ResponseEntity<?> execute(@RequestBody String code) {
        return ResponseEntity.ok().body(judgeService.executeCode(code));
    }

}