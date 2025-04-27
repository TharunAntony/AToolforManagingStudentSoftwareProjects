package com.example.atoolformanagingstudentsoftwareprojects.dto;


// Form object that holds the student's answers to the preferences survey
public class StudentPreferencesForm {

    private Integer workingStyle;
    private Integer workingHours;
    private Integer technicalSkill;
    private Integer communicationSkill;
    private Integer leadershipPreference;
    private Integer deadlineApproach;
    private Integer teamworkExperience;
    private Boolean priorExperience;

    //Empty Constructor
    public StudentPreferencesForm() {}

    //Getters and Setters
    public Integer getWorkingStyle() {
        return workingStyle;
    }

    public void setWorkingStyle(Integer workingStyle) {
        this.workingStyle = workingStyle;
    }

    public Integer getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(Integer workingHours) {
        this.workingHours = workingHours;
    }

    public Integer getTechnicalSkill() {
        return technicalSkill;
    }

    public void setTechnicalSkill(Integer technicalSkill) {
        this.technicalSkill = technicalSkill;
    }

    public Integer getCommunicationSkill() {
        return communicationSkill;
    }

    public void setCommunicationSkill(Integer communicationSkills) {
        this.communicationSkill = communicationSkills;
    }

    public Integer getLeadershipPreference() {
        return leadershipPreference;
    }

    public void setLeadershipPreference(Integer leadershipPreference) {
        this.leadershipPreference = leadershipPreference;
    }

    public Integer getDeadlineApproach() {
        return deadlineApproach;
    }

    public void setDeadlineApproach(Integer deadlineApproach) {
        this.deadlineApproach = deadlineApproach;
    }

    public Integer getTeamworkExperience() {
        return teamworkExperience;
    }

    public void setTeamworkExperience(Integer teamworkExperience) {
        this.teamworkExperience = teamworkExperience;
    }

    public Boolean getPriorExperience() {
        return priorExperience;
    }

    public void setPriorExperience(Boolean priorExperience) {
        this.priorExperience = priorExperience;
    }
}
