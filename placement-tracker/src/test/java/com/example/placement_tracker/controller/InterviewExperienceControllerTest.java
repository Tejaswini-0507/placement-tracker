package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.InterviewExperienceRequest;
import com.example.placement_tracker.entity.*;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.repository.*;
import com.example.placement_tracker.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("InterviewExperienceControllerTest")
public class InterviewExperienceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    InterviewExperienceRepository experienceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private StudentApplicationRepository applicationRepository;

    @Autowired
    private InterviewRoundConfigRepository configRepository;

    private Student testStudent;
    private Company testCompany;
    private Position testPosition;
    private String jwtToken;
    private InterviewExperience testExperience;
    private StudentApplication testApplication;
    private InterviewRoundConfig testRound;

    @BeforeEach()
    void setUp(){
        MockitoAnnotations.openMocks(this);
        experienceRepository.deleteAll();
        applicationRepository.deleteAll();
        configRepository.deleteAll();
        positionRepository.deleteAll();
        studentRepository.deleteAll();
        companyRepository.deleteAll();

        testStudent = Student.builder()
                .id(UUID.randomUUID())
                .email("test@mail.com")
                .name("test")
                .passwordHash("hash_password")
                .batch(2023)
                .branch("CSE")
                .build();

        testStudent = studentRepository.save(testStudent);

        jwtToken = jwtUtil.generateToken(
                testStudent.getEmail(),
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

        testApplication = StudentApplication.builder()
                .id(UUID.randomUUID())
                .student(testStudent)
                .company(testCompany)
                .status(ApplicationStatus.APPLIED)
                .position(testPosition)
                .statusUpdatedAt(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000))
                .build();

        testApplication = applicationRepository.save(testApplication);

        testRound = InterviewRoundConfig.builder()
                .id(UUID.randomUUID())
                .company(testCompany)
                .roundName("OA")
                .roundNumber(1)
                .build();

        testRound = configRepository.save(testRound);

        testExperience = InterviewExperience.builder()
                .student(testStudent)
                .company(testCompany)
                .dateExperienced(System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000))
                .difficultyRating(DifficultyLevel.MEDIUM)
                .studentApplication(testApplication)
                .interviewRoundConfig(testRound)
                .upvotes(5)
                .downvotes(6)
                .build();

        testExperience = experienceRepository.save(testExperience);
    }

    @Test
    @DisplayName("Should create experience")
    void createExperience() throws Exception{

        InterviewExperienceRequest request = InterviewExperienceRequest.builder()
                .companyId(testCompany.getId())
                .positionId(testPosition.getId())
                .interviewRoundConfigId(testRound.getId())
                .dateExperienced(System.currentTimeMillis() -  (3L * 24 * 60 * 60 * 1000))
                .difficultyRating(String.valueOf(DifficultyLevel.MEDIUM))
                .questionsAsked("HashMap, Strings, DP, System Design")
                .experienceSummary("Overall it was easy but make sure you revise DP & System design")
                .result(String.valueOf(InterviewResult.WAITING_LIST))
                .build();

        mockMvc.perform(post("/experience")
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.difficultyRating").value("MEDIUM"))
                .andExpect(jsonPath("$.interviewRoundConfigId").value(testRound.getId().toString()));
    }

    @Test
    @DisplayName("Should get all my experiences")
    void getMyExperiences() throws Exception{

        mockMvc.perform(get("/experience/my-experiences")
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Should get experinces by Id")
    void getExperienceById() throws Exception{

        mockMvc.perform(get("/experience/{id}",testExperience.getId())
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Should get experiences by company")
    void getExperienceByCompanyId() throws Exception{
        mockMvc.perform(get("/experience/company/{companyId}",testCompany.getId())
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should update Experience successfully")
    void updateExperience() throws Exception{
        InterviewExperienceRequest request = InterviewExperienceRequest.builder()
                .companyId(testCompany.getId())
                .positionId(testPosition.getId())
                .interviewRoundConfigId(testRound.getId())
                .dateExperienced(System.currentTimeMillis() -  (3L * 24 * 60 * 60 * 1000))
                .difficultyRating(String.valueOf(DifficultyLevel.MEDIUM))
                .questionsAsked("HashMap, Strings, DP, System Design")
                .experienceSummary("Overall it was easy but make sure you revise DP & System design")
                .result(String.valueOf(InterviewResult.FAILED))
                .build();

        mockMvc.perform(put("/experience/{id}",testExperience.getId())
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value(String.valueOf(InterviewResult.FAILED)))
                .andExpect(jsonPath("$.difficultyRating").value(String.valueOf(DifficultyLevel.MEDIUM)));
    }

    @Test
    @DisplayName("Should update upvotes")
    void updateUpvotes() throws Exception{
        mockMvc.perform(post("/experience/upvote/{id}",testExperience.getId())
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(6));
    }

    @Test
    @DisplayName("Should update downvotes successfully")
    void updateDownVotes() throws Exception{
        mockMvc.perform(post("/experience/downvote/{id}",testExperience.getId())
                        .header("Authorization","Bearer "+jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downvotes").value(7));

    }

}
