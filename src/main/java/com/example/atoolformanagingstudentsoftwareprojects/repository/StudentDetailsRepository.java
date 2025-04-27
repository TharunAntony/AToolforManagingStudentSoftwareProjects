package com.example.atoolformanagingstudentsoftwareprojects.repository;

import com.example.atoolformanagingstudentsoftwareprojects.model.StudentDetails;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentDetailsRepository extends JpaRepository<StudentDetails, Long> {

    @Query("SELECT sd FROM StudentDetails sd JOIN FETCH sd.projects WHERE sd.student = :user")
    StudentDetails findByStudent(User user);
}
