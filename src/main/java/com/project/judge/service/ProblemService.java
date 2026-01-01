package com.project.judge.service;

import com.github.dockerjava.api.exception.UnauthorizedException;
import com.project.judge.dto.request.CreateProblemRequest;
import com.project.judge.dto.request.CreateTestCaseRequest;
import com.project.judge.dto.response.ProblemResponse;
import com.project.judge.exception.BadRequestException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.Exam;
import com.project.judge.model.Problem;
import com.project.judge.model.TestCase;
import com.project.judge.repository.ExamRepo;
import com.project.judge.repository.ProblemRepo;
import com.project.judge.repository.TestCaseRepo;
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
    private final TestCaseRepo testCaseRepo;

    @Transactional
    public ProblemResponse createProblem(CreateProblemRequest request, String instructorId){
        log.info("Creating problem for exam: {}", request.getExamId());

        Exam exam = examRepo.findById(request.getExamId())
                .orElseThrow(() -> new NotFoundException("EXAM_NOT_FOUND", "Exam not found"));

        if(!exam.getInstructor().getUserId().equals(instructorId)){
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



    @Transactional
    public void addTestCase(Long problemId, CreateTestCaseRequest request, String instructorId){
        log.info("Adding test case for problem: {}", problemId);

        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> new NotFoundException("PROBLEM_NOT_FOUND", "Problem not found"));

        if(!problem.getExam().getInstructor().getUserId().equals(instructorId)){
            throw new UnauthorizedException("You can only add test cases to your own problems");
        }

        if(problem.getExam().getIsActive()){
            throw new BadRequestException("Cannot add test cases to an active exam");
        }

        TestCase testCase = TestCase.builder()
                .problem(problem)
                .input(request.getInput())
                .expectedOutput(request.getExpectedOutput())
                .isHidden(request.getIsHidden() != null ? request.getIsHidden() : false)
                .orderIndex(request.getOrderIndex())
                .points(request.getPoints())
                .build();

        testCaseRepo.save(testCase);
        log.info("Test case added to problem: {}", problemId);
    }


    @Transactional
    public void deleteProblem(Long problemId, String instructorId) {
        log.info("Deleting problem: {}", problemId);

        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(() -> new NotFoundException("PROBLEM_NOT_FOUND", "Problem not found"));

        if (!problem.getExam().getInstructor().getUserId().equals(instructorId)) {
            throw new UnauthorizedException("You can only delete your own problems");
        }

        if (problem.getExam().getIsActive()) {
            throw new BadRequestException("Cannot delete problems from an active exam");
        }

        problemRepo.delete(problem);
        log.info("Problem deleted: {}", problemId);
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
