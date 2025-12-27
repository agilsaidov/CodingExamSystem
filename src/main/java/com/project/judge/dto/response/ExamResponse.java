package com.project.judge.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ExamResponse {
    private String examId;
    private String title;
    private String description;
    private String groupId;
    private String groupName;
    private String instructorName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Boolean isActive;
    private Integer problemCount;
    private LocalDateTime createdAt;
}