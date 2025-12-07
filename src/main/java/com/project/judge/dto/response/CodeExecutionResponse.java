package com.project.judge.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CodeExecutionResponse {
    private boolean success;
    private String output;
    private String error;
}
