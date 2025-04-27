package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

//Table to store convenor specific details
@Entity
public class ConvenorDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    //Link to the user
    @OneToOne
    @JoinColumn(name ="convenor_id", nullable = false)
    private User convenor;

    // Projects managed by this convenor
    @OneToMany(mappedBy = "convenor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Project> projects = new ArrayList<>();

    //Empty Constructor
    public ConvenorDetails() {
    }

    //Getter and setters
    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public User getConvenor() {
        return convenor;
    }

    public void setConvenor(User convenor) {
        this.convenor = convenor;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
