package com.project.judge.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeSubmissionRequest {
    
    @JsonProperty("source_code")
    private String sourceCode;
    
    @JsonProperty("language_id")
    private Integer languageId;
    
    @JsonProperty("stdin")
    private String stdin;
    
    @JsonProperty("expected_output")
    private String expectedOutput;
    
    @JsonProperty("cpu_time_limit")
    private Double cpuTimeLimit;
    
    @JsonProperty("memory_limit")
    private Integer memoryLimit; //KB
    
    @JsonProperty("wall_time_limit")
    private Double wallTimeLimit;
    
    @JsonProperty("stack_limit")
    private Integer stackLimit; // KB
    
    @JsonProperty("max_processes_and_or_threads")
    private Integer maxProcessesAndOrThreads;
    
    @JsonProperty("enable_per_process_and_thread_time_limit")
    private Boolean enablePerProcessAndThreadTimeLimit;
    
    @JsonProperty("enable_per_process_and_thread_memory_limit")
    private Boolean enablePerProcessAndThreadMemoryLimit;
    
    @JsonProperty("max_file_size")
    private Integer maxFileSize; // KB
}