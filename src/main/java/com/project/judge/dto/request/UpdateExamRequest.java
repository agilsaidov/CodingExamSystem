package com.project.judge.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateExamRequest {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
}