package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.model.GroupMember;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.model.Groups;
import com.example.atoolformanagingstudentsoftwareprojects.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupMemberService {
    @Autowired
    private GroupMemberRepository groupMemberRepository;

    public List<GroupMember> getAllGroupMemberships(User user) {
        return groupMemberRepository.findByStudent(user.getStudentDetails());
    }

    public List<GroupMember> getGroupMembersByGroup(Groups group) {
        return groupMemberRepository.findByGroup(group);
    }


}
