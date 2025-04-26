package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

//Table to store all users
@Entity
public class User {

    //Primary key id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Username and password for user
    private String username;
    private String password;

    //Details of user
    @Enumerated(EnumType.STRING)
    private Role role;
    private String firstName;
    private String lastName;
    private String email;

    //Link to studentDetails
    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL)
    private StudentDetails studentDetails;

    //Link to convenorDetails
    @OneToOne(mappedBy = "convenor", cascade = CascadeType.ALL)
    private ConvenorDetails convenorDetails;

    //Empty Constructor
    public User() {
    }


    //Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public StudentDetails getStudentDetails() {
        return studentDetails;
    }

    public void setStudentDetails(StudentDetails studentDetails) {
        this.studentDetails = studentDetails;
    }

    public ConvenorDetails getConvenorDetails() {
        return convenorDetails;
    }

    public void setConvenorDetails(ConvenorDetails convenorDetails) {
        this.convenorDetails = convenorDetails;
    }
}
