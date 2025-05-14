package com.example.atoolformanagingstudentsoftwareprojects;

import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.StudentDetailsRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.StudentPreferencesRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.UserRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Random;


@SpringBootApplication
public class AToolforManagingStudentSoftwareProjectsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AToolforManagingStudentSoftwareProjectsApplication.class, args);
    }


}

@Component
class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentDetailsRepository studentDetailsRepository;

    @Autowired
    private StudentPreferencesRepository studentPreferencesRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GroupService groupService;



    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        //testDataLoader();
    }

    public void testDataLoader(){

        //Create 30 test students
        for (int i = 1; i <= 30; i++) {
            User user = new User();
            user.setUsername("student" + i);
            user.setFirstName("Student" + i);
            user.setLastName("Test");
            user.setEmail("student" + i + "@example.com");
            user.setPassword(passwordEncoder.encode("password")); // default password
            user.setRole(Role.STUDENT);

            user = userRepository.save(user);

            StudentDetails details = new StudentDetails();
            details.setStudent(user);

            details = studentDetailsRepository.save(details);

            // Random preferences
            StudentPreferences preferences = new StudentPreferences();
            preferences.setStudentDetails(details);
            preferences.setWorkingStyle(randomInt(1, 3));
            preferences.setWorkingHours(randomInt(1, 4));
            preferences.setTechnicalSkill(randomInt(1, 5));
            preferences.setCommunicationSkill(randomInt(1, 5));
            preferences.setLeadershipPreference(randomInt(1, 3));
            preferences.setDeadlineApproach(randomInt(1, 2));
            preferences.setTeamworkExperience(randomInt(1, 5));
            preferences.setPriorExperience(new Random().nextBoolean());

            studentPreferencesRepository.save(preferences);
        }

        /* //Creates 20 test students without preferences (recommended to uncomment after using the testing the system with the students who do have preference)
        for (int i = 31; i <= 50; i++) {
            User user = new User();
            user.setUsername("student" + i);
            user.setFirstName("Student" + i);
            user.setLastName("Test");
            user.setEmail("student" + i + "@example.com");
            user.setPassword(passwordEncoder.encode("password")); // default password
            user.setRole(Role.STUDENT);

            user = userRepository.save(user);

            StudentDetails details = new StudentDetails();
            details.setStudent(user);

            details = studentDetailsRepository.save(details);
        }*/

        System.out.println("Test students generated successfully.");
    }

    private int randomInt(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }


}


