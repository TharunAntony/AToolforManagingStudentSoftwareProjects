package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.model.Groups;
import com.example.atoolformanagingstudentsoftwareprojects.model.Submission;
import com.example.atoolformanagingstudentsoftwareprojects.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SubmissionService {

    @Autowired
    private SubmissionRepository submissionRepository;

    public void saveSubmission(Submission submission) {
        submissionRepository.save(submission);
    }

    public Submission findByGroup(Groups group) {
        return submissionRepository.findByGroup(group);
    }

    public Submission findById(Long id) {
        return submissionRepository.findById(id).orElse(null);
    }

}
