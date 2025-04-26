package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.dto.RegistrationForm;
import com.example.atoolformanagingstudentsoftwareprojects.repository.UserRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/register")
    public String Register(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute RegistrationForm form, Model model) {
        if(userRepository.findByUsername(form.getUsername()) != null) {
            model.addAttribute("usernameError", "Username is already taken");
            return "register";
        }

        registrationService.registerUser(form);
        return "redirect:/login";

    }
}
