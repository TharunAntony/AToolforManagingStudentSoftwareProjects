package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.dto.ProjectForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.*;
import com.example.atoolformanagingstudentsoftwareprojects.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/convenor")
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private CustomUserDetailsService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private PeerReviewRepository peerReviewRepository;
    @Autowired
    private GroupMemberService groupMemberService;

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
        //Grab the project
        Project project = projectService.getProjectById(id);

        model.addAttribute("project", project);
        model.addAttribute("groups", project.getGroups());
        model.addAttribute("students", project.getStudents());

        return "convenor/viewProject";
    }

    @GetMapping("/project/{id}/editProject")
    public String editProjectForm(@PathVariable Long id, Model model) {
        Project project  = projectService.getProjectById(id);
        ProjectForm projectForm = projectService.convertToForm(project);
        model.addAttribute("projectForm", projectForm);
        model.addAttribute("project", project);
        return "convenor/editProject";
    }

    @PostMapping("/project/{id}/editProject")
    public String editProject(@ModelAttribute ProjectForm form, @PathVariable Long id, @AuthenticationPrincipal CurrentUser currentUser) {
        Project project = new Project();
        project.setId(id);
        project.setTitle(form.getTitle());
        project.setDescription(form.getDescription());
        project.setGroupCapacity(form.getGroupCapacity());
        project.setStudents(projectService.getProjectById(id).getStudents());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime deadline = LocalDateTime.parse(form.getDeadline(), formatter);
        project.setDeadline(deadline);

        ConvenorDetails convenorDetails = currentUser.getUser().getConvenorDetails();
        project.setConvenor(convenorDetails);

        projectService.updateProject(project);

        return "redirect:/convenor/projects";
    }

    @PostMapping("/project/{id}/delete")
    public String deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return "redirect:/convenor/projects";
    }


    @GetMapping("/listAssignStudents")
    public String showAssignStudents(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        List<Project> projects = projectService.getProjects(user);
        model.addAttribute("username", user.getUsername());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("projects", projects);

        return "convenor/listAssignStudents";
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
            StudentDetails studentDetails = userService.getUserById(studentId).getStudentDetails();

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
            redirectAttributes.addFlashAttribute("error", "Please select at least one student to remove.");
            return "redirect:/convenor/project/{projectId}/manageStudents";
        }
        Project project = projectService.getProjectById(projectId);

        //Remove each student from any group in this project
        for (Long studentId : studentIds) {
            User student = userService.getUserById(studentId);

            //Find any group memberships this student has for the project
            List<GroupMember> groupMemberships = groupMemberService.getAllGroupMemberships(student);
            for (GroupMember gm : groupMemberships) {
                if (gm.getGroup() != null && gm.getGroup().getProject().getId().equals(projectId)) {
                    groupMemberRepository.delete(gm); // remove from group
                }
            }

            // Delete peer reviews made by this student for this project
            List<PeerReview> reviewsByStudent = peerReviewRepository.findByReviewerAndProject(student, project);
            peerReviewRepository.deleteAll(reviewsByStudent);

            //Finally, remove from project
            project.getStudents().removeIf(s -> s.getStudent().getId().equals(studentId));
        }

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

    @PostMapping("/project/{id}/assignGroups/deleteGroups")
    public String deleteGroups(@PathVariable("id") Long projectId, RedirectAttributes redirectAttributes) {
        try {

            projectService.deleteGroupsForProject(projectId);
            redirectAttributes.addFlashAttribute("success", "All groups deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete groups." );
        }
        return "redirect:/convenor/project/" + projectId + "/assignGroups";
    }




}
