package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ConvenorController {

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/convenor/home")
    public String convenorHome(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        List<Project> projects = projectRepository.findByConvenor(user.getConvenorDetails());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("projects", projects);
        return "convenor/home";
    }

}
