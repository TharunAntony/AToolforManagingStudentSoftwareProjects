package com.example.atoolformanagingstudentsoftwareprojects.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String Title;
    private String Description;
    @ManyToOne
    @JoinColumn(name = "convenor_id")
    private User Convenor;
    private LocalDateTime Deadline;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public User getConvenor() {

        return Convenor;
    }

    public void setConvenor(User convenor) {
        Convenor = convenor;
    }

    public LocalDateTime getDeadline() {
        return Deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        Deadline = deadline;
    }
}
