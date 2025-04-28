package com.example.atoolformanagingstudentsoftwareprojects.controller;

import com.example.atoolformanagingstudentsoftwareprojects.dto.StudentPreferencesForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.StudentDetails;
import com.example.atoolformanagingstudentsoftwareprojects.model.StudentPreferences;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.repository.StudentDetailsRepository;
import com.example.atoolformanagingstudentsoftwareprojects.service.CurrentUser;
import com.example.atoolformanagingstudentsoftwareprojects.service.ProjectService;
import com.example.atoolformanagingstudentsoftwareprojects.service.StudentPreferencesService;
import com.example.atoolformanagingstudentsoftwareprojects.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentPreferencesService studentPreferencesService;

    @Autowired
    private StudentDetailsRepository studentDetailsRepository;

    @Autowired
    private ProjectService projectService;

    @GetMapping("/home")
    public String home(Model model, @AuthenticationPrincipal CurrentUser currentUser) {
        User user = currentUser.getUser();
        List<Project> projects = studentService.getStudentProjects(user);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("projects", projects);
        return "student/home";
    }

    @GetMapping("/preferences")
    public String viewPreferences(Model model, @AuthenticationPrincipal CurrentUser currentUser) {
        User user = currentUser.getUser();
        StudentPreferences preferences = studentPreferencesService.getPreferencesByStudent(user);

        model.addAttribute("preferences", preferences);
        model.addAttribute("username", user.getUsername());
        return "student/viewPreferences";
    }

    @GetMapping("/preferences/manage")
    public String showEditPreferencesForm(Model model, @AuthenticationPrincipal CurrentUser currentUser) {
        User user = currentUser.getUser();
        StudentPreferences preferences = studentPreferencesService.getPreferencesByStudent(user);

        StudentPreferencesForm form;
        if (preferences != null) {
            form = studentPreferencesService.convertToForm(preferences);
        } else {
            form = new StudentPreferencesForm();
        }

        model.addAttribute("preferencesForm", form);
        model.addAttribute("username", user.getUsername());
        return "student/managePreferences";
    }

    @PostMapping("/preferences/manage")
    public String updatePreferences(@ModelAttribute("preferencesForm") StudentPreferencesForm form, @AuthenticationPrincipal CurrentUser currentUser) {
        StudentDetails studentDetails = studentDetailsRepository.findByStudent(currentUser.getUser());

        StudentPreferences preferences = new StudentPreferences();
        preferences.setStudentDetails(studentDetails);
        preferences.setWorkingStyle(form.getWorkingStyle());
        preferences.setWorkingHours(form.getWorkingHours());
        preferences.setTechnicalSkill(form.getTechnicalSkill());
        preferences.setCommunicationSkill(form.getCommunicationSkill());
        preferences.setLeadershipPreference(form.getLeadershipPreference());
        preferences.setDeadlineApproach(form.getDeadlineApproach());
        preferences.setTeamworkExperience(form.getTeamworkExperience());
        preferences.setPriorExperience(form.getPriorExperience());

        studentPreferencesService.savePreferences(preferences, currentUser.getUser());
        return "redirect:/student/preferences";
    }

    @GetMapping("/student/projects")
    public String viewStudentProjects(@AuthenticationPrincipal CurrentUser currentUser, Model model) {
        User user = currentUser.getUser();
        List<Project> studentProjects = projectService.getProjectsForStudent(user);

        model.addAttribute("projects", studentProjects);
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("username", user.getUsername());

        return "student/viewProjects";
    }




}
