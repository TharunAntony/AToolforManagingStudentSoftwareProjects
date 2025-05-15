package com.example.atoolformanagingstudentsoftwareprojects.controller;


import com.example.atoolformanagingstudentsoftwareprojects.dto.SubmissionMarkForm;
import com.example.atoolformanagingstudentsoftwareprojects.dto.SubmissionMarkFormList;
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
@RequestMapping("/convenor")
public class ConvenorController {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private MarkService markService;


    @GetMapping("/home")
    public String convenorHome(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        List<Project> projects = projectRepository.findByConvenor(user.getConvenorDetails());
        List<Project> currentProjects = new ArrayList<>();

        for (Project project : projects) {
            if(project.getDeadline().isAfter(LocalDateTime.now())) {
                currentProjects.add(project);
            }
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("projects", currentProjects);
        return "convenor/home";
    }

    @GetMapping("/submissions")
    public String showProjectSubmissions(@AuthenticationPrincipal CurrentUser currentUser, Model model) {

        User user = currentUser.getUser();
        List<Project> projects = projectService.getProjects(user);
        List<Project> projectsWithSubmissions = new ArrayList<>();

        for(Project project : projects){
            int submissionCount = 0;

            for(Groups group : project.getGroups()){
                Submission submission = group.getSubmission();
                if(submission != null && submission.isSubmitted()){
                    submissionCount++;
                }
            }

            if(submissionCount > 0){
                project.setSubmissionCount(submissionCount); // transient field
                projectsWithSubmissions.add(project);
            }
        }

        model.addAttribute("projectsWithSubmissions", projectsWithSubmissions);

        return "convenor/submissions";
    }

    @GetMapping("/submissions/project/{projectId}")
    public String viewProjectSubmissions(@PathVariable Long projectId, Model model) {

        Project project = projectService.getProjectById(projectId);
        List<Groups> groups = project.getGroups();

        List<Submission> submissions = new ArrayList<>();

        for (Groups group : groups) {
            Submission submission = group.getSubmission();
            if (submission != null && submission.isSubmitted()) {
                submissions.add(submission);
            }
        }

        boolean canReturnMarks = false;
        if (project.getDeadline() != null) {
            LocalDateTime cutoff = project.getDeadline().plusDays(3);
            canReturnMarks = LocalDateTime.now().isAfter(cutoff);
        }


        model.addAttribute("project", project);
        model.addAttribute("submissions", submissions);
        model.addAttribute("canReturnMarks", canReturnMarks);
        return "convenor/projectSubmissions";
    }

    @PostMapping("/submissions/project/{projectId}/saveMarks")
    public String saveMarks(@PathVariable Long projectId, @ModelAttribute("marksList") SubmissionMarkFormList marksList, RedirectAttributes redirectAttributes) {

        if (marksList != null && marksList.getMarkForms() != null) {
            for (SubmissionMarkForm form : marksList.getMarkForms()) {
                Submission submission = submissionService.findById(form.getSubmissionId());
                if (submission != null) {
                    submission.setFinalGroupMark(form.getMark());
                    submissionService.saveSubmission(submission);
                }
            }
        }

        List<Groups> markedGroups = new ArrayList<>();
        for (SubmissionMarkForm form : marksList.getMarkForms()) {
            Submission submission = submissionService.findById(form.getSubmissionId());
            if (submission != null) {
                submission.setFinalGroupMark(form.getMark());
                submissionService.saveSubmission(submission);
                markedGroups.add(submission.getGroup());
            }
        }

        markService.calculateAndSaveAdjustedMarks(markedGroups);

        redirectAttributes.addFlashAttribute("success", "Marks saved successfully!");
        return "redirect:/convenor/submissions/project/" + projectId;
    }

}
