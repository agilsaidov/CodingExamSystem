package com.project.judge.dto.response;

import com.project.judge.model.ExamStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExamDetailResponse {
    private String examId;
    private String title;
    private String description;
    private String groupId;
    private String groupName;
    private String instructorName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<ProblemInfo> problems;

    // Teacher/Admin-specific info
    private ExamStatistics statistics;
    
    // For students
    private ExamStatus studentStatus;
    private Integer studentScore;
    private BigDecimal studentPercentage;
    private LocalDateTime studentStartedAt;
    private Integer studentTimeSpentMinutes;
    private Integer studentTimeRemainingMinutes;

    @Data
    @Builder
    public static class ProblemInfo {
        private Long problemId;
        private String title;
        private String description;
        private Integer points;
        private Integer timeLimit;
        private Integer memoryLimit;
        private Integer orderIndex;

        // Only for teachers/admins
        private Integer testCaseCount;

        // Only for students - their submission info
        private Integer myBestScore;      // Best score achieved (e.g., 80 out of 100)
        private String myBestStatus;      // Status of the best submission (e.g., "ACCEPTED", "WRONG_ANSWER")
        private Integer myAttempts;       // Total number of attempts/submissions
    }

    @Data
    @Builder
    public static class ExamStatistics {
        private Integer totalStudents;
        private Integer studentsStarted;
        private Integer studentsCompleted;
        private Double averageScore;
        private Integer highestScore;
        private Integer lowestScore;
    }
}