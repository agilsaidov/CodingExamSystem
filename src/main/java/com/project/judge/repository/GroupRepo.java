package com.project.judge.repository;

import com.project.judge.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepo extends JpaRepository<Group, String> {
    List<Group> findByInstructorUserId(String instructorId);

    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.members WHERE g.groupId = :groupId")
    Optional<Group> findByIdWithMembers(@Param("groupId") String groupId);

    @Query("SELECT DISTINCT g FROM Group g " +
            "LEFT JOIN FETCH g.members " +
            "LEFT JOIN FETCH g.exams " +
            "WHERE g.groupId = :groupId")
    Optional<Group> findByIdWithMembersAndExams(@Param("groupId") String groupId);

    boolean existsByGroupId(String groupId);
}
