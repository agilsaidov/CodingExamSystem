package com.project.judge.repository;

import com.project.judge.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepo extends JpaRepository<Exam, String> {

    List<Exam> findByGroupGroupId(String groupId);
}
