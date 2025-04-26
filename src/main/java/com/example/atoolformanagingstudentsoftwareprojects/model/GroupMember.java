package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

//Table to store details about group members
@Entity
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    //What group a student is in
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Groups group;

    //Student details
    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    //Empty Constructor
    public GroupMember() {}

    //Getters and setters
    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public Groups getGroup() {
        return group;
    }

    public void setGroup(Groups group) {
        this.group = group;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }
}
