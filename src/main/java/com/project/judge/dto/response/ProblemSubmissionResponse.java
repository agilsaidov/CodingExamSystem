package com.project.judge.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Judge0SubmissionResponse {
    
    private String token;
    
    private Status status;
    
    private String stdout;
    
    private String stderr;
    
    @JsonProperty("compile_output")
    private String compileOutput;
    
    private String message;
    
    private Double time; // execution time in seconds
    
    private Integer memory; // memory used in KB