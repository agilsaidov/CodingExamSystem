package com.project.judge.service;

import com.github.dockerjava.api.exception.UnauthorizedException;
import com.project.judge.constant.JudgeStatus;
import com.project.judge.dto.request.SubmitCodeRequest;
import com.project.judge.dto.response.JudgeSubmissionResponse;
import com.project.judge.dto.response.SubmissionResponse;
import com.project.judge.exception.BadRequestException;
import com.project.judge.exception.NotFoundException;
import com.project.judge.model.*;
import com.project.judge.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionService {

    private final ProblemRepo problemRepo;
    private final UserRepo userRepo;
    private final GroupRepo groupRepo;
    private final GroupMemberRepo groupMemberRepo;
    private final ExamResultRepo examResultRepo;
    private final SubmissionRepo submissionRepo;
    private final TestCaseResultRepo testCaseResultRepo;
    private final JudgeService judgeService;

    @Transactional
    public SubmissionResponse submitCode(SubmitCodeRequest request, String studentId){
        log.info("Processing submission for problem: {} by student: {}" ,
                request.getProblemId(), studentId);

        Problem problem = problemRepo.findByIdWithTestCases(request.getProblemId())
                .orElseThrow(() -> new NotFoundException("PROBLEM_NOT_FOUND", "Problem not found"));

        AppUser student = userRepo.findByUserId(studentId)
                .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND", "User not found"));

        if(student.getRole() != Role.STUDENT){
            throw new UnauthorizedException("Only students can submit solutions");
        }

        Exam exam = problem.getExam();

        validateExamAccess(exam, studentId);

        validateExamTiming(exam);

        ExamResult examResult = getOrCreateExamResult(exam, student);

        if (examResult.getStatus() == ExamStatus.NOT_STARTED) {
            examResult.setStatus(ExamStatus.IN_PROGRESS);
            examResult.setStartedAt(LocalDateTime.now());
            examResultRepo.save(examResult);
        }

        // Check if exam time has expired for this student
        if (exam.getDurationMinutes() != null && examResult.getStartedAt() != null) {
            LocalDateTime expiryTime = examResult.getStartedAt()
                    .plusMinutes(exam.getDurationMinutes());
            if (LocalDateTime.now().isAfter(expiryTime)) {
                throw new BadRequestException("Exam time has expired");
            }
        }
        Submission submission = Submission.builder()
                .student(student)
                .problem(problem)
                .sourceCode(request.getSourceCode())
                .languageId(request.getLanguageId())
                .status("PENDING")
                .totalTestCases(problem.getTestCases().size())
                .passedTestCases(0)
                .score(0)
                .build();

        submission = submissionRepo.save(submission);
        log.info("Submission created: {}", submission.getSubmissionId());

        // Run code against test cases asynchronously
        processSubmissionAsync(submission, problem);

        return mapToSubmissionResponse(submission);

    }








    //Helper methods
    private void processSubmissionAsync(Submission submission, Problem problem) {
        // In production, this will be async using @Async or message queue
        try {
            log.info("Running submission {} against {} test cases",
                    submission.getSubmissionId(), problem.getTestCases().size());

            List<JudgeSubmissionResponse> results = judgeService.submitWithTestCases(
                    submission.getSourceCode(),
                    submission.getLanguageId(),
                    problem.getTestCases(),
                    problem.getTimeLimit(),
                    problem.getMemoryLimit()
            );

            // Save test case results
            int passedCount = 0;
            int totalScore = 0;

            for (int i = 0; i < results.size(); i++) {
                JudgeSubmissionResponse judgeResult = results.get(i);
                TestCase testCase = problem.getTestCases().get(i);

                boolean passed = judgeResult.getStatus() != null &&
                        JudgeStatus.isAccepted(judgeResult.getStatus().getId());

                TestCaseResult testCaseResult = TestCaseResult.builder()
                        .submission(submission)
                        .testCase(testCase)
                        .status(judgeResult.getStatus() != null ?
                                judgeResult.getStatus().getDescription() : "ERROR")
                        .actualOutput(judgeResult.getStdout())
                        .executionTime(judgeResult.getTime() != null ?
                                judgeResult.getTime() : 0.0)
                        .memoryUsed(judgeResult.getMemory())
                        .stderr(judgeResult.getStderr())
                        .compileOutput(judgeResult.getCompileOutput())
                        .build();

                testCaseResultRepo.save(testCaseResult);

                if (passed) {
                    passedCount++;
                    if (testCase.getPoints() != null) {
                        totalScore += testCase.getPoints();
                    }
                }
            }

            submission.setPassedTestCases(passedCount);
            submission.setScore(totalScore);
            submission.setStatus(passedCount == problem.getTestCases().size() ?
                    "ACCEPTED" : "WRONG_ANSWER");
            submission.setJudgedAt(LocalDateTime.now());
            submissionRepo.save(submission);


            updateExamResult(submission);

            log.info("Submission {} completed: {}/{} passed, score: {}",
                    submission.getSubmissionId(), passedCount,
                    problem.getTestCases().size(), totalScore);

        } catch (Exception e) {
            log.error("Error processing submission: {}", submission.getSubmissionId(), e);
            submission.setStatus("ERROR");
            submission.setJudgedAt(LocalDateTime.now());
            submissionRepo.save(submission);
        }
    }

    private void updateExamResult(Submission submission) {
        Problem problem = submission.getProblem();
        Exam exam = problem.getExam();
        AppUser student = submission.getStudent();

        ExamResult examResult = examResultRepo
                .findByExamExamIdAndStudentUserId(exam.getExamId(), student.getUserId())
                .orElseThrow(() -> new NotFoundException("RESULT_NOT_FOUND","Exam result not found"));


        // Recalculate total score from all submissions
        List<Submission> allSubmissions = submissionRepo
                .findByStudentUserIdAndProblemExamExamId(student.getUserId(), exam.getExamId());

        // Get best submission for each problem
        int totalScore = allSubmissions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getProblem().getProblemId(),
                        Collectors.maxBy(Comparator.comparingInt(s -> s.getScore() != null ? s.getScore() : 0))
                ))
                .values().stream()
                .filter(Optional::isPresent)
                .mapToInt(opt -> opt.get().getScore() != null ? opt.get().getScore() : 0)
                .sum();

        examResult.setTotalScore(totalScore);
        examResult.calculatePercentage();
        examResultRepo.save(examResult);
    }


    private void validateExamAccess(Exam exam, String studentId){
        GroupMemberId memberId = new  GroupMemberId(exam.getGroup().getGroupId(), studentId);

        if(!groupMemberRepo.existsById(memberId)){
            throw new UnauthorizedException("You are not a member of this exam's group");
        }
    }

    private  void validateExamTiming(Exam exam){

        if(!exam.getIsActive()){
            throw new BadRequestException("This exam is not active");
        }

        LocalDateTime now = LocalDateTime.now();
        if(exam.getStartTime() != null && now.isBefore(exam.getStartTime())){
            throw new BadRequestException("Exam has not started yet");
        }

        if(exam.getEndTime() != null && now.isAfter(exam.getEndTime())){
            throw new BadRequestException("Exam has ended");
        }
    }

    private ExamResult getOrCreateExamResult(Exam exam, AppUser student) {
        return examResultRepo
                .findByExamExamIdAndStudentUserId(exam.getExamId(), student.getUserId())
                .orElseGet(() -> {
                    int maxScore = exam.getProblems().stream()
                            .mapToInt(Problem::getPoints)
                            .sum();

                    ExamResult result = ExamResult.builder()
                            .exam(exam)
                            .student(student)
                            .status(ExamStatus.NOT_STARTED)
                            .totalScore(0)
                            .maxPossibleScore(maxScore)
                            .build();

                    return examResultRepo.save(result);
                });
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
        return SubmissionResponse.builder()
                .submissionId(submission.getSubmissionId())
                .problemId(submission.getProblem().getProblemId())
                .problemTitle(submission.getProblem().getTitle())
                .languageId(submission.getLanguageId())
                .status(submission.getStatus())
                .score(submission.getScore())
                .passedTestCases(submission.getPassedTestCases())
                .totalTestCases(submission.getTotalTestCases())
                .submittedAt(submission.getSubmittedAt())
                .judgedAt(submission.getJudgedAt())
                .build();
    }

}
