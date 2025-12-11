package com.project.judge.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberId implements Serializable {
    
    @Column(name = "group_id", length = 10)
    private String groupId;
    
    @Column(name = "student_id", length = 10)
    private String studentId;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMemberId that = (GroupMemberId) o;
        return Objects.equals(groupId, that.groupId) && 
               Objects.equals(studentId, that.studentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(groupId, studentId);
    }
}