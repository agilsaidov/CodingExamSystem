package com.project.judge.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SubmissionDetailResponse {
    private Long submissionId;
    private Long problemId;
    private String problemTitle;
    private String studentId;
    private String studentName;
    private String sourceCode;
    private Integer languageId;
    private String status;
    private Integer score;
    private Integer passedTestCases;
    private Integer totalTestCases;
    private LocalDateTime submittedAt;
    private LocalDateTime judgedAt;
    private List<TestCaseResultInfo> testCaseResults;

    @Data
    @Builder
    public static class TestCaseResultInfo {
        private Long testCaseId;
        private Boolean isHidden;
        private String status;
        private Double executionTime;
        private Integer memoryUsed;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private String stderr;
    }
}