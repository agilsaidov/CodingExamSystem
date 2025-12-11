package com.project.judge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;
    
    @Column(name = "total_score", nullable = false)
    private Integer totalScore = 0;
    
    @Column(name = "max_possible_score", nullable = false)
    private Integer maxPossibleScore;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal percentage;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    
    @Column(name = "time_spent_minutes")
    private Integer timeSpentMinutes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Helper method to calculate percentage
    public void calculatePercentage() {
        if (maxPossibleScore != null && maxPossibleScore > 0) {
            this.percentage = BigDecimal.valueOf(totalScore)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(maxPossibleScore), 2, RoundingMode.HALF_UP);
        }
    }
}