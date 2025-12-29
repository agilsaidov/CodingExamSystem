package com.project.judge.controller;

import com.project.judge.dto.request.CreateExamRequest;
import com.project.judge.dto.request.UpdateExamRequest;
import com.project.judge.dto.response.ExamDetailResponse;
import com.project.judge.dto.response.ExamResponse;
import com.project.judge.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exam")
public class ExamController {

    private final ExamService examService;

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    public ResponseEntity<ExamResponse> createExam(@Valid @RequestBody CreateExamRequest createExamRequest,
                                                   Authentication authentication) {

        ExamResponse response = examService.createExam(createExamRequest, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{examId}/activate")
    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    public ResponseEntity<Void> activateExam(@PathVariable String examId,
                                             Authentication authentication) {

        examService.activateExam(examId, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{examId}/deactivate")
    @PreAuthorize("hasAnyRole('INSTRUCTOR')")
    public ResponseEntity<Void> deactivateExam(@PathVariable String examId,
                                               Authentication authentication) {

        examService.deactivateExam(examId, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExamResponse>> getGroupExams(@PathVariable String groupId,
                                                            Authentication authentication) {

        List<ExamResponse> response = examService.getGroupExams(groupId, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ExamDetailResponse> getExamDetails(@PathVariable String examId,
                                                             Authentication authentication) {

        ExamDetailResponse response = examService.getExamDetails(examId, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PutMapping("/{examId}")
    public ResponseEntity<ExamResponse> updateExam(@PathVariable String examId,
                                                   @Valid @RequestBody UpdateExamRequest request,
                                                   Authentication authentication){

        ExamResponse response = examService.updateExam(examId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @DeleteMapping("/{examId}")
    public ResponseEntity<Void> deleteExam(@PathVariable String examId,
                                           Authentication authentication){

        examService.deleteExam(examId, authentication.getName());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
