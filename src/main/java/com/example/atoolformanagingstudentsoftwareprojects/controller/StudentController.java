package com.example.atoolformanagingstudentsoftwareprojects.controller;

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
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private MarkRepository markRepository;

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal CurrentUser currentUser) {
        User user = currentUser.getUser();
        List<Project> projects = projectRepository.findBystudents(user.getStudentDetails());

        List<Project> currentProjects = new ArrayList<>();
        List<Project> completedProjects = new ArrayList<>();
        List<Groups> studentGroups = studentService.getStudentGroups(user.getStudentDetails().getId());


        for(Groups group : studentGroups){
            Submission submission = group.getSubmission();
            if(submission == null || !submission.isSubmitted()){
                currentProjects.add(group.getProject());
            }else{
                completedProjects.add(group.getProject());
            }
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("projects", projects);
        model.addAttribute("completedProjects", completedProjects);
        model.addAttribute("currentProjects", currentProjects);
        return "student/home";
    }

    @GetMapping("/projects")
    public String viewStudentProjects(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        List<Project> studentProjects = studentService.getStudentProjects(user);

        model.addAttribute("projects", studentProjects);
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("username", user.getUsername());

        return "student/projects";
    }

    @GetMapping("/project/{projectId}")
    public String viewStudentProject(@PathVariable Long projectId, @AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();

        Project project = projectService.getProjectById(projectId);

        //Find all group memberships for this student
        List<GroupMember> memberships = groupMemberRepository.findByStudent(user.getStudentDetails());

        Groups studentGroup = null;

        //Check if the student is assigned to a group for this project
        for (GroupMember groupMember : memberships) {
            if (groupMember.getGroup() != null && groupMember.getGroup().getProject().getId().equals(project.getId())) {
                studentGroup = groupMember.getGroup();
                break;
            }
        }

        List<GroupMember> groupMembers = new ArrayList<>();
        if (studentGroup != null) {
            groupMembers = groupMemberRepository.findByGroup(studentGroup);
            Submission submission = submissionService.findByGroup(studentGroup);
            model.addAttribute("submission", submission);
        }else {
            model.addAttribute("submission", null);
        }

        Mark mark = markRepository.findByStudentAndProject(user.getStudentDetails(), project);
        if (mark != null && mark.getAdjustedMark() != null) {
            model.addAttribute("adjustedMark", mark.getAdjustedMark());
        } else {
            model.addAttribute("adjustedMark", null);
        }

        model.addAttribute("project", project);
        model.addAttribute("group", studentGroup);
        model.addAttribute("groupMembers", groupMembers);



        return "student/viewProject";
    }

    @PostMapping("/project/{projectId}/submit")
    public String submitProject(@RequestParam Long groupId, @AuthenticationPrincipal CurrentUser currentUser, RedirectAttributes redirectAttributes) {

        Groups group = studentService.getGroupById(groupId);
        if (group == null) {
            redirectAttributes.addFlashAttribute("error", "Group not found.");
            return "redirect:/student/project/" + groupId;
        }

        Submission submission = submissionService.findByGroup(group);
        if (submission != null && submission.isSubmitted()) {
            redirectAttributes.addFlashAttribute("error", "This group has already submitted the project.");
            return "redirect:/student/project/" + group.getProject().getId();
        }

        if (submission == null) {
            submission = new Submission();
            submission.setGroup(group);
            submission.setProject(group.getProject());
        }

        submission.setSubmitted(true);
        submission.setSubmittedAt(LocalDateTime.now());

        LocalDateTime deadline = group.getProject().getDeadline();
        if (deadline != null && submission.getSubmittedAt().isAfter(deadline)) {
            submission.setLate(true);
        }

        submissionService.saveSubmission(submission);

        redirectAttributes.addFlashAttribute("success", "Project submitted successfully!");
        return "redirect:/student/home";
    }


}
