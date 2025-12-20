package com.project.judge.auth.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GroupListResponse {
    private String groupId;
    private String groupName;
    private String instructorName;
    private Integer memberCount;
    private Integer activeExamCount;
    private LocalDateTime createdAt;
}