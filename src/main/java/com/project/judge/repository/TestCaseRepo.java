package com.project.judge.repository;

import com.project.judge.model.TestCase;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCaseRepo extends JpaRepository<TestCase, Long> { }

