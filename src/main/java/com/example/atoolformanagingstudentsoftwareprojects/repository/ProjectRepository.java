package com.example.atoolformanagingstudentsoftwareprojects.repository;

import com.example.atoolformanagingstudentsoftwareprojects.model.ConvenorDetails;
import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    public List<Project> findByConvenor(ConvenorDetails convenor);
}
