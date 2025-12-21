package com.project.judge.service;

import com.github.dockerjava.api.exception.UnauthorizedException;
import com.project.judge.auth.dto.response.GroupListResponse;
import com.project.judge.dto.request.CreateGroupRequest;
import com.project.judge.exception.BadRequestException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.*;
import com.project.judge.repository.ExamRepo;
import com.project.judge.repository.GroupMemberRepo;
import com.project.judge.repository.GroupRepo;
import com.project.judge.repository.UserRepo;
import com.project.judge.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupService {

    private final GroupRepo groupRepo;
    private final UserRepo userRepo;
    private final GroupMemberRepo groupMemberRepo;
    private final ExamRepo examRepo;


    @Transactional(readOnly = true)
    public List<GroupListResponse> getMyGroups(String userId) {

        log.info("Fetching groups for user {}", userId);

        AppUser user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found with userId: " + userId));

        List<Group> groups;

        if(user.getRole() == Role.INSTRUCTOR || user.getRole() == Role.ADMIN){
            groups = groupRepo.findByInstructorUserId(userId);

        }else{

            List<GroupMember> memberships = groupMemberRepo.findByStudentUserId(userId);
            groups = memberships.stream()
                    .map(GroupMember::getGroup)
                    .collect(Collectors.toList());
        }

        return groups.stream()
                .map(group -> mapToGroupListResponse(group, user.getRole(),
                        user.getRole() == Role.STUDENT ? userId : null))
                .collect(Collectors.toList());


    }


    @Transactional
    public GroupListResponse createGroup(CreateGroupRequest request, String instructorId) {
        log.info("Creating group: {} by instructor: {}", request.getGroupName(), instructorId);

        AppUser instructor = userRepo.findByUserId(instructorId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found with userId: " + instructorId));

        String groupId = IdGenerator.generateId("GRP", 5);

        Group group = Group.builder()
                .groupId(groupId)
                .groupName(request.getGroupName())
                .instructor(instructor)
                .build();

        group = groupRepo.save(group);
        log.info("Group created successfully: {} ", groupId);

        return mapToGroupListResponse(group, Role.INSTRUCTOR,null);
    }

    @Transactional
    public void addStudentToGroup(String groupId, String studentId, String instructorId) {
        log.info("Adding student {} to group {}", studentId, groupId);

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException("GROUP_NOT_FOUND", "Group not found with groupId: " + groupId));

        if(!group.getInstructor().getUserId().equals(instructorId)){
            throw new UnauthorizedException("You can only add students to your own groups");
        }

        AppUser student = userRepo.findByUserId(studentId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found with studentId: " + studentId));

        if(student.getRole() != Role.STUDENT){
            throw new BadRequestException("User is not a student");
        }

        GroupMemberId memberId = new GroupMemberId(groupId, studentId);
        if(groupMemberRepo.existsById(memberId)){
            throw new BadRequestException("User is already in group");
        }

        GroupMember groupMember = GroupMember.builder()
                .id(memberId)
                .group(group)
                .student(student)
                .build();

        groupMemberRepo.save(groupMember);
        log.info("Added student {} to group {}", studentId, groupId);
    }



    // Helper Methods
    private GroupListResponse mapToGroupListResponse(Group group, Role userRole, String studentId) {
        GroupListResponse.GroupListResponseBuilder builder = GroupListResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .instructorName(group.getInstructor().getFullName())
                .memberCount(group.getMembers().size())
                .createdAt(group.getCreatedAt());

        // Count active exams
        List<Exam> exams = examRepo.findByGroupGroupId(group.getGroupId());
        long activeExamCount = exams.stream()
                .filter(Exam::getIsActive)
                .filter(this::isExamCurrentlyAvailable)
                .count();
        builder.activeExamCount((int) activeExamCount);

        return builder.build();
    }


    private boolean isExamCurrentlyAvailable(Exam exam) {
        LocalDateTime now = LocalDateTime.now();
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            return false;
        }
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            return false;
        }
        return true;
    }

}
