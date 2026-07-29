package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.UserProfileRequest;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.repository.StudentRepository;
import com.example.placement_tracker.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.parameters.P;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UserProfileControllerTest")
public class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    StudentRepository studentRepository;

    private Student testStudent;
    private String jwtToken;


    @BeforeEach
    void setUp(){
        studentRepository.deleteAll();

        testStudent = Student.builder()
                .id(UUID.randomUUID())
                .email("test@mail.com")
                .name("Test")
                .branch("CSE")
                .passwordHash("hash_password")
                .batch(2026)
                .build();

        testStudent = studentRepository.save(testStudent);

        jwtToken = jwtUtil.generateToken(
                testStudent.getEmail(),
                testStudent.getId().toString()
        );
    }

    @Test
    @DisplayName("Should get my profile successfully")
    void testGetMyProfile() throws Exception{

        mockMvc.perform(get("/profile/me")
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    @DisplayName("Should get profile by Id")
    void testGetProfileById() throws Exception{

        mockMvc.perform(get("/profile/{studentId}",testStudent.getId())
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.com"))
                .andExpect(jsonPath("$.name").value("Test"));
    }

    @Test
    @DisplayName("Should update profile successfully")
    void updateProfile() throws Exception{
        UserProfileRequest request = UserProfileRequest.builder()
                .githubUrl("xyz.com")
                .linkedinUrl("abc.com")
                .phoneNumber("8522896826")
                .bio("Passionate about backend development")
                .build();

        mockMvc.perform(put("/profile/me")
                .header("Authorization","Bearer "+jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.githubUrl").value("xyz.com"));
    }



}
