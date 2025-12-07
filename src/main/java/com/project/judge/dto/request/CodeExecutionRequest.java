package com.project.judge.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class CodeExecutionRequest {

    @NotNull(message = "code field can't be null")
    private String code;
}
