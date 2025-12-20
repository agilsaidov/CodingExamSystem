package com.project.judge.auth.dto.response;

import com.project.judge.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String userId;
    private String username;
    private Role role;
    private String fullName;
    private List<GroupListResponse> groups;
}