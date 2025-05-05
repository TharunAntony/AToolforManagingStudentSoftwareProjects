package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Table to store details about a project
@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //The convenor for the project
    @ManyToOne
    @JoinColumn(name = "convenor_id")
    private ConvenorDetails convenor;

    //List of groups in a project
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Groups> groups;

    @ManyToMany
    @JoinTable(
            name = "project_students",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<StudentDetails> students = new ArrayList<>();


    //List of all the submissions made for the project
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions = new ArrayList<>();

    //Details about the project
    private String Title;
    private String Description;
    private LocalDateTime Deadline;
    private Integer groupCapacity;
    @Transient
    private int submissionCount;

    //Empty Constructor
    public Project(){
    }

    //Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public ConvenorDetails getConvenor() {

        return convenor;
    }

    public void setConvenor(ConvenorDetails convenor) {
        this.convenor = convenor;
    }

    public LocalDateTime getDeadline() {
        return Deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        Deadline = deadline;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }

    public int getGroupCapacity() {
        return groupCapacity;
    }

    public void setGroupCapacity(Integer groupCapacity) {
        this.groupCapacity = groupCapacity;
    }

    public List<Groups> getGroups() {
        return groups;
    }

    public void setGroups(List<Groups> groups) {
        this.groups = groups;
    }

    public List<StudentDetails> getStudents() {
        return students;
    }

    public void setStudents(List<StudentDetails> students) {
        this.students = students;
    }

    public int getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(int submissionCount) {
        this.submissionCount = submissionCount;
    }
}
