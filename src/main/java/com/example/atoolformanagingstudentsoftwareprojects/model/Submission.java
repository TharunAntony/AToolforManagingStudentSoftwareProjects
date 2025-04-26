package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.Entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

//Table to store submission details
@Entity
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Link to project
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    //Link to group that made the submission
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Groups group;


    //Details about submission
    private LocalDateTime submittedAt;
    private boolean submitted;
    private boolean late;
    private Double finalGroupMark;

    //Empty Constructor
    public Submission(){
    }

    //Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Groups getGroup() {
        return group;
    }

    public void setGroup(Groups group) {
        this.group = group;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public boolean isLate() {
        return late;
    }

    public void setLate(boolean late) {
        this.late = late;
    }

    public Double getFinalGroupMark() {
        return finalGroupMark;
    }

    public void setFinalGroupMark(Double finalGroupMark) {
        this.finalGroupMark = finalGroupMark;
    }
}
