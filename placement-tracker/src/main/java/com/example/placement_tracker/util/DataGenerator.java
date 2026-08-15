package com.example.placement_tracker.util;

import com.example.placement_tracker.entity.*;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

//import static org.springframework.data.elasticsearch.annotations.IndexOptions.positions;

//@Component
public class DataGenerator implements CommandLineRunner {

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    StudentApplicationRepository applicationRepository;

    @Autowired
    InterviewExperienceRepository experienceRepository;

    @Autowired
    InterviewRoundConfigRepository configRepository;

    @Autowired
    PositionRepository positionRepository;

    @Override
    public void run(String... args) throws Exception{
        List<Student> students = studentRepository.findAll();
        List<Company> companies = companyRepository.findAll();
        List<Position> positions = positionRepository.findAll();

        if(students.isEmpty() || companies.isEmpty() || positions.isEmpty()){
            createInitialData();
            students = studentRepository.findAll();
            companies = companyRepository.findAll();
            positions = positionRepository.findAll();
        }

//        addMoreInterviewRoundConfigs(companies);
//        addMoreApplications(students, companies, positions);
//        addMoreExperiences(students, companies);

        System.out.println("Test data added successfully");
    }

    private void createInitialData(){
        System.out.println("Creating initial data");
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 500; i++) {
            Student student = Student.builder()
//                    .id(UUID.randomUUID())
                    .email("student" + i + "@example.com")
                    .name("Student " + i)
                    .passwordHash("hashed-password")
                    .batch(2024 + (i % 4))
                    .branch("CSE")
                    .createdAt(System.currentTimeMillis())
                    .build();
            students.add(student);
        }
        studentRepository.saveAll(students);
        System.out.println("Created 50 students");


        List<Company> companies = new ArrayList<>();
        String[] companyNames = {
                "Google", "Amazon", "Microsoft", "Apple", "Meta",
                "Netflix", "Uber", "Airbnb", "LinkedIn", "Spotify",
                "Adobe", "Salesforce", "Oracle", "IBM", "Intel",
                "PayPal", "Stripe", "Zoom", "Slack", "Notion"
        };

        for(String name : companyNames){
            Company company = Company.builder()
//                    .id(UUID.randomUUID())
                    .name(name)
                    .headQuarters("Bangalore")
                    .description(name + " is a great company")
                    .website("https://" + name.toLowerCase() + ".com")
                    .createdAt(System.currentTimeMillis())
                    .build();
            companies.add(company);
        }

        companyRepository.saveAll(companies);
        System.out.println("Created 20 companies");

        List<InterviewRoundConfig> configs = new ArrayList<>();
        String[] roundNames = {
                "OA",
                "GD",
                "Round 1",
                "Round 2",
                "HR Round",
                "Final Round"
        };

        for(Company company : companies){
            for(int i = 0 ;i < roundNames.length; i++){
                InterviewRoundConfig config = InterviewRoundConfig.builder()
//                        .id(UUID.randomUUID())
                        .company(company)
                        .roundName(roundNames[i])
                        .roundNumber(i+1)
                        .createdAt(System.currentTimeMillis())
                        .build();

                configs.add(config);
            }
        }

        configRepository.saveAll(configs);
        System.out.println("Created 100 interview round configs");

        List<Position> positions = new ArrayList<>();
        String[] roles = {
                "Software engineer","Senior Engineer",
                "Manager","Data Analyst","DevOps"
        };

        for(Company company : companies){
            for(String role : roles){
                Position position  = Position.builder()
//                        .id(UUID.randomUUID())
                        .company(company)
                        .title(role)
                        .createdAt(System.currentTimeMillis())
                        .build();

                positions.add(position);
            }
        }
        positionRepository.saveAll(positions);
        System.out.println("Created 100 positions");


    }

    private void addMoreInterviewRoundConfigs(List<Company> companies){
        List<InterviewRoundConfig> configs = new ArrayList<>();
        String[] roundNames = {"Online Assessment", "Technical Round 1", "Technical Round 2", "HR Round", "Final Round"};

        // Add more configs for existing companies
        for (Company company : companies) {
            for (int i = 0; i < roundNames.length; i++) {
                InterviewRoundConfig config = InterviewRoundConfig.builder()
//                        .id(UUID.randomUUID())
                        .company(company)
                        .roundName(roundNames[i])
                        .roundNumber(i + 1)
                        .createdAt(System.currentTimeMillis())
                        .build();
                configs.add(config);
            }
        }

        configRepository.saveAll(configs);
        System.out.println("Added" + configs.size()+" interview round configs");


    }

//    private void addMoreApplications(List<Student> students,List<Company> companies,List<Position> positions){
//        List<StudentApplication> applications = new ArrayList<>();
//
//        ApplicationStatus[] statuses = {
//                ApplicationStatus.APPLIED,
//                ApplicationStatus.OFFER_RECEIVED,
//                ApplicationStatus.OFFER_ACCEPTED,
//                ApplicationStatus.JOINING_LETTER_RECEIVED,
//                ApplicationStatus.OFFER_DECLINED,
//                ApplicationStatus.INTERVIEW_SCHEDULED,
//                ApplicationStatus.INTERVIEW_COMPLETED,
//                ApplicationStatus.RESULT_WAITING,
//                ApplicationStatus.OA_COMPLETED,
//                ApplicationStatus.OA_SCHEDULED,
//                ApplicationStatus.SELECTED
//
//        };
//        Random random = new Random();
//        System.out.println("Students: " + students.size());
//        System.out.println("Companies: " + companies.size());
//        System.out.println("Positions: " + positions.size());
//
//        for(int i = 0 ; i < 200; i++){
//            Student student = students.get(random.nextInt(students.size()));
//            Company company = companies.get(random.nextInt(companies.size()));
//            Position position = positions.stream()
//                    .filter(p -> p.getCompany().getId().equals(company.getId()))
//                    .findAny()
//                    .orElseGet(() -> positions.get(0));
//
//            StudentApplication app = StudentApplication.builder()
//                    .id(UUID.randomUUID())
//                    .student(student)
//                    .company(company)
//                    .position(position)
//                    .status(statuses[random.nextInt(statuses.length)])
//                    .statusUpdatedAt(System.currentTimeMillis() - random.nextLong((30*24*60*60*100)))
//                    .createdAt(System.currentTimeMillis())
//                    .build();
//
//            applications.add(app);
//        }
//
//        applicationRepository.saveAll(applications);
//        System.out.println("Added 500 applications");
//    }

    private void addMoreApplications(List<Student> students,
                                     List<Company> companies,
                                     List<Position> positions) {

        List<StudentApplication> applications = new ArrayList<>();

        ApplicationStatus[] statuses = {
                ApplicationStatus.APPLIED,
                ApplicationStatus.OFFER_RECEIVED,
                ApplicationStatus.OFFER_ACCEPTED,
                ApplicationStatus.JOINING_LETTER_RECEIVED,
                ApplicationStatus.OFFER_DECLINED,
                ApplicationStatus.INTERVIEW_SCHEDULED,
                ApplicationStatus.INTERVIEW_COMPLETED,
                ApplicationStatus.RESULT_WAITING,
                ApplicationStatus.OA_COMPLETED,
                ApplicationStatus.OA_SCHEDULED,
                ApplicationStatus.SELECTED
        };

        Random random = new Random();

        // Create a map: CompanyId -> Positions
        Map<UUID, List<Position>> companyPositions = positions.stream()
                .collect(Collectors.groupingBy(p -> p.getCompany().getId()));

        for (Student student : students) {

            // Shuffle companies so every student applies to different companies
            List<Company> shuffledCompanies = new ArrayList<>(companies);
            Collections.shuffle(shuffledCompanies);

            // Each student applies to up to 15 companies
            int applicationsPerStudent = Math.min(15, shuffledCompanies.size());

            for (int i = 0; i < applicationsPerStudent; i++) {

                Company company = shuffledCompanies.get(i);

                List<Position> companyPositionList =
                        companyPositions.get(company.getId());

                if (companyPositionList == null || companyPositionList.isEmpty()) {
                    continue;
                }

                Position position =
                        companyPositionList.get(random.nextInt(companyPositionList.size()));

                StudentApplication application = StudentApplication.builder()
                        .student(student)
                        .company(company)
                        .position(position)
                        .status(statuses[random.nextInt(statuses.length)])
                        .statusUpdatedAt(System.currentTimeMillis()
                                - random.nextInt(30) * 24L * 60 * 60 * 1000)
                        .createdAt(System.currentTimeMillis())
                        .build();

                applications.add(application);
            }
        }

        applicationRepository.saveAll(applications);

        System.out.println("Generated " + applications.size() + " applications.");
    }

    private void addMoreExperiences(List<Student> students,List<Company> companies){
        List<InterviewExperience> experiences = new ArrayList<>();

        DifficultyLevel[] difficultyLevels ={
                DifficultyLevel.EASY,
                DifficultyLevel.MEDIUM,
                DifficultyLevel.HARD,
                DifficultyLevel.EXPERT
        };

        InterviewResult[] results = {
                InterviewResult.PASSED,
                InterviewResult.FAILED,
                InterviewResult.WAITING_LIST,
                InterviewResult.PENDING
        };

        List<InterviewRoundConfig> configs = configRepository.findAll();

        Random random = new Random();

        for(int i = 0; i < 1000; i++){
            Student student = students.get(random.nextInt(students.size()));
            Company company = companies.get(random.nextInt(companies.size()));
            InterviewRoundConfig config = configs.isEmpty() ? null : configs.get(random.nextInt(configs.size()));

            InterviewExperience exp = InterviewExperience.builder()
//                    .id(UUID.randomUUID())
                    .student(student)
                    .company(company)
                    .interviewRoundConfig(config)
                    .dateExperienced(System.currentTimeMillis() - random.nextLong(60 * 24 * 24 * 1000))
                    .difficultyRating(difficultyLevels[random.nextInt(difficultyLevels.length)])
                    .experienceSummary("Great experience at "+ company.getName())
                    .totalProblemsAsked(random.nextInt(5)+1)
                    .questionsAsked("Arrays, HashMap, DP, Graphs")
                    .helpfulResources("LeetCode, GeeksFOrGeeks")
                    .result(results[random.nextInt(results.length)])
                    .upvotes(random.nextInt(50))
                    .downvotes(random.nextInt(10))
                    .createdAt(System.currentTimeMillis() - random.nextLong(60*24*60*1000))
                    .build();

            experiences.add(exp);
        }
        experienceRepository.saveAll(experiences);
        System.out.println("Added 1000 experiences");
    }
}
