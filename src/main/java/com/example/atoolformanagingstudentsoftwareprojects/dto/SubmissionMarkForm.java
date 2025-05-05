package com.example.atoolformanagingstudentsoftwareprojects.dto;

//Used to save a groups mark for a submission
public class SubmissionMarkForm {

    private Long submissionId;
    private Double mark;

    public SubmissionMarkForm(){

    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Double getMark() {
        return mark;
    }

    public void setMark(Double mark) {
        this.mark = mark;
    }
}
