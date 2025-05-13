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

    public void calculateAndSaveAdjustedMarks(List<Groups> groupList) {

        for (Groups group : groupList) {

            Submission submission = group.getSubmission();

            if (submission == null || !submission.isSubmitted() || submission.getFinalGroupMark() == null) {
                continue;
            }

            double groupMark = submission.getFinalGroupMark();

            List<GroupMember> members = groupMemberRepository.findByGroup(group);
            List<User> users = new ArrayList<>();
            for (GroupMember gm : members) {
                users.add(gm.getStudent().getStudent());
            }

            List<PeerReview> peerReviews = peerReviewRepository.findByProjectAndGroup(group.getProject(), group);

            Map<Long, List<Integer>> receivedScores = new HashMap<>();
            for (User user : users) {
                receivedScores.put(user.getId(), new ArrayList<>());
            }

            for (PeerReview review : peerReviews) {
                if (review.getReviewee() != null && receivedScores.containsKey(review.getReviewee().getId())) {
                    receivedScores.get(review.getReviewee().getId()).add(review.getScore());
                }
            }

            int expectedReviews = users.size() * (users.size() - 1);
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
                continue;
            }

            Map<Long, Double> averages = new HashMap<>();
            List<Double> allAverages = new ArrayList<>();

            for (User user : users) {
                List<Integer> scores = receivedScores.get(user.getId());
                if (scores == null || scores.size() == 0) {
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

            double groupAvg = 0.0;
            if (allAverages.size() > 0) {
                double total = 0;
                for (double a : allAverages) {
                    total += a;
                }
                groupAvg = total / allAverages.size();
            }

            if (groupAvg == 0) {
                continue;
            }

            for (User user : users) {
                StudentDetails student = user.getStudentDetails();
                double studentAvg = averages.get(user.getId());

                double factor = studentAvg / groupAvg;
                if (factor < 0.8) factor = 0.8;
                if (factor > 1.2) factor = 1.2;

                double finalMark = groupMark * factor;
                finalMark = Math.round(finalMark * 100.0) / 100.0;
                if (finalMark > 100) finalMark = 100;

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
