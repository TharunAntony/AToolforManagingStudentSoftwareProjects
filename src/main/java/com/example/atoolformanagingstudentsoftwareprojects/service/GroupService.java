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

    //Auto allocates students into groups in a project based on preferences
    public void autoAllocateGroups(Long projectId) {
        //Get project and validate it exists
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        //Get students for this project
        List<User> students = projectService.getStudentsAssignedToProject(project);
        if (students == null || students.isEmpty()) {
            throw new IllegalStateException("There are no students assigned to this project.");
        }

        //Find students who are already in groups
        List<GroupMember> existingMembers = groupMembersRepository.findByGroup_Project(project);
        Set<Long> alreadyAssignedIds = new HashSet<>();

        //Track which students are already in groups
        for (GroupMember member : existingMembers) {
            alreadyAssignedIds.add(member.getStudent().getStudent().getId());
        }

        //Make a list of students who still need groups
        List<User> studentsNeedingGroups = new ArrayList<>();
        for (User s : students) {
            if (!alreadyAssignedIds.contains(s.getId())) {
                studentsNeedingGroups.add(s);
            }
        }

        System.out.println(studentsNeedingGroups);

        //Get rid of students who are already in groups
        students = studentsNeedingGroups;

        //Figure out how many groups we need
        int studentCount = students.size();
        int maxPerGroup = project.getGroupCapacity();
        int groupCount = (int) Math.ceil((double) studentCount / maxPerGroup);

        //Split students based on whether they filled out preferences
        List<User> studentsWithPrefs = new ArrayList<>();
        List<User> studentsWithoutPrefs = new ArrayList<>();

        for (User s : students) {
            StudentPreferences prefs = s.getStudentDetails().getStudentPreferences();
            if (prefs != null) {
                studentsWithPrefs.add(s);
            } else {
                studentsWithoutPrefs.add(s);
            }
        }

        //Main algorithm works with students who have preferences
        students = studentsWithPrefs;

        //Add some randomness to avoid same groupings when ran multiple times
        Collections.shuffle(students);

        //Calculate how well each pair of students would work together
        Map<User, Map<User, Double>> compatScores = new HashMap<>();
        for (User student1 : students) {
            Map<User, Double> studentScores = new HashMap<>();

            for (User student2 : students) {
                //Don't compare students to themselves
                if (!student1.equals(student2)) {
                    //Higher score = better match
                    double score = calculateCompatibility(student1, student2);
                    studentScores.put(student2, score);
                }
            }

            compatScores.put(student1, studentScores);
        }

        //Choose "seed" students to start each group
        List<User> seedStudents = new ArrayList<>();
        List<User> remainingStudents = new ArrayList<>(students);

        //First try to pick leaders or students who are comfortable in any role
        for (Iterator<User> it = remainingStudents.iterator(); it.hasNext() && seedStudents.size() < groupCount;) {
            User s = it.next();
            int leadershipScore = s.getStudentDetails().getStudentPreferences().getLeadershipPreference();

            //leadership 1 = wants to lead, 3 = flexible
            if (leadershipScore == 1 || leadershipScore == 3) {
                seedStudents.add(s);
                it.remove();
            }
        }

        //If we don't have enough leaders, just pick random students
        while (seedStudents.size() < groupCount && !remainingStudents.isEmpty()) {
            seedStudents.add(remainingStudents.remove(0));
        }

        //Create the actual groups
        List<Groups> groupsList = new ArrayList<>();

        //Create a group for each seed student
        for (int i = 0; i < seedStudents.size(); i++) {
            User seedStudent = seedStudents.get(i);

            // Create group in database
            Groups group = new Groups();
            group.setProject(project);
            group = groupRepository.save(group);

            group.setGroupName("Group " + group.getId());
            group = groupRepository.save(group);
            // Add seed student to group
            GroupMember seedGroupMember = new GroupMember();
            seedGroupMember.setGroup(group);
            seedGroupMember.setStudent(seedStudent.getStudentDetails());
            groupMembersRepository.save(seedGroupMember);

            groupsList.add(group);
        }

        //Keep track of which groups still have space
        List<Groups> availableGroups = new ArrayList<>(groupsList);

        //Keep track of how many students are in each group
        Map<Long, Integer> currentGroupSizes = new HashMap<>();
        for (Groups g : groupsList) {
            //Each group starts with one student (the seed)
            currentGroupSizes.put(g.getId(), 1);
        }

        //Now assign remaining students who have preferences
        Random rand = new Random();
        while (!remainingStudents.isEmpty()) {
            //Pick a random student to assign next
            int randomIndex = rand.nextInt(remainingStudents.size());
            User student = remainingStudents.remove(randomIndex);

            //Find the best group for this student
            Groups bestGroup = null;
            double bestMatchScore = -1;

            //Check each available group
            for (Groups group : availableGroups) {
                //Skip full groups
                int currentSize = currentGroupSizes.get(group.getId());
                if (currentSize >= maxPerGroup) {
                    continue;
                }

                //Get current members of this group
                List<GroupMember> groupMembers = groupMembersRepository.findByGroup(group);

                //Calculate average compatibility with existing members
                double totalCompatibility = 0;
                for (GroupMember member : groupMembers) {
                    User memberUser = member.getStudent().getStudent();
                    // Get compatibility or use 0.5 as fallback
                    double pairScore = compatScores.get(student).getOrDefault(memberUser, 0.5);
                    totalCompatibility += pairScore;
                }

                //Calculate average (or use 0.5 for empty groups)
                double avgCompat = groupMembers.isEmpty() ? 0.5 : totalCompatibility / groupMembers.size();

                // Keep track of best match
                if (avgCompat > bestMatchScore) {
                    bestMatchScore = avgCompat;
                    bestGroup = group;
                }
            }

            //Assign student to best group
            if (bestGroup != null) {
                //Create membership record
                GroupMember newMember = new GroupMember();
                newMember.setGroup(bestGroup);
                newMember.setStudent(student.getStudentDetails());
                groupMembersRepository.save(newMember);

                // Update group size
                int newSize = currentGroupSizes.get(bestGroup.getId()) + 1;
                currentGroupSizes.put(bestGroup.getId(), newSize);

                //Remove group from available list if it is full
                if (newSize >= maxPerGroup) {
                    availableGroups.remove(bestGroup);
                }
            } else {
                throw new IllegalStateException("Couldn't find an available group with space!");
            }
        }

        //Assigns students without preferences randomly
        for (User student : studentsWithoutPrefs) {
            // Find the smallest group that still has room
            Groups smallestGroup = null;
            int smallestSize = Integer.MAX_VALUE;

            for (Groups group : availableGroups) {
                int size = currentGroupSizes.get(group.getId());
                if (size < smallestSize && size < maxPerGroup) {
                    smallestSize = size;
                    smallestGroup = group;
                }
            }

            if (smallestGroup != null) {
                // Add the student to the smallest group
                GroupMember newMember = new GroupMember();
                newMember.setGroup(smallestGroup);
                newMember.setStudent(student.getStudentDetails());
                groupMembersRepository.save(newMember);

                // Update group size
                int newSize = currentGroupSizes.get(smallestGroup.getId()) + 1;
                currentGroupSizes.put(smallestGroup.getId(), newSize);

                // Remove group if now full
                if (newSize >= maxPerGroup) {
                    availableGroups.remove(smallestGroup);
                }
            } else {
                throw new IllegalStateException("No groups available for students without preferences!");
            }
        }
    }

    //Calculates the compatibility of 2 students working in a group using their preferences
    public double calculateCompatibility(User s1, User s2) {
        StudentPreferences p1 = s1.getStudentDetails().getStudentPreferences();
        StudentPreferences p2 = s2.getStudentDetails().getStudentPreferences();

        double score = 0;

        //Compares the users working style (If they are independent, collaborative or flexible)
        if (p1.getWorkingStyle() == p2.getWorkingStyle() || p1.getWorkingStyle() == 3 || p2.getWorkingStyle() == 3) {
            score += 0.15;
        }

        //Compares preferred working hours to match working hours of students
        if (p1.getWorkingHours() == p2.getWorkingHours() || p1.getWorkingHours() == 4 || p2.getWorkingHours() == 4) {
            score += 0.1;
        }

        //Compares technical Skill, the closer they are in skill the better the score
        score += (1.0 - Math.abs(p1.getTechnicalSkill() - p2.getTechnicalSkill()) / 4.0) * 0.15;

        //Compares communication skills and pairs up strong with weak communication
        int combinedComm = p1.getCommunicationSkill() + p2.getCommunicationSkill();
        int difference = Math.abs(6 - combinedComm);
        score += Math.max(0.1 - 0.02 * (difference), 0);

        //Compares to see if one is leader and other one is a supporter. Higher score if they are.
        if ((p1.getLeadershipPreference() == 1 && p2.getLeadershipPreference() == 2)
                || (p1.getLeadershipPreference() == 2 && p2.getLeadershipPreference() == 1)) {
            score += 0.1;
        }

        //Checks when students like to finish the project by
        if (p1.getDeadlineApproach() == p2.getDeadlineApproach()) {
            score += 0.1;
        }


        //Encourages pairing between students with different experience levels
        int combinedTeamExp = p1.getTeamworkExperience() + p2.getTeamworkExperience();
        int differenceTeamExp = Math.abs(6 - combinedTeamExp);
        score += Math.max(1 - 0.2 * (differenceTeamExp), 0) * 0.15;


        if (p1.getPriorExperience() && !p2.getPriorExperience() || !p1.getPriorExperience() && p2.getPriorExperience()) {
            score += 0.15;
        }

        return score;
    }

    //Used to calculate the average compatibility between students in group and get a group compatibility score
    public Map<Long, Double> evaluateGroupCompatibility(List<Groups> groups) {
        Map<Long, Double> groupScores = new HashMap<>();

        for (Groups group : groups) {
            List<GroupMember> members = groupMembersRepository.findByGroup(group);
            double totalCompat = 0;
            int pairCount = 0;

            //Calculate compatibility between each pair
            for (int i = 0; i < members.size(); i++) {
                for (int j = i+1; j < members.size(); j++) {
                    try {
                        User u1 = members.get(i).getStudent().getStudent();
                        User u2 = members.get(j).getStudent().getStudent();

                        //Skip if either user is null
                        if (u1 == null || u2 == null) {
                            totalCompat += 0; // Default neutral compatibility
                            pairCount++;
                            continue;
                        }

                        //Calculate compatibility and handle potential exceptions
                        double compatScore = calculateCompatibility(u1, u2);
                        totalCompat += compatScore;
                        pairCount++;
                    } catch (Exception exception) {
                        //If any error occurs during compatibility calculation use a default score of 0.5
                        totalCompat += 0.5;
                        pairCount++;
                    }
                }
            }

            //Calculate average compatibility ensuring we don't divide by zero
            double avgCompat = (pairCount > 0) ? totalCompat / pairCount : 0.5;
            groupScores.put(group.getId(), avgCompat);
        }

        return groupScores;
    }

    public Groups getGroupById(Long id) {
        return groupRepository.findById(id).orElse(null);
    }

}