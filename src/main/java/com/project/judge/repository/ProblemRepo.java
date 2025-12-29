package com.project.judge.repository;

import com.project.judge.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepo extends JpaRepository<Problem, Long> {
    Optional<Problem> findByIdWithTestCases(Long problemId);
}
