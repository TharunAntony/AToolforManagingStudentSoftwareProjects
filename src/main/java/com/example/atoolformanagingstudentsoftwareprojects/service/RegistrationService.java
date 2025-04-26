package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.dto.RegistrationForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.*;
import com.example.atoolformanagingstudentsoftwareprojects.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class RegistrationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentDetailsRepository studentDetailsRepository;

    @Autowired
    private ConvenorDetailsRepository convenorDetailsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Saves a new user into the database
    public void registerUser(RegistrationForm form) {
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());

        User savedUser = userRepository.save(user);

        //Saves the users detail into corresponding table depending on whether they are student or convenor
        if (form.getRole() == Role.STUDENT) {
            StudentDetails studentDetails = new StudentDetails();
            studentDetails.setStudent(savedUser);
            studentDetailsRepository.save(studentDetails);
        } else if (form.getRole() == Role.CONVENOR) {
            ConvenorDetails convenorDetails = new ConvenorDetails();
            convenorDetails.setConvenor(savedUser);
            convenorDetailsRepository.save(convenorDetails);
        }
    }
}
