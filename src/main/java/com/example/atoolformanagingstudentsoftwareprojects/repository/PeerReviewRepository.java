package com.example.atoolformanagingstudentsoftwareprojects.repository;

import com.example.atoolformanagingstudentsoftwareprojects.model.Groups;
import com.example.atoolformanagingstudentsoftwareprojects.model.PeerReview;
import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeerReviewRepository extends JpaRepository<PeerReview, Long> {

     List<PeerReview> findByReviewerAndProject(User reviewer, Project project);
     PeerReview findByReviewerAndRevieweeAndProject(User reviewer, User reviewee, Project project);
     List<PeerReview> findByProjectAndGroup(Project project, Groups group);



}
