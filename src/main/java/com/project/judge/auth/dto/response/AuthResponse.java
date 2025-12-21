package com.project.judge.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.judge.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    @JsonProperty("user_id")
    private String userId;
    private String username;
    private Role role;
    @JsonProperty("full_name")
    private String fullName;
    private List<GroupListResponse> groups;
}