package com.project.judge.repository;

import com.project.judge.model.AppUser;
import com.project.judge.model.Exam;
import com.project.judge.model.ExamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepo extends JpaRepository<ExamResult, Long> {

    Optional<ExamResult> findByExamExamIdAndStudentUserId(String examId, String userId);

    List<ExamResult> findByExamExamId(String examId);

    List<ExamResult> findByStudentUserId(String studentId);
}
