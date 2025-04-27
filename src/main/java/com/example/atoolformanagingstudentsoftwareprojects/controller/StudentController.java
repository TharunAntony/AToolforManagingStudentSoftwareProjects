package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.CurrentUser;
import com.example.atoolformanagingstudentsoftwareprojects.service.ProjectService;
import com.example.atoolformanagingstudentsoftwareprojects.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal CurrentUser currentUser) {
        User user = currentUser.getUser();
        List<Project> projects = studentService.getStudentProjects(user);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("projects", projects);
        return "student/home";
    }


}
