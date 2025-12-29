package com.project.judge.repository;

import com.project.judge.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepo extends JpaRepository<Submission, Long> {

    List<Submission> findByStudentUserIdAndProblemProblemIdOrderBySubmittedAtDesc(
            String studentId, Long problemId);

    List<Submission> findByStudentUserIdAndProblemExamExamId(String studentId, String examId);

    @Query("SELECT s FROM Submission s LEFT JOIN FETCH s.testCaseResults " +
            "WHERE s.submissionId = :submissionId")
    Optional<Submission> findByIdWithTestCaseResults(@Param("submissionId") Long id);
}
