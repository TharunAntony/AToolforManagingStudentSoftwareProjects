package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.dto.MarkListForm;
import com.example.atoolformanagingstudentsoftwareprojects.dto.PeerReviewList;
import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.GroupMemberRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.MarkRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.PeerReviewRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PeerReviewController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private PeerReviewService peerReviewService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private PeerReviewRepository peerReviewRepository;
    @Autowired
    private GroupService groupService;
    @Autowired
    private MarkRepository markRepository;
    @Autowired
    private MarkService markService;


    //Student peer review pages
    @GetMapping("/student/peerReview")
    public String peerReview(@AuthenticationPrincipal CurrentUser currentUser, Model model){
        User user = currentUser.getUser();
        List<Project> pendingReviewProjects = new ArrayList<>();
        List<Project> submittedReviewProjects = new ArrayList<>();

        List<Groups> studentGroups = studentService.getStudentGroups(user.getStudentDetails().getId());

        for (Groups group : studentGroups) {
            Submission submission = group.getSubmission();

            if (submission != null && submission.isSubmitted()) {
                Project project = group.getProject();

                boolean hasGivenReviews = peerReviewService.studentReviewed(user, project);
                if (hasGivenReviews) {
                    submittedReviewProjects.add(project);
                } else {
                    pendingReviewProjects.add(project);
                }
            }
        }

        model.addAttribute("pendingReviewProjects", pendingReviewProjects);
        model.addAttribute("submittedReviewProjects", submittedReviewProjects);
        return "student/peerReviews";
    }

    @GetMapping("/student/peerReview/project/{projectId}")
    public String showPeerReviewForm(@PathVariable Long projectId, @AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User currentStudent = currentUser.getUser();
        Project project = projectService.getProjectById(projectId);

        Groups group = studentService.getGroupForProject(currentStudent, project);
        if (group == null) {
            model.addAttribute("error", "You are not part of a group for this project.");
            return "redirect:/student/peerReview";
        }

        List<GroupMember> groupMembers = groupMemberRepository.findByGroup(group);
        List<PeerReview> peerReviewList = peerReviewService.getPeerReviews(currentStudent, groupMembers, group, project);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadlineWithBuffer = project.getDeadline().plusDays(3);

        boolean alreadyReviewed = peerReviewService.studentReviewed(currentStudent, project);
        boolean canEdit = now.isBefore(deadlineWithBuffer);


        model.addAttribute("canEdit", canEdit);
        model.addAttribute("alreadyReviewed", alreadyReviewed);
        model.addAttribute("peerReviews", peerReviewList);
        model.addAttribute("project", project);

        if (!canEdit && alreadyReviewed) {
            List<PeerReview> submittedReviews = peerReviewRepository.findByReviewerAndProject(currentStudent, project);
            model.addAttribute("submittedReviews", submittedReviews);
        }

        return "student/givePeerReviews";
    }

    @PostMapping("/student/peerReview/project/{projectId}/submit")
    public String submitPeerReviews(@PathVariable Long projectId, @AuthenticationPrincipal CurrentUser currentUser, @ModelAttribute PeerReviewList peerReviewList, RedirectAttributes redirectAttributes) {
        User reviewer = currentUser.getUser();

        Project project = projectService.getProjectById(projectId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadlineWithBuffer = project.getDeadline().plusDays(3);

        if (now.isAfter(deadlineWithBuffer)) {
            redirectAttributes.addFlashAttribute("error", "Peer review deadline has passed. You cannot submit now.");
            return "redirect:/student/peerReview";
        }


        for (PeerReview peerReview : peerReviewList.getPeerReviews()) {
            if (peerReview.getScore() > 0 && peerReview.getReviewee() != null) {
                peerReviewService.savePeerReview(reviewer, peerReview.getReviewee(), projectId, peerReview.getScore(), peerReview.getComment());
            }
        }

        redirectAttributes.addFlashAttribute("success", "Your peer reviews have been submitted.");
        return "redirect:/student/peerReview";
    }


    //Convenor peer review pages
    @GetMapping("/convenor/{projectId}/peerReviews")
    public String showPeerReviewGroups(@PathVariable Long projectId, Model model) {
        Project project = projectService.getProjectById(projectId);
        List<Groups> groups = project.getGroups();

        model.addAttribute("project", project);
        model.addAttribute("groups", groups);
        return "convenor/peerReviewGroups";
    }

    @GetMapping("/convenor/{projectId}/peerReviews/{groupId}")
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

    @PostMapping("/convenor/{projectId}/peerReviews/{groupId}/updateMarks")
    public String updateGroupMarks(@PathVariable Long projectId, @PathVariable Long groupId, @ModelAttribute MarkListForm marksList, RedirectAttributes redirectAttributes) {

        if (marksList != null && marksList.getMarks() != null) {
            markService.updateAdjustedMarks(marksList.getMarks());
            redirectAttributes.addFlashAttribute("success", "Marks updated successfully.");
        }

        return "redirect:/convenor/" + projectId + "/peerReviews/" + groupId;
    }


}
