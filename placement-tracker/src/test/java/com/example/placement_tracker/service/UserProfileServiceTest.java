package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.UserProfileRequest;
import com.example.placement_tracker.dto.UserProfileResponse;
import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.repository.CompanyRepository;
import com.example.placement_tracker.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

public class UserProfileServiceTest {

    @Mock
    StudentRepository studentRepository;


    @InjectMocks
    UserProfileService userProfileService;

    private Student testStudent;
    UserProfileRequest request;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        testStudent = Student.builder()
                .id(UUID.randomUUID())
                .email("test@mail.com")
                .name("test")
                .branch("CSE")
                .batch(2023)
                .build();

        request = UserProfileRequest.builder()
                .name("test")
                .email("test@mail.com")
                .bio("Passionate about backend developing and interested in learning and exploring new technologies")
                .build();


    }

    @Test
    @DisplayName("Get My Profile")
    void getMyProfile(){
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@mail.com");

        SecurityContextHolder.setContext(securityContext);

        when(studentRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(testStudent));

        UserProfileResponse response = userProfileService.getMyProfile();

        assertNotNull(response);
        assertEquals("test@mail.com",response.getEmail());
        assertEquals("test",response.getName());

    }

    @Test
    @DisplayName("Should get profile by ID")
    void getProfileById(){
        when(studentRepository.findById(testStudent.getId())).thenReturn(Optional.of(testStudent));

        UserProfileResponse response = userProfileService.getProfileById(testStudent.getId().toString());

        assertNotNull(response);
        assertEquals("test@mail.com",response.getEmail());
        assertEquals(testStudent.getId(),response.getId());
    }

    @Test
    @DisplayName("Should update profile")
    void updateProfile(){
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@mail.com");

        SecurityContextHolder.setContext(securityContext);

        when(studentRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(testStudent));

        when(studentRepository.save(any(Student.class))).thenReturn(testStudent);

        UserProfileResponse response = userProfileService.updateMyProfile(request);

        assertNotNull(response);
        assertEquals(request.getBio(),response.getBio());
    }
}
