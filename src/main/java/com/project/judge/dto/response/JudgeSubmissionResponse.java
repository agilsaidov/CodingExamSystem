package com.project.judge.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgeSubmissionResponse {
    
    private String token;
    
    private Status status;
    
    private String stdout;
    
    private String stderr;
    
    @JsonProperty("compile_output")
    private String compileOutput;
    
    private String message;
    
    private Double time;
    
    private Integer memory;
    
    @JsonProperty("wall_time")
    private Double wallTime;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Status {
        private Integer id;
        private String description;
    }
}