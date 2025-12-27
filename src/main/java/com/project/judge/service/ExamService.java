package com.project.judge.service;

import com.github.dockerjava.api.exception.UnauthorizedException;
import com.project.judge.dto.request.CreateExamRequest;
import com.project.judge.dto.response.ExamResponse;
import com.project.judge.exception.BadRequestException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.AppUser;
import com.project.judge.model.Exam;
import com.project.judge.model.Group;
import com.project.judge.repository.ExamRepo;
import com.project.judge.repository.GroupMemberRepo;
import com.project.judge.repository.GroupRepo;
import com.project.judge.repository.UserRepo;
import com.project.judge.utils.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExamService {

    private final ExamRepo examRepo;
    private final GroupRepo groupRepo;
    private final UserRepo userRepo;

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
}
