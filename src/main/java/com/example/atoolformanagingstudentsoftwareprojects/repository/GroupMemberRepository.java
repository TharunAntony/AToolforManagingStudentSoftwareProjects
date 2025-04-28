package com.example.atoolformanagingstudentsoftwareprojects.repository;

import com.example.atoolformanagingstudentsoftwareprojects.model.GroupMember;
import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroup_Project(Project project);
}
