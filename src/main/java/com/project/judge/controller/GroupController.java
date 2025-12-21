package com.project.judge.controller;

import com.project.judge.auth.dto.response.GroupListResponse;
import com.project.judge.dto.request.CreateGroupRequest;
import com.project.judge.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    public ResponseEntity<GroupListResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            Authentication authentication
            ){
        GroupListResponse response = groupService.createGroup(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("{groupId}/members/{studentId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    public ResponseEntity<Void> addStudent(@PathVariable String groupId,
                                           @PathVariable String studentId,
                                           Authentication authentication){

        groupService.addStudentToGroup(groupId, studentId, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

