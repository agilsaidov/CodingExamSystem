package com.project.judge.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProblemRequest {
    
    @NotBlank(message = "Exam ID is required")
    private String examId;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 10000, message = "Description is too long")
    private String description;

    @NotNull(message = "Points are required")
    @Min(value = 1, message = "Points must be at least 1")
    @Max(value = 100, message = "Points cannot exceed 100")
    private Integer points;

    @Min(value = 1, message = "Time limit must be at least 1 second")
    @Max(value = 60, message = "Time limit cannot exceed 60 seconds")
    private Integer timeLimit;  // Default: 5 seconds

    @Min(value = 1000, message = "Memory limit must be at least 1KB")
    @Max(value = 512000, message = "Memory limit cannot exceed 512MB")
    private Integer memoryLimit;  // Default: 128MB (128000 KB)

    @Min(value = 1, message = "Order index must be at least 1")
    private Integer orderIndex;
}