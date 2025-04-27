package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.dto.StudentPreferencesForm;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.repository.StudentDetailsRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.StudentPreferencesRepository;
import com.example.atoolformanagingstudentsoftwareprojects.model.StudentDetails;
import com.example.atoolformanagingstudentsoftwareprojects.model.StudentPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentPreferencesService {

    private final StudentPreferencesRepository studentPreferencesRepository;
    private final StudentDetailsRepository studentDetailsRepository;

    @Autowired
    public StudentPreferencesService(StudentPreferencesRepository studentPreferenceRepository, StudentDetailsRepository studentDetailsRepository) {
        this.studentPreferencesRepository = studentPreferenceRepository;
        this.studentDetailsRepository = studentDetailsRepository;
    }


    public StudentPreferences getPreferencesByStudent(User user) {
        StudentDetails studentDetails = studentDetailsRepository.findByStudent(user);
        if (studentDetails == null) return null;
        return studentPreferencesRepository.findByStudentDetails(studentDetails);
    }

    @Transactional
    public void savePreferences(StudentPreferences newPreferences, User user) {
        StudentDetails studentDetails = studentDetailsRepository.findByStudent(user);

        if (studentDetails == null) {
            throw new IllegalArgumentException("StudentDetails not found for user.");
        }

        StudentPreferences existingPreferences = studentPreferencesRepository.findByStudentDetails(studentDetails);

        if (existingPreferences != null) {
            // Update existing preferences
            existingPreferences.setWorkingStyle(newPreferences.getWorkingStyle());
            existingPreferences.setWorkingHours(newPreferences.getWorkingHours());
            existingPreferences.setTechnicalSkill(newPreferences.getTechnicalSkill());
            existingPreferences.setCommunicationSkill(newPreferences.getCommunicationSkill());
            existingPreferences.setLeadershipPreference(newPreferences.getLeadershipPreference());
            existingPreferences.setDeadlineApproach(newPreferences.getDeadlineApproach());
            existingPreferences.setTeamworkExperience(newPreferences.getTeamworkExperience());
            existingPreferences.setPriorExperience(newPreferences.getPriorExperience());
            studentPreferencesRepository.save(existingPreferences);
        } else {
            // No existing preferences, so create new
            newPreferences.setStudentDetails(studentDetails);
            studentPreferencesRepository.save(newPreferences);
        }
    }




    public StudentPreferencesForm convertToForm(StudentPreferences preferences) {
        StudentPreferencesForm form = new StudentPreferencesForm();
        form.setWorkingStyle(preferences.getWorkingStyle());
        form.setWorkingHours(preferences.getWorkingHours());
        form.setTechnicalSkill(preferences.getTechnicalSkill());
        form.setCommunicationSkill(preferences.getCommunicationSkill());
        form.setLeadershipPreference(preferences.getLeadershipPreference());
        form.setDeadlineApproach(preferences.getDeadlineApproach());
        form.setTeamworkExperience(preferences.getTeamworkExperience());
        form.setPriorExperience(preferences.getPriorExperience());
        return form;
    }

}
