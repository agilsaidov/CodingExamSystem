package com.project.judge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_case_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id", nullable = false)
    private TestCase testCase;
    
    @Column(length = 50, nullable = false)
    private String status;
    
    @Column(name = "actual_output", columnDefinition = "TEXT")
    private String actualOutput;
    
    @Column(name = "execution_time")
    private Double executionTime;
    
    @Column(name = "memory_used")
    private Integer memoryUsed;
    
    @Column(columnDefinition = "TEXT")
    private String stderr;
    
    @Column(name = "compile_output", columnDefinition = "TEXT")
    private String compileOutput;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}