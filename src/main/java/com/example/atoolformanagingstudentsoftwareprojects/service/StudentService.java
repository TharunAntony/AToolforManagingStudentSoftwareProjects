package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.StudentDetails;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.repository.StudentDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class StudentService {

    private final StudentDetailsRepository studentDetailsRepository;

    public StudentService(StudentDetailsRepository studentDetailsRepository) {
        this.studentDetailsRepository = studentDetailsRepository;
    }

    public List<Project> getStudentProjects(User user) {
        StudentDetails studentDetails = studentDetailsRepository.findByStudent(user);

        if (studentDetails == null) {
            return Collections.emptyList();
        }

        return studentDetails.getProjects();
    }

}
