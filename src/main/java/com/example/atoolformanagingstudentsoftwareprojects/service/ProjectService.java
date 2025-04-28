package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.dto.ProjectForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.StudentDetailsRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentDetailsRepository studentDetailsRepository;

    public void saveProject(ProjectForm form,@AuthenticationPrincipal CurrentUser currentUser) {
        Project project = new Project();
        project.setTitle(form.getTitle());
        project.setDescription(form.getDescription());
        project.setGroupCapacity(form.getGroupCapacity());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime deadline = LocalDateTime.parse(form.getDeadline(), formatter);
        project.setDeadline(deadline);

        ConvenorDetails convenorDetails = currentUser.getUser().getConvenorDetails();
        project.setConvenor(convenorDetails);

        projectRepository.save(project);
    }

    public List<Project> getProjects(User user) {
        return projectRepository.findByConvenor(user.getConvenorDetails());
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + id));
    }

    // Get the list of students assigned to the project
    public List<User> getStudentsAssignedToProject(Project project) {
        List<User> users = new java.util.ArrayList<>();
        for (StudentDetails details : project.getStudents()) {
            users.add(details.getStudent());
        }
        return users;
    }

    // Get the students available to be added to the project (students not already in it)
    public List<User> getAvailableStudentsForProject(Project project) {
        List<User> allStudents = userRepository.findByRole(Role.STUDENT);
        List<User> assignedStudents = getStudentsAssignedToProject(project);

        List<User> availableStudents = new java.util.ArrayList<>();
        for (User student : allStudents) {
            if (!assignedStudents.contains(student)) {
                availableStudents.add(student);
            }
        }
        return availableStudents;
    }

    public void updateProject(Project updatedProject) {
        //Fetch the existing project from the database using its ID
        Project existingProject = projectRepository.findById(updatedProject.getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + updatedProject.getId()));

        //Update fields with the new data
        existingProject.setTitle(updatedProject.getTitle());
        existingProject.setDescription(updatedProject.getDescription());
        existingProject.setDeadline(updatedProject.getDeadline());
        existingProject.setGroupCapacity(updatedProject.getGroupCapacity());
        existingProject.setConvenor(updatedProject.getConvenor());
        existingProject.setStudents(updatedProject.getStudents()); // Update student list if needed

        //Save the updated project
        projectRepository.save(existingProject);
    }


    public ProjectForm convertToForm(Project project) {
        ProjectForm form = new ProjectForm();
        form.setTitle(project.getTitle());
        form.setDescription(project.getDescription());
        form.setDeadline(project.getDeadline().toString());
        form.setGroupCapacity(project.getGroupCapacity());
        return form;
    }

    //Gets groups for a project
    public List<Groups> getGroupsForProject(Project project) {
        return project.getGroups();
    }

    public List<Project> getProjectsForStudent(User user) {
        return user.getStudentDetails().getProjects();
    }




}
