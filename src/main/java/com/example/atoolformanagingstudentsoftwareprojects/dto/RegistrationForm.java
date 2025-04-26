package com.example.atoolformanagingstudentsoftwareprojects.dto;

import com.example.atoolformanagingstudentsoftwareprojects.model.Role;

//Data transfer object to register a new user
public class RegistrationForm {

    //Details needed to register new user
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;

    //Getters and Setters
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
