package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.dto.ProjectForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.ConvenorDetails;
import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/convenor")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/create-project")
    public String showCreateProjectForm(Model model) {
        model.addAttribute("projectForm", new ProjectForm());
        return "convenor/createProject";
    }

    @PostMapping("/create-project")
    public String createProject(@ModelAttribute ProjectForm projectForm, @AuthenticationPrincipal CurrentUser currentUser) {
        Project project = new Project();
        project.setTitle(projectForm.getTitle());
        project.setDescription(projectForm.getDescription());
        project.setGroupCapacity(projectForm.getGroupCapacity());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime deadline = LocalDateTime.parse(projectForm.getDeadline(), formatter);
        project.setDeadline(deadline);

        ConvenorDetails convenorDetails = currentUser.getUser().getConvenorDetails();
        project.setConvenor(convenorDetails);

        projectRepository.save(project);

        return "redirect:/convenor/home";
    }

}
