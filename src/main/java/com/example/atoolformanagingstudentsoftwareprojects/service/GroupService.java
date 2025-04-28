package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.model.Project;
import com.example.atoolformanagingstudentsoftwareprojects.model.Groups;
import com.example.atoolformanagingstudentsoftwareprojects.model.GroupMember;
import com.example.atoolformanagingstudentsoftwareprojects.model.User;
import com.example.atoolformanagingstudentsoftwareprojects.model.StudentPreferences;
import com.example.atoolformanagingstudentsoftwareprojects.repository.GroupMemberRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.GroupsRepository;
import com.example.atoolformanagingstudentsoftwareprojects.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GroupService {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GroupsRepository groupRepository;

    @Autowired
    private GroupMemberRepository groupMembersRepository;


    public void autoAllocateGroups(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + projectId));

        List<User> students = projectService.getStudentsAssignedToProject(project);

        if (students == null || students.isEmpty()) {
            throw new IllegalStateException("No students assigned to project.");
        }

        //Get all students already assigned to a group in this project
        List<GroupMember> alreadyGrouped = groupMembersRepository.findByGroup_Project(project);
        Set<Long> groupedStudentIds = new HashSet<>();

        for (GroupMember gm : alreadyGrouped) {
            groupedStudentIds.add(gm.getStudent().getId());
        }

        //Getting of the students already assigned to a project
        List<User> ungroupedStudents = new ArrayList<>();
        for (User student : students) {
            if (!groupedStudentIds.contains(student.getStudentDetails().getId())) {
                ungroupedStudents.add(student);
            }
        }

        students = ungroupedStudents;

        //Separating students who have set their preference and students who haven't
        List<User> studentsWithPreferences = new ArrayList<>();
        List<User> studentsWithoutPreferences = new ArrayList<>();

        for (User student : students) {
            StudentPreferences prefs = student.getStudentDetails().getStudentPreferences();
            if (prefs != null) {
                studentsWithPreferences.add(student);
            } else {
                //storing students without preferences to randomly assign later
                studentsWithoutPreferences.add(student);
            }
        }

        //Allocating the student with preferences first
        students = studentsWithPreferences;

        Collections.shuffle(students);

        int groupCapacity = project.getGroupCapacity();
        int totalStudents = students.size();
        int numGroups = (int) Math.ceil((double) totalStudents / groupCapacity);

        int baseGroupSize = totalStudents / numGroups;
        int extraGroups = totalStudents % numGroups; // Groups that get 1 extra member

        // Build compatibility matrix
        Map<User, Map<User, Double>> compatibilityMatrix = new HashMap<>();
        for (User s1 : students) {
            Map<User, Double> scores = new HashMap<>();
            for (User s2 : students) {
                if (!s1.equals(s2)) {
                    double score = calculateCompatibility(s1, s2);
                    scores.put(s2, score);
                }
            }
            compatibilityMatrix.put(s1, scores);
        }

        List<User> seeds = new ArrayList<>();
        List<User> unassigned = new ArrayList<>(students);

        // Pick seeds (leaders or no preference)
        for (Iterator<User> it = unassigned.iterator(); it.hasNext() && seeds.size() < numGroups;) {
            User student = it.next();
            int leadership = student.getStudentDetails().getStudentPreferences().getLeadershipPreference();

            if (leadership == 1 || leadership == 3) {
                seeds.add(student);
                it.remove();
            }
        }

        // Fill remaining seeds randomly if needed
        while (seeds.size() < numGroups && !unassigned.isEmpty()) {
            seeds.add(unassigned.remove(0));
        }

        List<Groups> groups = new ArrayList<>();

        for (User seed : seeds) {
            Groups group = new Groups();
            group.setProject(project);
            group = groupRepository.save(group);

            group.setGroupName("Group " + group.getId());
            groupRepository.save(group);

            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setStudent(seed.getStudentDetails());
            groupMembersRepository.save(member);

            groups.add(group);
        }

        Random random = new Random();
        int groupIndex = 0;

        while (!unassigned.isEmpty()) {
            User student = unassigned.remove(random.nextInt(unassigned.size()));

            double bestScore = -1;
            Groups bestGroup = null;

            for (Groups group : groups) {
                int currentSize = group.getGroupMembers().size();
                int expectedSize = baseGroupSize + (groups.indexOf(group) < extraGroups ? 1 : 0);

                if (currentSize >= expectedSize) {
                    continue;
                }

                double totalCompat = 0;
                for (GroupMember gm : group.getGroupMembers()) {
                    totalCompat += compatibilityMatrix.get(student).getOrDefault(gm.getStudent(), 0.5);
                }
                double avgCompat = group.getGroupMembers().isEmpty() ? 0.5 : totalCompat / group.getGroupMembers().size();

                if (avgCompat > bestScore) {
                    bestScore = avgCompat;
                    bestGroup = group;
                }
            }

            if (bestGroup != null) {
                GroupMember member = new GroupMember();
                member.setGroup(bestGroup);
                member.setStudent(student.getStudentDetails());
                groupMembersRepository.save(member);
            } else {
                // fallback
                Groups fallbackGroup = groups.get(groupIndex);
                GroupMember member = new GroupMember();
                member.setGroup(fallbackGroup);
                member.setStudent(student.getStudentDetails());
                groupMembersRepository.save(member);

                groupIndex = (groupIndex + 1) % groups.size();
            }
        }

        //Now assign students without preferences randomly, trying to fill smaller groups first
        for (User student : studentsWithoutPreferences) {
            Groups smallestGroup = null;
            int minSize = Integer.MAX_VALUE;

            //Find the group with the fewest members
            for (Groups group : groups) {
                int groupSize = group.getGroupMembers().size();
                if (groupSize < minSize) {
                    minSize = groupSize;
                    smallestGroup = group;
                }
            }

            //Randomly assign the rest into groups
            if (smallestGroup != null) {
                GroupMember member = new GroupMember();
                member.setGroup(smallestGroup);
                member.setStudent(student.getStudentDetails());
                groupMembersRepository.save(member);
            }
        }

    }

    private double calculateCompatibility(User s1, User s2) {
        StudentPreferences p1 = s1.getStudentDetails().getStudentPreferences();
        StudentPreferences p2 = s2.getStudentDetails().getStudentPreferences();

        double score = 0;

        if (p1.getWorkingStyle() == p2.getWorkingStyle() || p1.getWorkingStyle() == 3 || p2.getWorkingStyle() == 3) {
            score += 0.15;
        }

        if (p1.getWorkingHours() == p2.getWorkingHours() || p1.getWorkingHours() == 4 || p2.getWorkingHours() == 4) {
            score += 0.1;
        }

        score += (1.0 - Math.abs(p1.getTechnicalSkill() - p2.getTechnicalSkill()) / 4.0) * 0.15;
        score += (1.0 - Math.abs(p1.getCommunicationSkill() - p2.getCommunicationSkill()) / 4.0) * 0.1;

        if ((p1.getLeadershipPreference() == 1 && p2.getLeadershipPreference() == 2)
                || (p1.getLeadershipPreference() == 2 && p2.getLeadershipPreference() == 1)) {
            score += 0.1;
        }

        if (p1.getDeadlineApproach() == p2.getDeadlineApproach()) {
            score += 0.1;
        }

        score += (p1.getTeamworkExperience() + p2.getTeamworkExperience()) / 10.0 * 0.15;

        if (p1.getPriorExperience()|| p2.getPriorExperience()) {
            score += 0.15;
        }

        return score;
    }
}
