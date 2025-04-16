package com.example.atoolformanagingstudentsoftwareprojects.repository;

import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    public User findByUsername(String username);
    public User findById(int id);

}
