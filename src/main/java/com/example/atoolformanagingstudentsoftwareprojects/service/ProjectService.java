package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.dto.ProjectForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.ConvenorDetails;
import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    public void saveProject(ProjectForm form,@AuthenticationPrincipal CurrentUser currentUser) {
        Project project = new Project();
        project.setTitle(form.getTitle());
        project.setDescription(form.getDescription());
        project.setGroupCapacity(form.getGroupCapacity());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime deadline = LocalDateTime.parse(form.getDeadline(), formatter);
        project.setDeadline(deadline);

        ConvenorDetails convenorDetails = currentUser.getUser().getConvenorDetails();
        project.setConvenor(convenorDetails);

        projectRepository.save(project);
    }

    public List<Project> getProjects(User user) {
        return projectRepository.findByConvenor(user.getConvenorDetails());
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + id));
    }

}
