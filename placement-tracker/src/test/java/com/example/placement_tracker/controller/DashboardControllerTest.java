package com.example.placement_tracker.controller;

import com.example.placement_tracker.entity.*;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.repository.*;
import com.example.placement_tracker.service.DashboardService;
import com.example.placement_tracker.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    PositionRepository positionRepository;

    @Autowired
    InterviewRoundConfigRepository configRepository;

    @Autowired
    StudentApplicationRepository applicationRepository;

    @Autowired
    InterviewExperienceRepository experienceRepository;

    @Autowired
    ResumeVersionRepository resumeRepository;

    @Autowired
    JwtUtil jwtUtil;

    @InjectMocks
    DashboardService dashboardService;

    private Student testStudent;
    private Company testCompany;
    private Position testPosition;
    private InterviewRoundConfig testRound;
    private StudentApplication testApplication;
    private InterviewExperience testExperience;
    private ResumeVersion testResume;
    private String jwtToken;

    @BeforeEach
    void setup(){

        experienceRepository.deleteAll();
        applicationRepository.deleteAll();
        resumeRepository.deleteAll();
        configRepository.deleteAll();
        positionRepository.deleteAll();

        companyRepository.deleteAll();
        studentRepository.deleteAll();


        testStudent = Student.builder()
                .id(UUID.randomUUID())
                .email("test@mail.com")
                .name("test")
                .passwordHash("hash_password")
                .branch("CSE")
                .batch(2024)
                .build();

        testStudent = studentRepository.save(testStudent);

        jwtToken = jwtUtil.generateToken(testStudent.getEmail(),
                testStudent.getId().toString());

        testCompany = Company.builder()
                .id(UUID.randomUUID())
                .name("Google")
                .build();

        testCompany = companyRepository.save(testCompany);

        testPosition = Position.builder()
                .id(UUID.randomUUID())
                .company(testCompany)
                .title("SDE")
                .location("HYD")
                .build();

        testPosition = positionRepository.save(testPosition);

        testRound = InterviewRoundConfig.builder()
                .id(UUID.randomUUID())
                .company(testCompany)
                .roundName("OA")
                .roundNumber(1)
                .build();

        testRound = configRepository.save(testRound);

        testApplication = StudentApplication.builder()
                .id(UUID.randomUUID())
                .student(testStudent)
                .company(testCompany)
                .position(testPosition)
                .status(ApplicationStatus.APPLIED)
                .statusUpdatedAt(System.currentTimeMillis() -(4L * 24 * 60 * 60 * 1000))
                .build();

        testApplication = applicationRepository.save(testApplication);


        testExperience = InterviewExperience.builder()
                .id(UUID.randomUUID())
                .student(testStudent)
                .company(testCompany)
                .studentApplication(testApplication)
                .interviewRoundConfig(testRound)
                .difficultyRating(DifficultyLevel.MEDIUM)
                .roundNumber(testRound.getRoundNumber())
                .dateExperienced(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000))
                .build();

        testExperience = experienceRepository.save(testExperience);

        testResume = ResumeVersion.builder()
                .id(UUID.randomUUID())
                .student(testStudent)
                .versionNumber(1)
                .fileUrl("file:///C:/Users/Tejaswini/OneDrive/Desktop/Tejaswini/Appana%20Sri%20Sai%20Jaya%20Tejaswini%20-%20Resume.pdf")
                .build();

        testResume = resumeRepository.save(testResume);

    }

    @Test
    @DisplayName("Should get the dashboard")
    void getDashboard()throws Exception{

        mockMvc.perform(get("/dashboard")
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }




}
