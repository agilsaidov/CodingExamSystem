package com.project.judge.service;

import com.github.dockerjava.api.exception.UnauthorizedException;
import com.project.judge.dto.request.CreateProblemRequest;
import com.project.judge.dto.response.ProblemResponse;
import com.project.judge.exception.BadRequestException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.Exam;
import com.project.judge.model.Problem;
import com.project.judge.repository.ExamRepo;
import com.project.judge.repository.ProblemRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProblemService {

    private final ExamRepo examRepo;
    private final ProblemRepo problemRepo;

    @Transactional
    public ProblemResponse createProblem(CreateProblemRequest request, String instructorId){
        log.info("Creating problem for exam: {}", request.getExamId());

        Exam exam = examRepo.findById(request.getExamId())
                .orElseThrow(() -> new NotFoundException("EXAM_NOT_FOUND", "Exam not found"));

        if(!exam.getInstructor().equals(instructorId)){
            throw new UnauthorizedException("You can only add problems to your own exams");
        }

        if(exam.getIsActive()){
            throw new BadRequestException("Cannot add problems to an active exam");
        }

        Problem problem = Problem.builder()
                .exam(exam)
                .title(request.getTitle())
                .description(request.getDescription())
                .points(request.getPoints())
                .timeLimit(request.getTimeLimit())
                .memoryLimit(request.getMemoryLimit())
                .orderIndex(request.getOrderIndex())
                .build();

        problem = problemRepo.save(problem);
        log.info("Created problem: {}", problem.getProblemId());

        return mapToProblemResponse(problem);
    }







    //Helper methods
    private ProblemResponse mapToProblemResponse(Problem problem){
        return ProblemResponse.builder()
                .problemId(problem.getProblemId())
                .examId(problem.getExam().getExamId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .points(problem.getPoints())
                .timeLimit(problem.getTimeLimit())
                .memoryLimit(problem.getMemoryLimit())
                .orderIndex(problem.getOrderIndex())
                .testCaseCount(problem.getTestCases().size())
                .build();
    }
}
