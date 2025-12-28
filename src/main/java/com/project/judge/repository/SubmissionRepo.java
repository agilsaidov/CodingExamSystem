package com.project.judge.repository;

import com.project.judge.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepo extends JpaRepository<Submission, Long> {

    List<Submission> findByStudentUserIdAndProblemProblemIdOrderBySubmittedAtDesc(
            String studentId, Long problemId);
}
