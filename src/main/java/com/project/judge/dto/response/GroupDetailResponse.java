package com.project.judge.dto.response;

import com.project.judge.model.ExamStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GroupDetailResponse {
    private String groupId;
    private String groupName;
    private String instructorId;
    private String instructorName;
    private Integer memberCount;
    private LocalDateTime createdAt;
    
    // Only for teachers and admins - full member list
    private List<MemberInfo> members;
    
    // Exams - role-based filtering
    private List<ExamSummary> exams;

    @Data
    @Builder
    public static class MemberInfo {
        private String studentId;
        private String studentName;
        private String username;
        private LocalDateTime joinedAt;
    }

    @Data
    @Builder
    public static class ExamSummary {
        private String examId;
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer durationMinutes;
        private Boolean isActive;
        private Integer problemCount;
        private Integer totalPoints;
        
        // Student-specific - only shown to students
        private ExamStatus myStatus;
        private Integer myScore;
        private BigDecimal myPercentage;
        private LocalDateTime myStartedAt;
        private Integer myTimeRemainingMinutes;
        
        // Teacher-specific - only shown to teachers/admins
        private Integer studentsStarted;
        private Integer studentsCompleted;
        private Double averageScore;
    }
}