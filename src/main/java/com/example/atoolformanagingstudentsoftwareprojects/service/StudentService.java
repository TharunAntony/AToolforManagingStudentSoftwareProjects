package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.GroupsRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.StudentDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class StudentService {

    private final GroupsRepository groupsRepository;
    private final StudentDetailsRepository studentDetailsRepository;
    private final ProjectRepository projectRepository;

    public StudentService(StudentDetailsRepository studentDetailsRepository, GroupsRepository groupsRepository, ProjectRepository projectRepository) {
        this.studentDetailsRepository = studentDetailsRepository;
        this.groupsRepository = groupsRepository;
        this.projectRepository = projectRepository;
    }

    public List<Project> getStudentProjects(User user) {
        StudentDetails studentDetails = studentDetailsRepository.findByStudent(user);

        if (studentDetails == null) {
            return Collections.emptyList();
        }

        return projectRepository.findBystudents(studentDetails);
    }

    public Groups getGroupById(Long groupId) {
        return groupsRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    public List<Groups> getStudentGroups(Long studentId) {
        StudentDetails studentDetails = studentDetailsRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Project> projects = projectRepository.findBystudents(studentDetails);
        List<Groups> studentGroups = new ArrayList<>();

        for (Project project : projects) {
            boolean submitted = true;
            for (Groups group : project.getGroups()) {
                List<GroupMember> students = group.getGroupMembers();
                for(GroupMember student : students){
                    if(student.getStudent().getId() == studentId){
                        studentGroups.add(group);
                        break;
                    }
                }
            }

        }
        return studentGroups;
    }

}
