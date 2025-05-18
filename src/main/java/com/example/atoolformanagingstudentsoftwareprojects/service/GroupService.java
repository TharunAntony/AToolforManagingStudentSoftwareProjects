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
        Project project = getValidProject(projectId);

        //Get students for this project
        List<User> students = getStudentsNeedingGroups(project);
        int maxPerGroup = project.getGroupCapacity();

        //Get existing groups for this project
        List<Groups> existingGroups = groupRepository.findByProject(project);

        //Determine if we need to create new groups or just fill existing ones
        boolean needNewGroups = false;
        int totalAvailableSpots = 0;

        //Count available spots in existing groups
        for (Groups group : existingGroups) {
            int currentSize = groupMembersRepository.findByGroup(group).size();
            totalAvailableSpots += (maxPerGroup - currentSize);
        }

        //Check if we need new groups
        if (students.size() > totalAvailableSpots) {
            needNewGroups = true;
        }

        //Split students based on whether they filled out preferences
        List<User> studentsWithPrefs = new ArrayList<>();
        List<User> studentsWithoutPrefs = new ArrayList<>();
        splitStudentsByPreference(students, studentsWithPrefs, studentsWithoutPrefs);

        //List of students with preferences for main algorithm
        students = studentsWithPrefs;

        //Add some randomness to avoid same groupings when ran multiple times
        Collections.shuffle(students);

        //Calculate how well each pair of students would work together
        Map<User, Map<User, Double>> compatScores = calculateCompatibilityScores(students);

        //Keep track of how many students are in each group
        Map<Long, Integer> currentGroupSizes = new HashMap<>();

        //Calculate current group sizes
        for (Groups group : existingGroups) {
            int size = groupMembersRepository.findByGroup(group).size();
            currentGroupSizes.put(group.getId(), size);
        }

        List<Groups> availableGroups = new ArrayList<>();
        List<User> seedStudents = new ArrayList<>();

        //If existing groups have space, use them. Only create new groups if needed.
        if (needNewGroups) {
            //Calculate how many new groups are needed - only for students who won't fit in existing spaces
            int remainingStudents = students.size() + studentsWithoutPrefs.size() - totalAvailableSpots;
            int additionalGroupsNeeded = calculateBalancedGroupCount(remainingStudents, maxPerGroup);

            //Choose "seed" students to start each new group
            seedStudents = pickSeedStudents(students, additionalGroupsNeeded);

            //Create the additional groups
            List<Groups> newGroups = createGroupsWithSeeds(seedStudents, project);

            //Add new groups to the list of existing groups
            existingGroups.addAll(newGroups);
        }

        //Keep track of which groups still have space
        availableGroups = recalculateGroupSizes(project, existingGroups, currentGroupSizes, maxPerGroup);

        //Remove seed students from main list as they're already allocated
        students.removeAll(seedStudents);

        //Now assign remaining students who have preferences
        assignRemainingStudents(students, compatScores, availableGroups, currentGroupSizes, maxPerGroup);

        //Assigns students without preferences
        assignStudentsWithoutPreferences(studentsWithoutPrefs, project, availableGroups, currentGroupSizes, maxPerGroup);
    }

    //Get project and validate it exists
    private Project getValidProject(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
    }

    //Get students for this project
    private List<User> getStudentsNeedingGroups(Project project) {
        List<User> allStudents = projectService.getStudentsAssignedToProject(project);
        if (allStudents == null || allStudents.isEmpty()) {
            throw new IllegalStateException("There are no students assigned to this project.");
        }

        //Find students who are already in groups
        Set<Long> alreadyAssignedIds = new HashSet<>();
        List<GroupMember> existingMembers = groupMembersRepository.findByGroup_Project(project);
        for (GroupMember member : existingMembers) {
            alreadyAssignedIds.add(member.getStudent().getStudent().getId());
        }

        //Make a list of students who still need groups
        List<User> ungroupedStudents = new ArrayList<>();
        for (User student : allStudents) {
            if (!alreadyAssignedIds.contains(student.getId())) {
                ungroupedStudents.add(student);
            }
        }

        //Get rid of students who are already in groups
        return ungroupedStudents;
    }

    //Split students based on whether they filled out preferences
    private void splitStudentsByPreference(List<User> all, List<User> withPrefs, List<User> withoutPrefs) {
        for (User s : all) {
            if (s.getStudentDetails().getStudentPreferences() != null) {
                withPrefs.add(s);
            } else {
                withoutPrefs.add(s);
            }
        }
    }

    //Calculate how well each pair of students would work together
    private Map<User, Map<User, Double>> calculateCompatibilityScores(List<User> students) {
        Map<User, Map<User, Double>> scores = new HashMap<>();

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
            scores.put(student1, studentScores);
        }

        return scores;
    }

    //Choose "seed" students to start each group
    private List<User> pickSeedStudents(List<User> students, int groupCount) {
        List<User> seedStudents = new ArrayList<>();
        List<User> remaining = new ArrayList<>(students);

        //First try to pick leaders or students who are comfortable in any role
        for (Iterator<User> it = remaining.iterator(); it.hasNext() && seedStudents.size() < groupCount;) {
            User s = it.next();
            int leadership = s.getStudentDetails().getStudentPreferences().getLeadershipPreference();
            //leadership 1 = wants to lead, 3 = flexible
            if (leadership == 1 || leadership == 3) {
                seedStudents.add(s);
                it.remove();
            }
        }

        //If we don't have enough leaders, just pick random students
        while (seedStudents.size() < groupCount && !remaining.isEmpty()) {
            seedStudents.add(remaining.remove(0));
        }

        students.clear();
        students.addAll(remaining);

        return seedStudents;
    }

    //Create the actual groups
    private List<Groups> createGroupsWithSeeds(List<User> seedStudents, Project project) {
        List<Groups> groups = new ArrayList<>();

        //Create a group for each seed student
        for (User seed : seedStudents) {
            // Create group in database
            Groups group = new Groups();
            group.setProject(project);
            group = groupRepository.save(group);
            group.setGroupName("Group " + group.getId());
            group = groupRepository.save(group);

            // Add seed student to group
            GroupMember member = new GroupMember();
            member.setGroup(group);
            member.setStudent(seed.getStudentDetails());
            groupMembersRepository.save(member);

            groups.add(group);
        }

        return groups;
    }

    //Keep track of how many students are in each group and keep track of which groups still have space
    private List<Groups> recalculateGroupSizes(Project project, List<Groups> allGroups, Map<Long, Integer> sizes, int maxCap) {
        List<Groups> available = new ArrayList<>();

        //Recalculate group sizes and filter out all the full groups
        for (Groups group : allGroups) {
            int size = groupMembersRepository.findByGroup(group).size();
            sizes.put(group.getId(), size);
            if (size < maxCap) {
                available.add(group);
            }
        }

        return available;
    }

    //Now assign remaining students who have preferences
    private void assignRemainingStudents(List<User> students, Map<User, Map<User, Double>> compatScores, List<Groups> availableGroups, Map<Long, Integer> sizes, int maxCap) {

        Random rand = new Random();

        while (!students.isEmpty() && !availableGroups.isEmpty()) {
            //Pick a random student to assign next
            int index = rand.nextInt(students.size());
            User student = students.remove(index);

            //Sort groups by size to balance load
            availableGroups.sort(Comparator.comparingInt(g -> sizes.get(g.getId())));

            //Find the best group for this student
            Groups bestGroup = null;
            double bestScore = -1;

            //Check each available group
            for (Groups group : availableGroups) {
                //Skip full groups
                if (sizes.get(group.getId()) >= maxCap) continue;

                //Get current members of this group
                List<GroupMember> members = groupMembersRepository.findByGroup(group);

                //Calculate average compatibility with existing members
                double totalScore = 0;
                for (GroupMember m : members) {
                    User memberUser = m.getStudent().getStudent();
                    // Get compatibility or use 0.5 as a fallback
                    double compScore = 0.5; // Default value
                    if (compatScores.containsKey(student) && memberUser != null) {
                        compScore = compatScores.get(student).getOrDefault(memberUser, 0.5);
                    }
                    totalScore += compScore;
                }

                //Calculate average (or use 0.5 for empty groups)
                double avg = members.isEmpty() ? 0.5 : totalScore / members.size();

                //Balance between compatibility and group size
                //Give smaller groups a bonus to encourage even distribution
                double sizeBonus = 0.05 * (maxCap - sizes.get(group.getId()));
                double adjustedScore = avg + sizeBonus;

                // Keep track of best match
                if (adjustedScore > bestScore) {
                    bestScore = adjustedScore;
                    bestGroup = group;
                }
            }

            //Assign student to best group
            if (bestGroup != null) {
                //Create membership record
                GroupMember member = new GroupMember();
                member.setGroup(bestGroup);
                member.setStudent(student.getStudentDetails());
                groupMembersRepository.save(member);

                // Update group size
                int newSize = sizes.get(bestGroup.getId()) + 1;
                sizes.put(bestGroup.getId(), newSize);

                //Remove group from available list if it is full
                if (newSize >= maxCap) {
                    availableGroups.remove(bestGroup);
                }
            } else {
                throw new IllegalStateException("Couldn't find an available group with space!");
            }
        }
    }

    //Assigns students without preferences randomly
    private void assignStudentsWithoutPreferences(List<User> students, Project project, List<Groups> availableGroups, Map<Long, Integer> sizes, int maxCap) {
        //Resort groups by size to ensure even distribution
        availableGroups.sort(Comparator.comparingInt(g -> sizes.get(g.getId())));

        int totalFreeSpots = 0;
        for (Groups g : availableGroups) {
            totalFreeSpots += (maxCap - sizes.get(g.getId()));
        }

        if (students.size() <= totalFreeSpots) {
            //Distribute into existing groups
            for (User student : students) {
                //Resort each time to pick the smallest group
                availableGroups.sort(Comparator.comparingInt(g -> sizes.get(g.getId())));

                //Pick the smallest group that still has space
                Groups bestGroup = null;
                for (Groups group : availableGroups) {
                    if (sizes.get(group.getId()) < maxCap) {
                        bestGroup = group;
                        break;
                    }
                }

                if (bestGroup != null) {
                    GroupMember member = new GroupMember();
                    member.setGroup(bestGroup);
                    member.setStudent(student.getStudentDetails());
                    groupMembersRepository.save(member);

                    int newSize = sizes.get(bestGroup.getId()) + 1;
                    sizes.put(bestGroup.getId(), newSize);

                    if (newSize >= maxCap) {
                        availableGroups.remove(bestGroup);
                    }
                }
            }
        } else {
            //Handle remaining students that can't fit into existing groups
            List<User> remainingStudents = new ArrayList<>(students);

            //First, fill existing groups
            for (Iterator<User> it = remainingStudents.iterator(); it.hasNext() && !availableGroups.isEmpty();) {
                User student = it.next();

                //Resort to get smallest group
                availableGroups.sort(Comparator.comparingInt(g -> sizes.get(g.getId())));
                Groups group = availableGroups.get(0);

                GroupMember member = new GroupMember();
                member.setGroup(group);
                member.setStudent(student.getStudentDetails());
                groupMembersRepository.save(member);

                int newSize = sizes.get(group.getId()) + 1;
                sizes.put(group.getId(), newSize);

                if (newSize >= maxCap) {
                    availableGroups.remove(group);
                }

                it.remove();
            }

            //Create new groups for any remaining students
            if (!remainingStudents.isEmpty()) {
                //Batch students into new groups
                List<List<User>> batches = makeBalancedBatches(remainingStudents, maxCap);
                for (List<User> batch : batches) {
                    Groups newGroup = new Groups();
                    newGroup.setProject(project);
                    newGroup = groupRepository.save(newGroup);
                    newGroup.setGroupName("Group " + newGroup.getId());
                    newGroup = groupRepository.save(newGroup);

                    for (User student : batch) {
                        GroupMember member = new GroupMember();
                        member.setGroup(newGroup);
                        member.setStudent(student.getStudentDetails());
                        groupMembersRepository.save(member);
                    }

                    sizes.put(newGroup.getId(), batch.size());
                }
            }
        }
    }

    //Calculates the best balanced number of groups
    private int calculateBalancedGroupCount(int studentCount, int maxPerGroup) {
        int minGroupCount = studentCount / maxPerGroup;
        int remainder = studentCount % maxPerGroup;

        if (remainder > 0) {
            minGroupCount++;
        }

        return minGroupCount;
    }

    //Splits a list of students into roughly equal batches
    private List<List<User>> makeBalancedBatches(List<User> students, int maxPerGroup) {
        List<List<User>> batches = new ArrayList<>();
        List<User> current = new ArrayList<>();

        for (User s : students) {
            current.add(s);
            if (current.size() == maxPerGroup) {
                batches.add(new ArrayList<>(current));
                current.clear();
            }
        }

        if (!current.isEmpty()) {
            batches.add(current);
        }

        return batches;
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
            score += 0.15;
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
            score += 0.10;
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