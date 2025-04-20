package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

@Entity
public class studentDetails {
    @Id
    private int Id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "student_Id")
    private User student;


}
