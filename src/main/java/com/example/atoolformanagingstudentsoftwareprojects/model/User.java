package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

@Entity
public class User {

    @Id
    private int id;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    private String firstName;
    private String lastName;
    private String email;

    public User(int id, String username, String password, String firstName) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
    }


    public User() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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


}
