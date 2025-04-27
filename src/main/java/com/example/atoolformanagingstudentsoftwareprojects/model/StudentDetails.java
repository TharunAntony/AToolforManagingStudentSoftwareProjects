package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

//Table to store student specific details
@Entity
public class StudentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Link to the user
    @OneToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    //List of projects a student has
    @ManyToMany(mappedBy = "students")
    private List<Project> projects = new ArrayList<>();


    //List of groups they are part of
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> groupMemberships = new ArrayList<>();

    //Marks received
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mark> marks = new ArrayList<>();

    //Empty Constructor
    public StudentDetails() {}

    public StudentDetails(User student) {
        this.student = student;
    }

    //Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public List<GroupMember> getGroupMemberships() {
        return groupMemberships;
    }

    public void setGroupMemberships(List<GroupMember> groupMemberships) {
        this.groupMemberships = groupMemberships;
    }

    public List<Mark> getMarks() {
        return marks;
    }

    public void setMarks(List<Mark> marks) {
        this.marks = marks;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }
}
