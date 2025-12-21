package com.project.judge.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class GroupListResponse {
    @JsonProperty("group_id")
    private String groupId;
    @JsonProperty("group_name")
    private String groupName;
    @JsonProperty("instructor_name")
    private String instructorName;
    @JsonProperty("member_count")
    private Integer memberCount;
    @JsonProperty("active_exam_count")
    private Integer activeExamCount;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}