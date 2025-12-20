package com.project.judge.repository;

import com.project.judge.model.GroupMember;
import com.project.judge.model.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepo extends JpaRepository<GroupMember, GroupMemberId> {

    List<GroupMember> findByStudentUserId(String studentId);

    List<GroupMember> findByGroupGroupId(String groupId);
}
