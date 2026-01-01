package com.project.judge.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProblemResponse {
    private Long problemId;
    private String examId;
    private String title;
    private String description;
    private Integer points;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer orderIndex;
    private Integer testCaseCount;
    private LocalDateTime createdAt;
}
