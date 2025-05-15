package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.GroupMemberRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.MarkRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.PeerReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MarkService {

    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private PeerReviewRepository peerReviewRepository;
    @Autowired
    private MarkRepository markRepository;

    // Calculates and saves adjusted individual marks for all students in each group
    public void calculateAndSaveAdjustedMarks(List<Groups> groupList) {

        for (Groups group : groupList) {

            //Get the submission for the group and skip if it doesn’t exist or hasn’t been marked
            Submission submission = group.getSubmission();
            if (submission == null || !submission.isSubmitted() || submission.getFinalGroupMark() == null) {
                continue;
            }

            //Store the original group mark
            double groupMark = submission.getFinalGroupMark();

            //Get all users in the group
            List<GroupMember> members = groupMemberRepository.findByGroup(group);
            List<User> users = new ArrayList<>();
            for (GroupMember gm : members) {
                users.add(gm.getStudent().getStudent());
            }

            //Get all peer reviews for this group
            List<PeerReview> peerReviews = peerReviewRepository.findByProjectAndGroup(group.getProject(), group);

            // Prepare a map to store all scores each student received
            Map<Long, List<Integer>> receivedScores = new HashMap<>();
            for (User user : users) {
                receivedScores.put(user.getId(), new ArrayList<>());
            }

            //Fill in the map with actual review scores for each student
            for (PeerReview review : peerReviews) {
                if (review.getReviewee() != null && receivedScores.containsKey(review.getReviewee().getId())) {
                    receivedScores.get(review.getReviewee().getId()).add(review.getScore());
                }
            }

            //If too few peer reviews were submitted, just return the raw group mark
            int expectedReviews = users.size() * (users.size() - 1); // everyone reviews everyone (except themselves)
            if (peerReviews.size() < expectedReviews / 2.0) {
                for (GroupMember gm : members) {
                    StudentDetails student = gm.getStudent();
                    Mark mark = markRepository.findByStudentAndProject(student, group.getProject());
                    if (mark == null) {
                        mark = new Mark();
                        mark.setStudent(student);
                        mark.setProject(group.getProject());
                    }
                    mark.setOriginalMark(groupMark);
                    mark.setAdjustedMark(Math.min(groupMark, 100.0));
                    markRepository.save(mark);
                }
                continue; // skip to next group
            }

            //Calculate each student’s average received score
            Map<Long, Double> averages = new HashMap<>();
            List<Double> allAverages = new ArrayList<>();

            for (User user : users) {
                List<Integer> scores = receivedScores.get(user.getId());
                if (scores == null || scores.isEmpty()) {
                    averages.put(user.getId(), 0.0);
                    allAverages.add(0.0);
                    continue;
                }

                int total = 0;
                for (int s : scores) {
                    total += s;
                }
                double avg = (double) total / scores.size();
                averages.put(user.getId(), avg);
                allAverages.add(avg);
            }

            //Calculate the overall average across the group (used for normalisation)
            double groupAvg = 0.0;
            if (allAverages.size() > 0) {
                double total = 0;
                for (double a : allAverages) {
                    total += a;
                }
                groupAvg = total / allAverages.size();
            }

            // If group average is somehow 0 (shouldn’t happen), skip
            if (groupAvg == 0) {
                continue;
            }

            //Now apply the mark adjustment for each student
            for (User user : users) {
                StudentDetails student = user.getStudentDetails();
                double studentAvg = averages.get(user.getId());

                //Normalise student score by comparing it to the group average
                double factor = studentAvg / groupAvg;

                //Clamp factor to be between 0.9 and 1.1 to prevent extreme changes
                if (factor < 0.9) factor = 0.9;
                if (factor > 1.1) factor = 1.1;

                // Multiply group mark by factor to get adjusted mark
                double finalMark = groupMark * factor;
                finalMark = Math.round(finalMark * 100.0) / 100.0;

                //Cap to 100
                if (finalMark > 100) finalMark = 100;

                //Save adjusted mark to database
                Mark mark = markRepository.findByStudentAndProject(student, group.getProject());
                if (mark == null) {
                    mark = new Mark();
                    mark.setStudent(student);
                    mark.setProject(group.getProject());
                }

                mark.setOriginalMark(groupMark);
                mark.setAdjustedMark(finalMark);
                markRepository.save(mark);
            }
        }
    }


    public void updateAdjustedMarks(List<Mark> marks) {

        for (Mark mark : marks) {
            Mark existing = markRepository.findById(mark.getId()).orElse(null);
            if (existing != null) {
                existing.setAdjustedMark(mark.getAdjustedMark());
                markRepository.save(existing);
            }
        }

    }


}
