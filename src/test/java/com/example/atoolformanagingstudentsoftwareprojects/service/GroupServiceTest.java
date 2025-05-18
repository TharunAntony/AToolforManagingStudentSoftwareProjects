package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.model.StudentDetails;
import com.example.atoolformanagingstudentsoftwareprojects.model.StudentPreferences;
import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
public class GroupServiceTest {

    @Autowired
    GroupsRepository groupsRepository;
    @Autowired
    GroupMemberRepository groupMemberRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StudentDetailsRepository studentDetailsRepository;
    @Autowired
    StudentPreferencesRepository studentPreferencesRepository;
    @Autowired
    GroupService groupService;
    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    ProjectService projectService;


    @Test
    void testCompatibilityScore() {
        //Setup preferences
        StudentPreferences prefs1 = new StudentPreferences();
        prefs1.setWorkingStyle(1);
        prefs1.setWorkingHours(2);
        prefs1.setTechnicalSkill(3);
        prefs1.setCommunicationSkill(3);
        prefs1.setLeadershipPreference(1);
        prefs1.setDeadlineApproach(1);
        prefs1.setTeamworkExperience(1);
        prefs1.setPriorExperience(false);

        StudentPreferences prefs2 = new StudentPreferences();
        prefs2.setWorkingStyle(1);
        prefs2.setWorkingHours(2);
        prefs2.setTechnicalSkill(3);
        prefs2.setCommunicationSkill(3);
        prefs2.setLeadershipPreference(2);
        prefs2.setDeadlineApproach(1);
        prefs2.setTeamworkExperience(1);
        prefs2.setPriorExperience(true);

        //Set up student details and user
        StudentDetails sd1 = new StudentDetails();
        sd1.setStudentPreferences(prefs1);
        User user1 = new User();
        user1.setStudentDetails(sd1);

        StudentDetails sd2 = new StudentDetails();
        sd2.setStudentPreferences(prefs2);
        User user2 = new User();
        user2.setStudentDetails(sd2);

        //Calculate the compatability score
        double score = groupService.calculateCompatibility(user1, user2);

        assertEquals(0.88, score, 0.01); // Allow a small delta

        System.out.println("Compatibility score test passed successfully with a score of" + score);
    }

    @Test
    void testEvaluateGroupCompatibility() {

        //Set up users and preferences
        User user1 = new User();
        user1.setUsername("test1");
        user1.setPassword("test1");
        user1.setFirstName("test1");
        user1.setLastName("test1");
        user1.setEmail("test1@example.com");
        user1.setRole(Role.STUDENT);
        User savedUser1 = userRepository.save(user1);

        StudentDetails details1 = new StudentDetails();
        details1.setStudent(savedUser1);
        StudentDetails savedDetails1 = studentDetailsRepository.save(details1);

        StudentPreferences prefs1 = new StudentPreferences();
        prefs1.setWorkingStyle(1);
        prefs1.setWorkingHours(2);
        prefs1.setTechnicalSkill(3);
        prefs1.setCommunicationSkill(1);
        prefs1.setLeadershipPreference(1);
        prefs1.setDeadlineApproach(1);
        prefs1.setTeamworkExperience(3);
        prefs1.setPriorExperience(true);
        prefs1.setStudentDetails(savedDetails1);
        studentPreferencesRepository.save(prefs1);

        savedDetails1.setStudentPreferences(prefs1);
        studentDetailsRepository.save(savedDetails1);
        savedUser1.setStudentDetails(savedDetails1);
        userRepository.save(savedUser1);


        User user2 = new User();
        user2.setUsername("test2");
        user2.setPassword("test2");
        user2.setFirstName("tes2");
        user2.setLastName("test2");
        user2.setEmail("test2@example.com");
        user2.setRole(Role.STUDENT);
        User savedUser2 = userRepository.save(user2);

        StudentDetails details2 = new StudentDetails();
        details2.setStudent(savedUser2);
        StudentDetails savedDetails2 = studentDetailsRepository.save(details2);

        StudentPreferences prefs2 = new StudentPreferences();
        prefs2.setWorkingStyle(1);
        prefs2.setWorkingHours(2);
        prefs2.setTechnicalSkill(2);
        prefs2.setCommunicationSkill(3);
        prefs2.setLeadershipPreference(2);
        prefs2.setDeadlineApproach(1);
        prefs2.setTeamworkExperience(4);
        prefs2.setPriorExperience(false);
        prefs2.setStudentDetails(savedDetails2);
        studentPreferencesRepository.save(prefs2);

        savedDetails2.setStudentPreferences(prefs2);
        studentDetailsRepository.save(savedDetails2);
        savedUser2.setStudentDetails(savedDetails2);
        userRepository.save(savedUser2);


        User user3 = new User();
        user3.setUsername("test3");
        user3.setPassword("test3");
        user3.setFirstName("test3");
        user3.setLastName("test3");
        user3.setEmail("test3@example.com");
        user3.setRole(Role.STUDENT);
        User savedUser3 = userRepository.save(user3);

        StudentDetails details3 = new StudentDetails();
        details3.setStudent(savedUser3);
        StudentDetails savedDetails3 = studentDetailsRepository.save(details3);

        StudentPreferences prefs3 = new StudentPreferences();
        prefs3.setWorkingStyle(2);
        prefs3.setWorkingHours(3);
        prefs3.setTechnicalSkill(4);
        prefs3.setCommunicationSkill(2);
        prefs3.setLeadershipPreference(2);
        prefs3.setDeadlineApproach(2);
        prefs3.setTeamworkExperience(2);
        prefs3.setPriorExperience(true);
        prefs3.setStudentDetails(savedDetails3);
        studentPreferencesRepository.save(prefs3);

        savedDetails3.setStudentPreferences(prefs3);
        studentDetailsRepository.save(savedDetails3);
        savedUser3.setStudentDetails(savedDetails3);
        userRepository.save(savedUser3);

        // Create group and members
        Groups group = new Groups();
        Groups savedGroup = groupsRepository.save(group);

        GroupMember m1 = new GroupMember();
        m1.setStudent(savedDetails1);
        m1.setGroup(savedGroup);

        GroupMember m2 = new GroupMember();
        m2.setStudent(savedDetails2);
        m2.setGroup(savedGroup);

        GroupMember m3 = new GroupMember();
        m3.setStudent(savedDetails3);
        m3.setGroup(savedGroup);

        groupMemberRepository.save(m1);
        groupMemberRepository.save(m2);
        groupMemberRepository.save(m3);

        List<GroupMember> groupMembers = new ArrayList<>();
        groupMembers.add(m1);
        groupMembers.add(m2);
        groupMembers.add(m3);
        group.setGroupMembers(groupMembers);
        groupsRepository.save(savedGroup);

        // Calculate and test compatibility
        double totalCompat = groupService.calculateCompatibility(savedUser1, savedUser2)
                + groupService.calculateCompatibility(savedUser1, savedUser3)
                + groupService.calculateCompatibility(savedUser2, savedUser3);

        double averageGroupScore = totalCompat / 3;

        Map<Long, Double> compatibilityMap = groupService.evaluateGroupCompatibility(List.of(savedGroup));
        double calculatedScore = compatibilityMap.get(savedGroup.getId());
        assertEquals(averageGroupScore, calculatedScore, 0.0001);
        System.out.println("Group compatibility test passed with score: " + calculatedScore);
    }

    @Test
    public void testRandomGroupCompatibility() {
        Long projectId = 7L;

        //Get the project
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        //Get students assigned to the project
        List<User> students = projectService.getStudentsAssignedToProject(project);

        int maxPerGroup = project.getGroupCapacity();
        int runs = 10;
        List<Double> runCompatScores = new ArrayList<>();

        for (int r = 0; r < runs; r++) {
            //Shuffle the students randomly
            Collections.shuffle(students);

            List<Groups> randomGroups = new ArrayList<>();

            for (int i = 0; i < students.size(); i += maxPerGroup) {
                //Save group before assigning members
                Groups group = new Groups();
                group.setProject(project);
                group.setGroupName("Random Test Group " + r + "-" + (i / maxPerGroup + 1));
                group = groupsRepository.save(group);

                // Now add members
                for (int j = i; j < i + maxPerGroup && j < students.size(); j++) {
                    GroupMember member = new GroupMember();
                    member.setGroup(group); // Use the saved group with ID
                    member.setStudent(students.get(j).getStudentDetails());
                    groupMemberRepository.save(member);
                }

                randomGroups.add(group);
            }

            //Evaluate compatibility score for the groups
            Map<Long, Double> result = groupService.evaluateGroupCompatibility(randomGroups);

            double totalScore = 0;
            int groupCount = 0;

            for (Double score : result.values()) {
                totalScore += score;
                groupCount++;
            }

            if (groupCount > 0) {
                double avg = totalScore / groupCount;
                runCompatScores.add(avg);
            }
        }

        //Print results
        System.out.println("Average compatibility scores of random allocation for project ID 7:");
        for (int i = 0; i < runCompatScores.size(); i++) {
            System.out.println("Run " + (i + 1) + ": " + runCompatScores.get(i));
        }

        assertTrue(true); // Just for manual inspection
    }



}
