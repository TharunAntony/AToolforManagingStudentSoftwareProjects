package com.example.atoolformanagingstudentsoftwareprojects;

import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;


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
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        usersave();
    }

    public void usersave(){
        User u1 = new User();
        u1.setUsername("John");
        u1.setPassword(passwordEncoder.encode("1234"));
        userRepository.save(u1);
    }


}

