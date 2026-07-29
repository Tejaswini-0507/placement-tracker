package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.StudentApplicationRequest;
import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.Position;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.entity.StudentApplication;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.repository.*;
import com.example.placement_tracker.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("StudentApplicationController Integration Tests")
public class StudentApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    PositionRepository positionRepository;

    @Autowired
    InterviewExperienceRepository experienceRepository;

    @Autowired
    DiscussionMessageRepository messageRepository;

    @Autowired
    DiscussionThreadRepository threadRepository;

    @Autowired
    StudentApplicationRepository applicationRepository;

    @Autowired
    InterviewRoundConfigRepository configRepository;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    private CompanyRepository companyRepository;

    private Student testStudent;
    private Company testCompany;
    private Position testPosition;
    private StudentApplication testApplication;
    private String jwtToken ;

    @BeforeEach
    void setUp(){
        messageRepository.deleteAll();
        threadRepository.deleteAll();
        experienceRepository.deleteAll();
        applicationRepository.deleteAll();
        configRepository.deleteAll();
        positionRepository.deleteAll();
        studentRepository.deleteAll();
        companyRepository.deleteAll();

        testStudent = Student.builder()
                .id(UUID.randomUUID())
                .email("tes@mail.com")
                .name("Test")
                .passwordHash("hashed-password")
                .branch("CSE")
                .batch(2026)
                .build();

        testStudent = studentRepository.save(testStudent);

        jwtToken = jwtUtil.generateToken(
                testStudent.getEmail(),
                testStudent.getId().toString()
        );

        testCompany = Company.builder()
                .id(UUID.randomUUID())
                .name("Google")
                .description("Great Company")
                .build();
        testCompany = companyRepository.save(testCompany);

        testPosition = Position.builder()
                .id(UUID.randomUUID())
                .title("SDE")
                .company(testCompany)
                .location("HYD")
                .build();

        testPosition = positionRepository.save(testPosition);

        testApplication = StudentApplication.builder()
                .id(UUID.randomUUID())
                .student(testStudent)
                .company(testCompany)
                .status(ApplicationStatus.APPLIED)
                .statusUpdatedAt(System.currentTimeMillis() - (3L* 24 * 60 * 60 * 1000))
                .position(testPosition)
                .build();

        testApplication = applicationRepository.save(testApplication);
    }

    @Test
    @DisplayName("Should create application successfully")
    void createApplication() throws Exception{
        StudentApplicationRequest request = StudentApplicationRequest.builder()
                .companyId(testCompany.getId())
                .positionTitle(testPosition.getTitle())
                .location("HYD")
                .status(String.valueOf(ApplicationStatus.APPLIED))
                .statusUpdatedAt(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000))
                .notes("Applies via portal")
                .build();

       mockMvc.perform(post("/student-application")
               .header("Authorization","Bearer "+ jwtToken)
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.positionTitle").value("SDE"))
               .andExpect(jsonPath("$.status").value("APPLIED"));

    }

    @Test
    @DisplayName("Should get Application By ID")
    void getApplicationById() throws Exception{
        StudentApplication application = StudentApplication.builder()
                .id(UUID.randomUUID())
                .student(testStudent)
                .company(testCompany)
                .status(ApplicationStatus.APPLIED)
                .statusUpdatedAt(System.currentTimeMillis() - (3L* 24 * 60 * 60 * 1000))
                .position(testPosition)
                .build();

        application = applicationRepository.save(application);

        mockMvc.perform(get("/student-application/{id}" ,application.getId())
                        .header("Authorization","Bearer "+ jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should get all my applications")
    void getAllMyApplications() throws Exception{
        mockMvc.perform(get("/student-application/my-applications")
                .header("Authorization", "Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should update the application")
    void updateApplication()throws  Exception{
        StudentApplicationRequest request = StudentApplicationRequest.builder()
                .companyId(testCompany.getId())
                .positionTitle(testPosition.getTitle())
                .location("HYD")
                .status(String.valueOf(ApplicationStatus.INTERVIEW_SCHEDULED))
                .statusUpdatedAt(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000))
                .notes("Applies via portal")
                .build();

        mockMvc.perform(put("/student-application/{id}",testApplication.getId())
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INTERVIEW_SCHEDULED"));
    }

}
