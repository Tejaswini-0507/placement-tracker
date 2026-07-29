package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.DashboardStats;
import com.example.placement_tracker.entity.*;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewRound;
import com.example.placement_tracker.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DashboardServiceTest {

    @Mock
    StudentRepository studentRepository;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    StudentApplicationRepository applicationRepository;

    @Mock
    InterviewExperienceRepository experienceRepository;

    @Mock
    ResumeVersionRepository resumeVersionRepository;


    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    DashboardService dashboardService;

    private Student testStudent;
    private Company testCompany;
    private InterviewExperience testExperience;
    private StudentApplication testApplication;
    private ResumeVersion testResumeVersion;
    private DashboardStats testStats;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);

        testStudent = Student.builder()
                .id(UUID.randomUUID())
                .email("test@mail.com")
                .name("test")
                .build();

        testCompany = Company.builder()
                .id(UUID.randomUUID())
                .name("Google")
                .build();
    }

    @Test
    @DisplayName("Should get the Dashboard")
    void getDashboard(){

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testStudent.getEmail());

        SecurityContextHolder.setContext(securityContext);

        when(studentRepository.findByEmail(testStudent.getEmail()))
                .thenReturn(Optional.of(testStudent));
        when(applicationRepository.findByStudent_Id(testStudent.getId()))
                .thenReturn(Collections.emptyList());
        when(experienceRepository.findByStudent_Id(testStudent.getId()))
                .thenReturn(Collections.emptyList());
        when(resumeVersionRepository.findByStudent_Id(testStudent.getId()))
                .thenReturn(Collections.emptyList());

        DashboardStats stats = dashboardService.getDashBoard();

        assertNotNull(stats);
        assertEquals(0,stats.getTotalApplications());
        assertEquals(0,stats.getTotalExperiences());
        assertEquals(0,stats.getTotalResumeVersions());

    }

    @Test
    @DisplayName("Should calculate application success rate correctly")
    void testApplicationSuccessRate(){

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testStudent.getEmail());

        SecurityContextHolder.setContext(securityContext);


        StudentApplication app1 = StudentApplication.builder()
                .company(testCompany)
                .status(ApplicationStatus.OFFER_RECEIVED)
                .createdAt(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
                .build();

        StudentApplication app2 = StudentApplication.builder()
                .company(testCompany)
                .status(ApplicationStatus.REJECTED)
                .createdAt(System.currentTimeMillis() - 12L * 24 * 60 * 60 * 1000)
                .build();

        when(studentRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(testStudent));
        when(applicationRepository.findByStudent_Id(testStudent.getId()))
                .thenReturn(Arrays.asList(app1, app2));
        when(experienceRepository.findByStudent_Id(testStudent.getId()))
                .thenReturn(Collections.emptyList());
        when(resumeVersionRepository.findByStudent_Id(testStudent.getId()))
                .thenReturn(Collections.emptyList());

        DashboardStats stats = dashboardService.getDashBoard();

        assertNotNull(stats);
        assertEquals(2,stats.getTotalApplications());
        assertEquals(1,stats.getOffersCount());
        assertEquals(50.0,stats.getApplicationSuccessRate());
    }

}
