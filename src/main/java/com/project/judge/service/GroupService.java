package com.project.judge.service;

import com.github.dockerjava.api.exception.UnauthorizedException;
import com.project.judge.auth.dto.response.GroupListResponse;
import com.project.judge.dto.request.CreateGroupRequest;
import com.project.judge.dto.response.GroupDetailResponse;
import com.project.judge.exception.BadRequestException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.*;
import com.project.judge.repository.*;
import com.project.judge.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
    private final ExamResultRepo examResultRepo;


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


    @Transactional(readOnly = true)
    public GroupDetailResponse getGroupDetails(String groupId, String userId){
        log.info("Fetching group details {} for user {}", groupId, userId);

        Group group = groupRepo.findByIdWithMembers(groupId)
                .orElseThrow(() -> new NotFoundException("GROUP_NOT_FOUND","Group not found"));

        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found with userId: " + userId));

        boolean isInstructor = group.getInstructor().getUserId().equals(userId);
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isMember = user.getRole() == Role.STUDENT &&
                groupMemberRepo.existsById(new GroupMemberId(groupId, userId));

        if (!isInstructor && !isAdmin && !isMember) {
            throw new UnauthorizedException("You don't have access to this group");
        }

        return mapToGroupDetailResponse(group, user);
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



    @Transactional
    public void removeStudentFromGroup(String groupId, String studentId, String instructorId) {
        log.info("Removing student {} from group {}", studentId, groupId);

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException("GROUP_NOT_FOUND", "Group not found with groupId: " + groupId));

        if(!group.getInstructor().getUserId().equals(instructorId)){
            throw new UnauthorizedException("You can only remove students from your own groups");
        }

        GroupMemberId memberId = new GroupMemberId(groupId, studentId);
        if(!groupMemberRepo.existsById(memberId)){
            throw new NotFoundException("MEMBER_NOT_FOUND","Student is not a member of this group");
        }

        groupMemberRepo.deleteById(memberId);
        log.info("Removed student {} from group {}", studentId, groupId);

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


    private GroupDetailResponse mapToGroupDetailResponse(Group group, AppUser user) {
        boolean isTeacherOrAdmin = user.getRole() == Role.INSTRUCTOR || user.getRole() == Role.ADMIN;
        boolean isStudent = user.getRole() == Role.STUDENT;

        GroupDetailResponse.GroupDetailResponseBuilder builder = GroupDetailResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .instructorId(group.getInstructor().getUserId())
                .instructorName(group.getInstructor().getFullName())
                .memberCount(group.getMembers().size())
                .createdAt(group.getCreatedAt());


        if (isTeacherOrAdmin) {
            List<GroupDetailResponse.MemberInfo> members = group.getMembers().stream()
                    .map(m -> GroupDetailResponse.MemberInfo.builder()
                            .studentId(m.getStudent().getUserId())
                            .studentName(m.getStudent().getFullName())
                            .username(m.getStudent().getUsername())
                            .joinedAt(m.getJoinedAt())
                            .build())
                    .collect(Collectors.toList());
            builder.members(members);
        }

        List<Exam> exams = examRepo.findByGroupGroupId(group.getGroupId());

        if (isStudent) {
            exams = exams.stream()
                    .filter(Exam::getIsActive)
                    .filter(this::isExamCurrentlyAvailable)
                    .collect(Collectors.toList());
        }

        List<GroupDetailResponse.ExamSummary> examSummaries = exams.stream()
                .map(exam -> mapToExamSummary(exam, user))
                .collect(Collectors.toList());

        builder.exams(examSummaries);

        return builder.build();
    }



    private GroupDetailResponse.ExamSummary mapToExamSummary(Exam exam, AppUser user) {
        boolean isStudent = user.getRole() == Role.STUDENT;
        boolean isTeacherOrAdmin = user.getRole() == Role.INSTRUCTOR || user.getRole() == Role.ADMIN;

        GroupDetailResponse.ExamSummary.ExamSummaryBuilder builder = GroupDetailResponse.ExamSummary.builder()
                .examId(exam.getExamId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .durationMinutes(exam.getDurationMinutes())
                .isActive(exam.getIsActive())
                .problemCount(exam.getProblems().size())
                .totalPoints(exam.getProblems().stream()
                        .mapToInt(Problem::getPoints)
                        .sum());

        if (isStudent) {
            examResultRepo.findByExamExamIdAndStudentUserId(exam.getExamId(), user.getUserId())
                    .ifPresent(result -> {
                        builder.myStatus(result.getStatus());
                        builder.myScore(result.getTotalScore());
                        builder.myPercentage(result.getPercentage());
                        builder.myStartedAt(result.getStartedAt());

                        // Calculate remaining time
                        if (result.getStatus() == ExamStatus.IN_PROGRESS &&
                                exam.getDurationMinutes() != null &&
                                result.getStartedAt() != null) {

                            LocalDateTime deadline = result.getStartedAt()
                                    .plusMinutes(exam.getDurationMinutes());
                            long minutesRemaining = Duration.between(LocalDateTime.now(), deadline).toMinutes();
                            builder.myTimeRemainingMinutes((int) Math.max(0, minutesRemaining));
                        }
                    });
        }

        if (isTeacherOrAdmin) {
            List<ExamResult> results = examResultRepo.findByExamExamId(exam.getExamId());

            long studentsStarted = results.stream()
                    .filter(r -> r.getStatus() != ExamStatus.NOT_STARTED)
                    .count();

            long studentsCompleted = results.stream()
                    .filter(r -> r.getStatus() == ExamStatus.COMPLETED)
                    .count();

            double averageScore = results.stream()
                    .filter(r -> r.getStatus() == ExamStatus.COMPLETED)
                    .mapToInt(ExamResult::getTotalScore)
                    .average()
                    .orElse(0.0);

            builder.studentsStarted((int) studentsStarted);
            builder.studentsCompleted((int) studentsCompleted);
            builder.averageScore(averageScore);
        }

        return builder.build();
    }


}
