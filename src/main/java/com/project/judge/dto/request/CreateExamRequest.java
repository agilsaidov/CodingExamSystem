package com.project.judge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateExamRequest {
    @NotBlank(message = "Group ID is required")
    private String groupId;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255)
    private String title;

    @Size(max = 5000)
    private String description;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;
}
