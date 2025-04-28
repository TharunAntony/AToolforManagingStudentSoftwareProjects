package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.dto.ProjectForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.model.StudentDetails;
import com.example.atoolformanagingstudentsoftwareprojects.model.Groups;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.StudentDetailsRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.CurrentUser;
import com.example.atoolformanagingstudentsoftwareprojects.service.CustomUserDetailsService;
import com.example.atoolformanagingstudentsoftwareprojects.service.GroupService;
import com.example.atoolformanagingstudentsoftwareprojects.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/convenor")
public class ProjectController {
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private StudentDetailsRepository studentDetailsRepository;
    @Autowired
    private CustomUserDetailsService userService;
    @Autowired
    private GroupService groupService;

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


    @GetMapping("/project/{projectId}/manageStudents")
    public String manageStudents(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProjectById(projectId);

        List<User> availableStudents = projectService.getAvailableStudentsForProject(project);
        List<User> assignedStudents = projectService.getStudentsAssignedToProject(project);

        model.addAttribute("project", project);
        model.addAttribute("availableStudents", availableStudents);
        model.addAttribute("assignedStudents", assignedStudents);
        return "convenor/manageStudents";
    }

    // Add students to the project
    @PostMapping("/project/{projectId}/manageStudents/add")
    public String addStudentsToProject(@PathVariable Long projectId, @RequestParam(value = "studentIds", required = false) List<Long> studentIds, RedirectAttributes redirectAttributes) {

        if (studentIds == null || studentIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one student to add.");
            return "redirect:/convenor/project/{projectId}/manageStudents";
        }
        Project project = projectService.getProjectById(projectId);

        for (Long studentId : studentIds) {
            User studentUser = userService.getUserById(studentId);
            StudentDetails studentDetails = studentDetailsRepository.findByStudent(studentUser);

            if (studentDetails != null && !project.getStudents().contains(studentDetails)) {
                project.getStudents().add(studentDetails);
            }
        }

        projectService.updateProject(project);
        return "redirect:/convenor/project/{projectId}/manageStudents";
    }

    // Remove students from the project
    @PostMapping("/project/{projectId}/manageStudents/remove")
    public String removeStudentsFromProject(@PathVariable Long projectId, @RequestParam(value = "studentIds", required = false) List<Long> studentIds, RedirectAttributes redirectAttributes) {
        if (studentIds == null || studentIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one student to add.");
            return "redirect:/convenor/project/{projectId}/manageStudents";
        }
        Project project = projectService.getProjectById(projectId);

        project.getStudents().removeIf(details -> studentIds.contains(details.getStudent().getId()));

        projectService.updateProject(project);
        return "redirect:/convenor/project/{projectId}/manageStudents";
    }


    @GetMapping("/project/{projectId}/assignGroups")
    public String showAssignGroupsPage(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProjectById(projectId);
        List<User> assignedStudents = projectService.getStudentsAssignedToProject(project);
        List<Groups> groups = projectService.getGroupsForProject(project);

        model.addAttribute("project", project);
        model.addAttribute("assignedStudents", assignedStudents);
        model.addAttribute("groups", groups);

        //Group compatibility scores
        Map<Long, Double> groupScores = groupService.evaluateGroupCompatibility(groups);
        model.addAttribute("groupScores", groupScores);
        return "convenor/assignGroups";
    }

    @PostMapping("/project/{projectId}/assignGroups/autoAllocate")
    public String autoAllocateGroups(@PathVariable Long projectId, RedirectAttributes redirectAttributes) {
        try {
            groupService.autoAllocateGroups(projectId);
            redirectAttributes.addFlashAttribute("success", "Groups allocated successfully.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("error", "Failed to allocate groups.");
        }
        return "redirect:/convenor/project/" + projectId + "/assignGroups";
    }



}
