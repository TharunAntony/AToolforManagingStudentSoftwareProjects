package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.dto.ProjectForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupsRepository groupsRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private PeerReviewRepository peerReviewRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private MarkRepository markRepository;


    //Creates and saves the details of a new project
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

    // Get the students available to be added to the project (students who not already in it)
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

    @Transactional
    public void deleteGroupsForProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        List<Groups> groups = new ArrayList<>(project.getGroups()); // Make a copy to avoid concurrent modification

        for (Groups group : groups) {
            // Delete peer reviews
            peerReviewRepository.deleteByGroup(group);

            // Delete student marks
            List<GroupMember> members = groupMemberRepository.findByGroup(group);
            for (GroupMember member : members) {
                StudentDetails student = member.getStudent();
                Mark mark = markRepository.findByStudentAndProject(student, project);
                if (mark != null) {
                    markRepository.delete(mark);
                }
            }

            // Delete group members
            groupMemberRepository.deleteAll(members);

            // Delete submission
            Submission submission = submissionRepository.findByGroup(group);
            if (submission != null) {
                submission.setGroup(null);
                submissionRepository.delete(submission);
            }

            // Break the link between project and group
            group.setProject(null);
            project.getGroups().remove(group); // remove from list to prevent JPA from thinking it still exists
        }

        groupsRepository.deleteAll(groups);
    }

    //Deletes a project and all associated data safely
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        //Delete all groups and related data for the project
        deleteGroupsForProject(projectId);

        //Break student links
        for (StudentDetails student : project.getStudents()) {
            student.getProjects().remove(project);
        }
        project.getStudents().clear();

        // Remove link to convenor
        ConvenorDetails convenor = project.getConvenor();
        if (convenor != null) {
            convenor.getProjects().remove(project);
        }

        // Save project after breaking links
        projectRepository.save(project);

        //Delete the actual project
        projectRepository.delete(project);
    }


}
