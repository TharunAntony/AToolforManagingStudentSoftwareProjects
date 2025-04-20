package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

@Entity
public class convenorDetails {
    @Id
    private int Id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "convenor_Id")
    private User convenor;


}
