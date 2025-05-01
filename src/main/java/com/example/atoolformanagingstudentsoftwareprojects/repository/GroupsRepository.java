package com.example.atoolformanagingstudentsoftwareprojects.repository;

import com.example.atoolformanagingstudentsoftwareprojects.model.Groups;
import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupsRepository extends JpaRepository<Groups, Long> {

    void deleteByProjectId(Long projectId);
}
