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
        students = getStudentsNeedingGroups(students, project);

        //Figure out how many groups we need
        int maxPerGroup = project.getGroupCapacity();
        int groupCount = (int) Math.ceil((double) students.size() / maxPerGroup);

        //Split students based on whether they filled out preferences
        List<User> studentsWithPrefs = new ArrayList<>();
        List<User> studentsWithoutPrefs = new ArrayList<>();
        splitByPreferences(students, studentsWithPrefs, studentsWithoutPrefs);

        //Add some randomness to avoid same groupings when ran multiple times
        Collections.shuffle(studentsWithPrefs);

        //Calculate how well each pair of students would work together
        Map<User, Map<User, Double>> compatScores = calculateCompatibilityMap(studentsWithPrefs);

        //Choose "seed" students to start each group
        List<User> seedStudents = pickSeedStudents(studentsWithPrefs, groupCount);

        //Create the actual groups
        List<Groups> groupsList = createInitialGroups(seedStudents, project);

        //Rebuild list of all current groups in this project (including old ones from DB)
        Map<Long, Integer> currentGroupSizes = new HashMap<>();
        List<Groups> availableGroups = updateGroupSizesAndAvailability(groupsList, project, maxPerGroup, currentGroupSizes);

        //Now assign remaining students who have preferences
        assignStudentsWithPrefs(studentsWithPrefs, seedStudents, compatScores, availableGroups, currentGroupSizes, maxPerGroup);

        //Assigns students without preferences randomly
        assignStudentsWithoutPrefs(studentsWithoutPrefs, availableGroups, currentGroupSizes, maxPerGroup, project);
    }

    //Find students who are already in groups
    private List<User> getStudentsNeedingGroups(List<User> students, Project project) {
        Set<Long> alreadyAssignedIds = new HashSet<>();
        List<GroupMember> existingMembers = groupMembersRepository.findByGroup_Project(project);
        for (GroupMember member : existingMembers) {
            alreadyAssignedIds.add(member.getStudent().getStudent().getId());
        }

        List<User> result = new ArrayList<>();
        for (User s : students) {
            if (!alreadyAssignedIds.contains(s.getId())) {
                result.add(s);
            }
        }
        return result;
    }

    //Split students based on whether they filled out preferences
    private void splitByPreferences(List<User> all, List<User> withPrefs, List<User> withoutPrefs) {
        for (User s : all) {
            StudentPreferences prefs = s.getStudentDetails().getStudentPreferences();
            if (prefs != null) {
                withPrefs.add(s);
            } else {
                withoutPrefs.add(s);
            }
        }
    }

    //Calculate how well each pair of students would work together
    private Map<User, Map<User, Double>> calculateCompatibilityMap(List<User> students) {
        Map<User, Map<User, Double>> compat = new HashMap<>();
        for (User s1 : students) {
            Map<User, Double> scores = new HashMap<>();
            for (User s2 : students) {
                if (!s1.equals(s2)) {
                    scores.put(s2, calculateCompatibility(s1, s2));
                }
            }
            compat.put(s1, scores);
        }
        return compat;
    }

    //Choose "seed" students to start each group
    private List<User> pickSeedStudents(List<User> students, int needed) {
        List<User> seeds = new ArrayList<>();
        Iterator<User> it = students.iterator();
        while (it.hasNext() && seeds.size() < needed) {
            User s = it.next();
            int leader = s.getStudentDetails().getStudentPreferences().getLeadershipPreference();
            if (leader == 1 || leader == 3) {
                seeds.add(s);
                it.remove();
            }
        }

        while (seeds.size() < needed && !students.isEmpty()) {
            seeds.add(students.remove(0));
        }

        return seeds;
    }

    //Create the actual groups
    private List<Groups> createInitialGroups(List<User> seeds, Project project) {
        List<Groups> result = new ArrayList<>();
        for (User s : seeds) {
            Groups group = new Groups();
            group.setProject(project);
            group = groupRepository.save(group);
            group.setGroupName("Group " + group.getId());
            group = groupRepository.save(group);

            GroupMember m = new GroupMember();
            m.setGroup(group);
            m.setStudent(s.getStudentDetails());
            groupMembersRepository.save(m);

            result.add(group);
        }
        return result;
    }

    //Rebuild list of all current groups in this project (including old ones from DB)
    private List<Groups> updateGroupSizesAndAvailability(List<Groups> newGroups, Project project, int maxPerGroup, Map<Long, Integer> sizes) {
        List<Groups> available = new ArrayList<>();
        List<Groups> allGroups = groupRepository.findByProject(project);

        for (Groups group : allGroups) {
            int size = groupMembersRepository.findByGroup(group).size();
            sizes.put(group.getId(), size);
            if (size < maxPerGroup) {
                available.add(group);
            }
        }

        for (Groups g : newGroups) {
            sizes.put(g.getId(), 1); //Each group starts with one student (the seed)
        }

        return available;
    }

    //Now assign remaining students who have preferences
    private void assignStudentsWithPrefs(List<User> allWithPrefs, List<User> seedStudents, Map<User, Map<User, Double>> compat,
                                         List<Groups> available, Map<Long, Integer> sizes, int max) {
        List<User> remaining = new ArrayList<>(allWithPrefs);
        remaining.removeAll(seedStudents);
        Random rand = new Random();

        while (!remaining.isEmpty()) {
            User student = remaining.remove(rand.nextInt(remaining.size()));

            Groups bestGroup = null;
            double bestScore = -1;

            for (Groups group : available) {
                int currentSize = sizes.get(group.getId());
                if (currentSize >= max) continue;

                List<GroupMember> members = groupMembersRepository.findByGroup(group);
                double total = 0;
                for (GroupMember m : members) {
                    User other = m.getStudent().getStudent();
                    total += compat.get(student).getOrDefault(other, 0.5);
                }

                double avg = members.isEmpty() ? 0.5 : total / members.size();
                if (avg > bestScore) {
                    bestScore = avg;
                    bestGroup = group;
                }
            }

            if (bestGroup != null) {
                GroupMember m = new GroupMember();
                m.setGroup(bestGroup);
                m.setStudent(student.getStudentDetails());
                groupMembersRepository.save(m);

                int newSize = sizes.get(bestGroup.getId()) + 1;
                sizes.put(bestGroup.getId(), newSize);
                if (newSize >= max) available.remove(bestGroup);
            } else {
                throw new IllegalStateException("Couldn't find an available group with space!");
            }
        }
    }

    //Assigns students without preferences randomly
    private void assignStudentsWithoutPrefs(List<User> noPrefs, List<Groups> available, Map<Long, Integer> sizes,
                                            int maxPerGroup, Project project) {
        for (User s : noPrefs) {
            Groups smallest = null;
            int smallestSize = Integer.MAX_VALUE;

            for (Groups g : available) {
                int size = sizes.get(g.getId());
                if (size < smallestSize && size < maxPerGroup) {
                    smallest = g;
                    smallestSize = size;
                }
            }

            if (smallest != null) {
                GroupMember m = new GroupMember();
                m.setGroup(smallest);
                m.setStudent(s.getStudentDetails());
                groupMembersRepository.save(m);

                int newSize = sizes.get(smallest.getId()) + 1;
                sizes.put(smallest.getId(), newSize);
                if (newSize >= maxPerGroup) available.remove(smallest);
            } else {
                Groups g = new Groups();
                g.setProject(project);
                g = groupRepository.save(g);
                g.setGroupName("Group " + g.getId());
                g = groupRepository.save(g);

                GroupMember m = new GroupMember();
                m.setGroup(g);
                m.setStudent(s.getStudentDetails());
                groupMembersRepository.save(m);

                sizes.put(g.getId(), 1);
                available.add(g);
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