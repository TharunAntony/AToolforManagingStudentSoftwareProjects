package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

@Entity
public class Groups {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String groupName;
    @ManyToOne
    private Project projectID;
}
