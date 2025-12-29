package com.project.judge.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitCodeRequest {
    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotBlank(message = "Source code is required")
    @Size(max = 100000, message = "Source code too large")
    private String sourceCode;

    @NotNull(message = "Language ID is required")
    @Min(value = 1, message = "Invalid language ID")
    private Integer languageId;
}
