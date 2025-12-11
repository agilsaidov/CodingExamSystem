package com.project.judge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;
    
    @Column(name = "source_code", columnDefinition = "TEXT", nullable = false)
    private String sourceCode;
    
    @Column(name = "language_id", nullable = false)
    private Integer languageId;
    
    @Column(length = 50, nullable = false)
    private String status;
    
    @Column
    private Integer score;
    
    @Column(name = "passed_test_cases")
    private Integer passedTestCases;
    
    @Column(name = "total_test_cases")
    private Integer totalTestCases;
    
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;
    
    @Column(name = "judged_at")
    private LocalDateTime judgedAt;
    
    // Relationships
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestCaseResult> testCaseResults = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}