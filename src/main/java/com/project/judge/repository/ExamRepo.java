package com.project.judge.repository;

import com.project.judge.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepo extends JpaRepository<Exam, String> {

    List<Exam> findByGroupGroupId(String groupId);

    @Query("SELECT e FROM Exam e LEFT JOIN FETCH e.problems WHERE e.examId = :examId")
    Optional<Exam> findByIdWithProblems(@Param("examId") String examId);
}
