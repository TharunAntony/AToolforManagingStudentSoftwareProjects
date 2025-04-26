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
    private ConvenorDetails Convenor;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Groups> groups;


    //List of all the submissions made for the project
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions = new ArrayList<>();

    //Details about the project
    private String Title;
    private String Description;
    private LocalDateTime Deadline;

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

        return Convenor;
    }

    public void setConvenor(ConvenorDetails convenor) {
        Convenor = convenor;
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
}
