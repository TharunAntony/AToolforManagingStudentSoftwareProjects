package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.PeerReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PeerReviewService {
    @Autowired
    private PeerReviewRepository peerReviewRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private StudentService studentService;


    public boolean studentReviewed(User student, Project project) {
        List<PeerReview> reviews = peerReviewRepository.findByReviewerAndProject(student, project);
        return reviews != null && !reviews.isEmpty();
    }

    public List<PeerReview> getPeerReviews(User currentStudent, List<GroupMember> groupMembers, Groups group, Project project) {
        List<PeerReview> peerReviewList = new ArrayList<>();

        for (GroupMember groupMember : groupMembers) {
            Long groupMemberId = groupMember.getStudent().getId();
            Long currentUserId = currentStudent.getStudentDetails().getId();

            if (!groupMemberId.equals(currentUserId)) {
                User reviewee = groupMember.getStudent().getStudent();

                PeerReview existing = peerReviewRepository.findByReviewerAndRevieweeAndProject(currentStudent, reviewee, project);

                if (existing != null) {
                    peerReviewList.add(existing);
                } else {
                    PeerReview peerReview = new PeerReview();
                    peerReview.setReviewee(reviewee);
                    peerReview.setGroup(group);
                    peerReview.setProject(project);
                    peerReviewList.add(peerReview);
                }
            }
        }

        return peerReviewList;
    }


    public void savePeerReview(User Reviewer, User Reviewee, Long projectId, int score, String comment) {
        Project project = projectService.getProjectById(projectId);
        Groups group = studentService.getGroupForProject(Reviewer, project);

         PeerReview peerReview = peerReviewRepository.findByReviewerAndRevieweeAndProject(Reviewer, Reviewee, project);

        if (peerReview == null) {
            peerReview = new PeerReview();
            peerReview.setReviewer(Reviewer);
            peerReview.setReviewee(Reviewee);
            peerReview.setProject(project);
            peerReview.setGroup(group);
        }

        peerReview.setScore(score);
        peerReview.setComment(comment);

        peerReviewRepository.save(peerReview);
    }




}
