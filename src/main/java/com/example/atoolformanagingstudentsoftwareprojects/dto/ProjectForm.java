package com.example.atoolformanagingstudentsoftwareprojects.dto;

//Used to create a new project
public class ProjectForm {
    private String title;
    private String description;
    private String deadline;
    private Integer groupCapacity;

    //Getters and setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public Integer getGroupCapacity() {
        return groupCapacity;
    }

    public void setGroupCapacity(Integer groupCapacity) {
        this.groupCapacity = groupCapacity;
    }
}
