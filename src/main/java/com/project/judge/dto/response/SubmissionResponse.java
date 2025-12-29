package com.project.judge.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubmissionResponse {
    private Long submissionId;
    private Long problemId;
    private String problemTitle;
    private Integer languageId;
    private String status;
    private Integer score;
    private Integer passedTestCases;
    private Integer totalTestCases;
    private LocalDateTime submittedAt;
    private LocalDateTime judgedAt;
}