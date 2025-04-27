package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.dto.ProjectForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.CurrentUser;
import com.example.atoolformanagingstudentsoftwareprojects.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/convenor")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectService projectService;

    @GetMapping("/create-project")
    public String showCreateProjectForm(Model model) {
        model.addAttribute("projectForm", new ProjectForm());
        return "convenor/createProject";
    }

    @PostMapping("/create-project")
    public String createProject(@ModelAttribute ProjectForm projectForm, @AuthenticationPrincipal CurrentUser currentUser) {
        projectService.saveProject(projectForm, currentUser);
        return "redirect:/convenor/home";
    }

    //Shows all projects a convenor is moderating
    @GetMapping("/projects")
    public String showProjects(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        List<Project> projects = projectService.getProjects(user);
        model.addAttribute("projects", projects);
        model.addAttribute("firstName", currentUser.getUser().getFirstName());
        model.addAttribute("username", currentUser.getUsername());

        return "convenor/projects";
    }

    //Shows a page with the details of a single project
    @GetMapping("/project/{id}")
    public String viewProject(@PathVariable Long id, Model model) {
        //Grab the project if it exists, otherwise throw error
        Project project = projectRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found"));

        model.addAttribute("project", project);

        //Add groups and students to the model if needed
        model.addAttribute("groups", project.getGroups());
        model.addAttribute("students", project.getStudents());

        return "convenor/viewProject";
    }

}
