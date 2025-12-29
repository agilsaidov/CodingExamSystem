package com.project.judge.repository;

import com.project.judge.model.TestCaseResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestCaseResultRepo extends JpaRepository<TestCaseResult, Long> {
}
