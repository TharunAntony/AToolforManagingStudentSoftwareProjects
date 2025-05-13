package com.example.atoolformanagingstudentsoftwareprojects.dto;

import com.example.atoolformanagingstudentsoftwareprojects.model.Mark;

import java.util.List;

public class MarkListForm {

    private List<Mark> marks;

    public List<Mark> getMarks() {
        return marks;
    }

    public void setMarks(List<Mark> marks) {
        this.marks = marks;
    }
}
