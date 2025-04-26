package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

//Table to store the students marks
@Entity
public class Mark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Which students marks it is
    @ManyToOne
    @JoinColumn(name = "student_id")
    private StudentDetails student;

    //For which project
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    //Raw project mark before peer moderation
    private Double originalMark;

    //Adjusted mark after peer moderation
    private Double adjustedMark;

    //Empty Constructor
    public Mark() {
    }

    //Getters and setters
    public Long getId() {
        return id;
    }

    public StudentDetails getStudent() {
        return student;
    }

    public Project getProject() {
        return project;
    }

    public Double getOriginalMark() {
        return originalMark;
    }

    public Double getAdjustedMark() {
        return adjustedMark;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStudent(StudentDetails student) {
        this.student = student;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setOriginalMark(Double originalMark) {
        this.originalMark = originalMark;
    }

    public void setAdjustedMark(Double adjustedMark) {
        this.adjustedMark = adjustedMark;
    }
}
