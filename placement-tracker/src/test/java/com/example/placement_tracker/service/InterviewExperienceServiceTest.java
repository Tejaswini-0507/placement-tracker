package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.InterviewExperienceRequest;
import com.example.placement_tracker.dto.InterviewExperienceResponse;
import com.example.placement_tracker.entity.*;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.enums.InterviewRound;
import com.example.placement_tracker.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static net.bytebuddy.matcher.ElementMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@DisplayName("InterviewExperienceService Tests")
public class InterviewExperienceServiceTest {

    @Mock
    InterviewExperienceRepository experienceRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    InterviewRoundConfigRepository configRepository;

    @InjectMocks
    InterviewExperienceService interviewExperienceService;

    @Mock
    StudentApplicationRepository applicationRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    ExperienceSearchRepository experienceSearchRepository;

    private Student testStudent;
    private Company testCompany;
    private InterviewExperience testExperience;
    private InterviewRoundConfig testRound;
    private Position testPosition;
    private StudentApplication testApplication;
    private InterviewExperienceRequest request;


    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);

        testStudent = Student.builder()
                .id(UUID.randomUUID())
                .email("test@mail.com")
                .name("test")
                .branch("CSE")
                .batch(2023)
                .build();

        testCompany = Company.builder()
                .id(UUID.randomUUID())
                .name("Stripe")
                .build();

        testPosition = Position.builder()
                .id(UUID.randomUUID())
                .title("Software Developer")
                .build();

        testApplication = StudentApplication.builder()
                .id(UUID.randomUUID())
                .position(testPosition)
                .build();

        testRound = InterviewRoundConfig.builder()
                .id(UUID.randomUUID())
                .company(testCompany)
                .roundName("OA")
                .roundNumber(1)
                .build();

        ObjectMapper mapper = new ObjectMapper();

        ArrayNode topics = mapper.createArrayNode();
        topics.add("Arrays");
        topics.add("Strings");

        testExperience = InterviewExperience.builder()
                .id(UUID.randomUUID())
                .student(testStudent)
                .company(testCompany)
                .studentApplication(testApplication)
                .interviewRoundConfig(testRound)
                .dateExperienced(System.currentTimeMillis())
                .difficultyRating(DifficultyLevel.MEDIUM)
                .result(InterviewResult.PASSED)
                .topics(topics)
                .upvotes(5)
                .downvotes(0)
                .build();

        testExperience.setTopics(topics);


        request = InterviewExperienceRequest.builder()
                .companyId(testCompany.getId())
                .positionId(testPosition.getId())
                .interviewRoundConfigId(testRound.getId())
                .dateExperienced(testExperience.getDateExperienced())
                .difficultyRating(String.valueOf(testExperience.getDifficultyRating()))
                .result(testExperience.getResult().name())
                .topics("Arrays, Strings")
                .build();
    }

    @Test
    @DisplayName("Should create the interview experience")
    void createInterviewExperience(){

        System.out.println("Creating a experience");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testStudent.getEmail());

        SecurityContextHolder.setContext(securityContext);

        when(studentRepository.findByEmail(testStudent.getEmail()))
                .thenReturn(Optional.of(testStudent));

        when(companyRepository.findById(testCompany.getId()))
                .thenReturn(Optional.of(testCompany));

        when(applicationRepository.findByStudent_IdAndCompany_IdAndPosition_Id(testStudent.getId(),testCompany.getId(),testPosition.getId()))
                .thenReturn(Optional.of(testApplication));

        when(configRepository.findById(testRound.getId()))
                .thenReturn(Optional.of(testRound));

        when(experienceRepository.findByCompany_IdAndInterviewRoundConfig_Id(testCompany.getId(),testExperience.getInterviewRoundConfig().getId()))
                .thenReturn(List.of());

        when(experienceRepository.save(any(InterviewExperience.class)))
                .thenReturn(testExperience);

        InterviewExperienceResponse response = interviewExperienceService.createInterviewExperience(request);
        assertNotNull(response);
        assertEquals(response.getCompanyName(),testExperience.getCompany().getName());

        System.out.println("Created successfully");

    }

    @Test
    @DisplayName("Should upvote experience")
    void testUpvoteExperience(){
        System.out.println("Testing up votes");
        int initialVotes = testExperience.getUpvotes();
        when(experienceRepository.findById(testExperience.getId()))
                .thenReturn(Optional.of(testExperience));

        when(experienceRepository.save(testExperience)).thenReturn(testExperience);

        InterviewExperienceResponse response = interviewExperienceService.upvoteExperience(testExperience.getId());

        assertEquals(response.getUpvotes(),initialVotes + 1);

        System.out.println("Tested upvotes successfully");

    }

    @Test
    @DisplayName("Should downvote experience")
    void testDownvoteExperience(){
        System.out.println("Testing down votes");

        int initialDownvotes = testExperience.getDownvotes();
        when(experienceRepository.findById(testExperience.getId()))
                .thenReturn(Optional.of(testExperience));

        when(experienceRepository.save(testExperience)).thenReturn(testExperience);

        InterviewExperienceResponse response = interviewExperienceService.downvoteExperience(testExperience.getId());

        assertEquals(initialDownvotes + 1,response.getDownvotes());
        assertEquals(initialDownvotes + 1,testExperience.getDownvotes());

        System.out.println("Tested successfully");
    }



}
