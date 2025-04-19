package com.example.atoolformanagingstudentsoftwareprojects.controller.login;

import com.example.atoolformanagingstudentsoftwareprojects.AToolforManagingStudentSoftwareProjectsApplication;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Objects;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }



}

