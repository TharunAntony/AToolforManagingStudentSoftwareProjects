package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

//Stores peer reviews
@Entity
public class PeerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    //Student giving the review
    @ManyToOne
    private User reviewer;

    //Student receiving the review
    @ManyToOne
    private User reviewee;

    //Peer review details
    private int score;
    @Column(length = 2500)
    private String comment;

    //Which group they part of
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Groups group;

    //For which project
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    //Empty Constructor
    public PeerReview() {}

    //Getters and setters
    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public User getReviewee() {
        return reviewee;
    }

    public void setReviewee(User reviewee) {
        this.reviewee = reviewee;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Groups getGroup() {
        return group;
    }

    public void setGroup(Groups group) {
        this.group = group;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
