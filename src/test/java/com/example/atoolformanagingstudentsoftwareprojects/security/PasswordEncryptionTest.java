package com.example.atoolformanagingstudentsoftwareprojects.security;

import com.example.atoolformanagingstudentsoftwareprojects.dto.RegistrationForm;
import com.example.atoolformanagingstudentsoftwareprojects.repository.UserRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.RegistrationService;
import com.example.atoolformanagingstudentsoftwareprojects.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
public class PasswordEncryptionTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RegistrationService registrationService;

    @Test
    void testPasswordIsHashedCorrectly() {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "testPassword123";

        RegistrationForm registrationForm = new RegistrationForm();
        registrationForm.setUsername("PasswordTest");
        registrationForm.setPassword(rawPassword);
        registrationForm.setRole(Role.STUDENT);
        registrationForm.setEmail("passwordTest@gmail.com");
        registrationForm.setFirstName("PasswordTest");

        registrationService.registerUser(registrationForm);

        String storedPassword = userRepository.findByUsername("PasswordTest").getPassword();


        // Check that the hash is not equal to the raw password
        assertNotEquals(rawPassword, storedPassword, "Hashed password should not match raw password");

        // Check that the raw password matches the hashed one
        assertTrue(encoder.matches(rawPassword, storedPassword), "Encoded password should match raw password");

        System.out.println("Password Test passed successfully");

    }
}
