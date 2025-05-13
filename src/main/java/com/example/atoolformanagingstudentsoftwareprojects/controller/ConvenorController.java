package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.dto.MarkListForm;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private GroupService groupService;
    @Autowired
    private PeerReviewRepository peerReviewRepository;
    @Autowired
    private GroupsRepository groupsRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private MarkRepository markRepository;


    @GetMapping("/home")
    public String convenorHome(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        List<Project> projects = projectRepository.findByConvenor(user.getConvenorDetails());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("projects", projects);
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

    @GetMapping("/{projectId}/peerReviews")
    public String showPeerReviewGroups(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProjectById(projectId);
        List<Groups> groups = project.getGroups();

        model.addAttribute("project", project);
        model.addAttribute("groups", groups);
        return "convenor/peerReviewGroups";
    }

    @GetMapping("/{projectId}/peerReviews/{groupId}")
    public String viewGroupPeerReviews(@PathVariable Long projectId, @PathVariable Long groupId, Model model) {
        Project project = projectService.getProjectById(projectId);
        Groups group = groupService.getGroupById(groupId);
        List<PeerReview> reviews = peerReviewRepository.findByProjectAndGroup(project, group);
        List<GroupMember> groupMembers = groupMemberRepository.findByGroup(group);

        List<Mark> marks = new ArrayList<>();
        for (GroupMember member : groupMembers) {
            Mark mark = markRepository.findByStudentAndProject(member.getStudent(), project);
            if (mark != null) {
                marks.add(mark);
            }
        }

        model.addAttribute("project", project);
        model.addAttribute("group", group);
        model.addAttribute("reviews", reviews);
        model.addAttribute("marks", marks);
        return "convenor/viewGroupPeerReviews";

    }

    @PostMapping("/{projectId}/peerReviews/{groupId}/updateMarks")
    public String updateGroupMarks(@PathVariable Long projectId, @PathVariable Long groupId, @ModelAttribute MarkListForm marksList, RedirectAttributes redirectAttributes) {

        if (marksList != null && marksList.getMarks() != null) {
            markService.updateAdjustedMarks(marksList.getMarks());
            redirectAttributes.addFlashAttribute("success", "Marks updated successfully.");
        }

        return "redirect:/convenor/" + projectId + "/peerReviews/" + groupId;
    }




}
