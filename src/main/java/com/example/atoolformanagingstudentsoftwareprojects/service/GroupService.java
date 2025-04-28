package com.example.atoolformanagingstudentsoftwareprojects.service;

import com.example.atoolformanagingstudentsoftwareprojects.model.*;
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

        // Get all students already assigned to a group
        List<GroupMember> alreadyGrouped = groupMembersRepository.findByGroup_Project(project);
        Set<Long> groupedStudentIds = new HashSet<>();

        for (GroupMember gm : alreadyGrouped) {
            groupedStudentIds.add(gm.getStudent().getStudent().getId());
        }

        List<User> ungroupedStudents = new ArrayList<>();
        for (User student : students) {
            if (!groupedStudentIds.contains(student.getId())) {
                ungroupedStudents.add(student);
            }
        }

        students = ungroupedStudents;

        //Calculate groups based on students
        int totalStudents = students.size();
        int groupCapacity = project.getGroupCapacity();
        int numGroups = (int) Math.ceil((double) totalStudents / groupCapacity);

        //Separate students with and without preferences
        List<User> studentsWithPreferences = new ArrayList<>();
        List<User> studentsWithoutPreferences = new ArrayList<>();

        for (User student : students) {
            StudentPreferences prefs = student.getStudentDetails().getStudentPreferences();
            if (prefs != null) {
                studentsWithPreferences.add(student);
            } else {
                studentsWithoutPreferences.add(student);
            }
        }

        students = studentsWithPreferences;

        Collections.shuffle(students);

        //Build compatibility matrix to see whose preferences match
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

        // Pick seeds (leaders or flexible students)
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

        int groupNum = 0;
        // Create groups with seed students
        for (User seed : seeds) {
            groupNum++;
            Groups group = new Groups();
            group.setProject(project);
            group.setGroupName("Group " + groupNum);
            group = groupRepository.save(group);

            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setStudent(seed.getStudentDetails());
            groupMembersRepository.save(member);

            groups.add(group);
        }

        // Track which groups are still available
        List<Groups> availableGroups = new ArrayList<>(groups);
        Random random = new Random();

        // Track group sizes - this is the key fix
        Map<Long, Integer> groupSizes = new HashMap<>();
        for (Groups group : groups) {
            groupSizes.put(group.getId(), 1); // Start with 1 for seed member
        }

        // Assign unassigned students with preferences
        while (!unassigned.isEmpty()) {
            User student = unassigned.remove(random.nextInt(unassigned.size()));

            double bestScore = -1;
            Groups bestGroup = null;

            for (Groups group : availableGroups) {
                int currentSize = groupSizes.get(group.getId());
                if (currentSize >= groupCapacity) {
                    continue;
                }

                // For compatibility calculation, we need the actual members
                List<GroupMember> groupMembers = groupMembersRepository.findByGroup(group);

                double totalCompat = 0;
                for (GroupMember gm : groupMembers) {
                    totalCompat += compatibilityMatrix.get(student).getOrDefault(gm.getStudent().getStudent(), 0.5);
                }
                double avgCompat = groupMembers.isEmpty() ? 0.5 : totalCompat / groupMembers.size();

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

                // Update the group size in our tracking map
                int newSize = groupSizes.get(bestGroup.getId()) + 1;
                groupSizes.put(bestGroup.getId(), newSize);

                if (newSize >= groupCapacity) {
                    availableGroups.remove(bestGroup);
                }
            } else {
                throw new IllegalStateException("No available group with space left!");
            }
        }

        // Now assign students without preferences randomly
        for (User student : studentsWithoutPreferences) {
            Groups smallestGroup = null;
            int minSize = Integer.MAX_VALUE;

            for (Groups group : availableGroups) {
                int groupSize = groupSizes.get(group.getId());
                if (groupSize < minSize && groupSize < groupCapacity) {
                    minSize = groupSize;
                    smallestGroup = group;
                }
            }

            if (smallestGroup != null) {
                GroupMember member = new GroupMember();
                member.setGroup(smallestGroup);
                member.setStudent(student.getStudentDetails());
                groupMembersRepository.save(member);

                // Update the group size in our tracking map
                int newSize = groupSizes.get(smallestGroup.getId()) + 1;
                groupSizes.put(smallestGroup.getId(), newSize);

                if (newSize >= groupCapacity) {
                    availableGroups.remove(smallestGroup);
                }
            } else {
                throw new IllegalStateException("No available group for students without preferences!");
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

        if (p1.getPriorExperience() || p2.getPriorExperience()) {
            score += 0.15;
        }

        return score;
    }

    public Map<Long, Double> evaluateGroupCompatibility(List<Groups> groups) {
        Map<Long, Double> groupScores = new HashMap<>();

        for (Groups group : groups) {
            List<GroupMember> members = groupMembersRepository.findByGroup(group);
            double totalCompat = 0;
            int pairCount = 0;

            // Calculate compatibility between each pair
            for (int i = 0; i < members.size(); i++) {
                for (int j = i+1; j < members.size(); j++) {
                    try {
                        User u1 = members.get(i).getStudent().getStudent();
                        User u2 = members.get(j).getStudent().getStudent();

                        // Skip if either user is null
                        if (u1 == null || u2 == null) {
                            totalCompat += 0.5; // Default neutral compatibility
                            pairCount++;
                            continue;
                        }

                        // Calculate compatibility and handle potential exceptions
                        double compatScore = calculateCompatibility(u1, u2);
                        totalCompat += compatScore;
                        pairCount++;
                    } catch (Exception exception) {
                        // If any error occurs during compatibility calculation, use a default score of 0.5 (neutral compatibility)
                        totalCompat += 0.5;
                        pairCount++;
                    }
                }
            }

            // Calculate average compatibility, ensuring we don't divide by zero
            double avgCompat = (pairCount > 0) ? totalCompat / pairCount : 0.5;
            groupScores.put(group.getId(), avgCompat);
        }

        return groupScores;
    }
}