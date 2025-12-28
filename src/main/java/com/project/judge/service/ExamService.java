package com.project.judge.service;

import com.github.dockerjava.api.exception.UnauthorizedException;
import com.project.judge.dto.request.CreateExamRequest;
import com.project.judge.dto.request.UpdateExamRequest;
import com.project.judge.dto.response.ExamDetailResponse;
import com.project.judge.dto.response.ExamResponse;
import com.project.judge.exception.BadRequestException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.*;
import com.project.judge.repository.*;
import com.project.judge.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamRepo examRepo;
    private final GroupRepo groupRepo;
    private final UserRepo userRepo;
    private final GroupMemberRepo groupMemberRepo;
    private final SubmissionRepo submissionRepo;
    private final ExamResultRepo examResultRepo;

    @Transactional
    public ExamResponse createExam(CreateExamRequest request, String instructorId){
        log.info("Creating exam: {} for group: {}", request.getTitle(), request.getGroupId());

        AppUser instructor = userRepo.findByUserId(instructorId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "Instructor not found with id: " + instructorId));

        Group group = groupRepo.findById(request.getGroupId())
                .orElseThrow(() ->  new NotFoundException("GROUP_NOT_FOUND", "Group not found with id: " + request.getGroupId()));


        if(!group.getInstructor().getUserId().equals(instructorId)){
            throw new UnauthorizedException("You can only create exams for your own groups");
        }

        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                throw new BadRequestException("End time must be after start time");
            }
        }

        String examId = IdGenerator.generateId("EXM",5);

        Exam exam = Exam.builder()
                .examId(examId)
                .group(group)
                .instructor(instructor)
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(request.getDurationMinutes())
                .isActive(false)
                .build();

        exam = examRepo.save(exam);
        log.info("Exam created successfully: {} (no problems yet)", examId);

        return mapToExamResponse(exam);
    }

    @Transactional
    public void activateExam(String examId, String instructorId){
        log.info("Activating exam: {} by user: {}", examId, instructorId);

        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new NotFoundException("EXAM_NOT_FOUND", "Exam not found with id: " + examId));

        if(!exam.getInstructor().getUserId().equals(instructorId)){
            throw new UnauthorizedException("You can only activate exams created by you");
        }

        if(exam.getProblems().isEmpty()){
            throw new BadRequestException("Cannot activate exam without problems");
        }

        for(Problem problem : exam.getProblems()){
            if(problem.getTestCases().isEmpty()){
                throw new BadRequestException(
                        String.format("Problem '%s' has no test cases", problem.getTitle())
                );
            }
        }

        exam.setIsActive(true);
        examRepo.save(exam);

        log.info("Exam {} activated successfully with {} problems",
                examId, exam.getProblems().size());
    }


    @Transactional
    public void deactivateExam(String examId, String instructorId){
        log.info("Deactivating exam: {} by user: {}", examId, instructorId);

        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new NotFoundException("EXAM_NOT_FOUND", "Exam not found with id: " + examId));

        if(!exam.getInstructor().getUserId().equals(instructorId)){
            throw new UnauthorizedException("You can only deactivate exams created by you");
        }

        exam.setIsActive(false);
        examRepo.save(exam);
        log.info("Exam {} deactivated successfully", examId);
    }



    @Transactional(readOnly = true)
    public List<ExamResponse> getGroupExams(String groupId, String userId){
        log.info("Fetching exams for group {}", groupId);

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new NotFoundException(
                        "GROUP_NOT_FOUND",
                        "Group not found with id: " + groupId)
                );

        AppUser user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "USER_NOT_FOUND",
                        "User not found with id: " + userId)
                );

        boolean isInstructor = group.getInstructor().getUserId().equals(userId);
        boolean isMember = groupMemberRepo.existsById(new GroupMemberId(groupId, userId));
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if(!isInstructor && !isMember && !isAdmin){
            throw new UnauthorizedException("You don't have access to this group");
        }

        List<Exam> exams = examRepo.findByGroupGroupId(groupId);

        if(user.getRole() == Role.STUDENT && !isInstructor && !isAdmin){
            LocalDateTime now = LocalDateTime.now();
            exams = exams.stream()
                    .filter(exam -> exam.getIsActive() && isExamAvailable(exam, now))
                    .toList();
        }

        return exams.stream()
                .map(this::mapToExamResponse)
                .toList();
    }


    public ExamDetailResponse getExamDetails(String examId, String userId){
        log.info("Fetching exam details{} for user: {}", examId, userId);

        Exam exam = examRepo.findByIdWithProblems(examId)
                .orElseThrow(() -> new NotFoundException("EXAM_NOT_FOUND", "Exam not found"));

        AppUser user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

        boolean isInstructor = exam.getInstructor().getUserId().equals(userId);
        boolean isMember = groupMemberRepo.existsById(new GroupMemberId(exam.getGroup().getGroupId(), userId));
        boolean isAdmin = user.getRole() == Role.ADMIN;

        if(!isInstructor && !isMember && !isAdmin){
            throw new UnauthorizedException("You don't have access to this exam");
        }

        if(user.getRole() == Role.STUDENT && !isInstructor){
            if(!exam.getIsActive() || !isExamAvailable(exam, LocalDateTime.now())){
                throw new UnauthorizedException("This exam is not currently available");
            }
        }

        return mapToExamDetailsResponse(exam, user);
    }



    @Transactional
    public ExamResponse updateExam(String examId, UpdateExamRequest request, String instructorId){
        log.info("Updating exam {}", examId);

        Exam exam = examRepo.findById(examId)
                .orElseThrow(() -> new NotFoundException("EXAM_NOT_FOUND", "Exam not found"));

        if(!exam.getInstructor().getUserId().equals(instructorId)){
            throw new UnauthorizedException("You can only update exams created by you");
        }

        long startedCount = examResultRepo.findByExamExamId(examId).stream().
                filter(r -> r.getStatus() != ExamStatus.NOT_STARTED)
                .count();

        if(startedCount > 0){
            throw new BadRequestException("Cannot update exam - students have already started");
        }

        if (request.getTitle() != null) exam.setTitle(request.getTitle());
        if (request.getDescription() != null) exam.setDescription(request.getDescription());
        if (request.getStartTime() != null) exam.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) exam.setEndTime(request.getEndTime());
        if (request.getDurationMinutes() != null) exam.setDurationMinutes(request.getDurationMinutes());

        if (exam.getStartTime() != null && exam.getEndTime() != null) {
            if (exam.getEndTime().isBefore(exam.getStartTime())) {
                throw new BadRequestException("End time must be after start time");
            }
        }

        examRepo.save(exam);
        log.info("Exam {} updated successfully", examId);

        return mapToExamResponse(exam);
    }



    //Helper methods
    private ExamResponse mapToExamResponse(Exam exam) {
        return ExamResponse.builder()
                .examId(exam.getExamId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .groupId(exam.getGroup().getGroupId())
                .groupName(exam.getGroup().getGroupName())
                .instructorName(exam.getInstructor().getFullName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .durationMinutes(exam.getDurationMinutes())
                .isActive(exam.getIsActive())
                .problemCount(exam.getProblems().size())
                .createdAt(exam.getCreatedAt())
                .build();
    }


    private ExamDetailResponse mapToExamDetailsResponse(Exam exam, AppUser user){
        boolean isStudent = user.getRole() == Role.STUDENT;
        boolean isAdmin = user.getRole() == Role.ADMIN;
        boolean isInstructor = exam.getInstructor().getUserId().equals(user.getUserId());

        ExamDetailResponse.ExamDetailResponseBuilder builder = ExamDetailResponse.builder()
                .examId(exam.getExamId())
                .title(exam.getTitle())
                .description(exam.getDescription())
                .groupId(exam.getGroup().getGroupId())
                .groupName(exam.getGroup().getGroupName())
                .instructorName(exam.getInstructor().getFullName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .durationMinutes(exam.getDurationMinutes())
                .isActive(exam.getIsActive())
                .createdAt(exam.getCreatedAt());

        List<ExamDetailResponse.ProblemInfo> problemInfos = exam.getProblems().stream()
                .sorted(Comparator.comparingInt(p -> p.getOrderIndex() != null ? p.getOrderIndex() : 0))
                .map(p -> mapToProblemInfo(p, isStudent, user.getUserId()))
                .collect(Collectors.toList());
        builder.problems(problemInfos);

        if(isStudent){
            examResultRepo.findByExamExamIdAndStudentUserId(exam.getExamId(), user.getUserId())
                    .ifPresent(result -> {
                        builder.studentStatus(result.getStatus());
                        builder.studentScore(result.getTotalScore());
                        builder.studentPercentage(result.getPercentage());
                        builder.studentStartedAt(result.getStartedAt());
                        builder.studentTimeSpentMinutes(result.getTimeSpentMinutes());

                        // Calculate remaining time
                        if (result.getStatus() == ExamStatus.IN_PROGRESS &&
                                exam.getDurationMinutes() != null &&
                                result.getStartedAt() != null) {

                            LocalDateTime deadline = result.getStartedAt()
                                    .plusMinutes(exam.getDurationMinutes());
                            long minutesRemaining = java.time.Duration
                                    .between(LocalDateTime.now(), deadline).toMinutes();
                            builder.studentTimeRemainingMinutes((int) Math.max(0, minutesRemaining));
                        }
                    });
        }

        if (isInstructor || isAdmin) {
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

            int highestScore = results.stream()
                    .filter(r -> r.getStatus() == ExamStatus.COMPLETED)
                    .mapToInt(ExamResult::getTotalScore)
                    .max()
                    .orElse(0);

            int lowestScore = results.stream()
                    .filter(r -> r.getStatus() == ExamStatus.COMPLETED)
                    .mapToInt(ExamResult::getTotalScore)
                    .min()
                    .orElse(0);

            ExamDetailResponse.ExamStatistics statistics = ExamDetailResponse.ExamStatistics.builder()
                    .totalStudents(exam.getGroup().getMembers().size())
                    .studentsStarted((int) studentsStarted)
                    .studentsCompleted((int) studentsCompleted)
                    .averageScore(averageScore)
                    .highestScore(highestScore)
                    .lowestScore(lowestScore)
                    .build();

            builder.statistics(statistics);
        }

        return builder.build();

    }



    private ExamDetailResponse.ProblemInfo mapToProblemInfo(
            Problem problem, boolean isStudent, String studentId) {

        ExamDetailResponse.ProblemInfo.ProblemInfoBuilder builder =
                ExamDetailResponse.ProblemInfo.builder()
                        .problemId(problem.getProblemId())
                        .title(problem.getTitle())
                        .description(problem.getDescription())
                        .points(problem.getPoints())
                        .timeLimit(problem.getTimeLimit())
                        .memoryLimit(problem.getMemoryLimit())
                        .orderIndex(problem.getOrderIndex());

        if(!isStudent){
            builder.testCaseCount(problem.getTestCases().size());
        }

        if(isStudent){
            List<Submission> submissions = submissionRepo
                    .findByStudentUserIdAndProblemProblemIdOrderBySubmittedAtDesc(
                            studentId, problem.getProblemId());

            if(!submissions.isEmpty()){
                Submission bestSubmission = submissions.stream()
                        .max(Comparator.comparingInt(s -> s.getScore() != null ? s.getScore() : 0))
                        .orElse(null);

                builder.myBestScore(bestSubmission.getScore());
                builder.myBestStatus(bestSubmission.getStatus());
                builder.myAttempts(submissions.size());
            }
        }

        return builder.build();
    }

    private boolean isExamAvailable(Exam exam, LocalDateTime now) {
        if (exam.getStartTime() == null || exam.getEndTime() == null) {
            return true; // No time restrictions
        }
        return !now.isBefore(exam.getStartTime()) && !now.isAfter(exam.getEndTime());
    }
}
