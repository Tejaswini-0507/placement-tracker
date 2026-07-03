package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.AuthResponse;
import com.example.placement_tracker.dto.LoginRequest;
import com.example.placement_tracker.dto.RegistrationRequest;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.repository.StudentRepository;
import com.example.placement_tracker.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;


    public AuthResponse register(RegistrationRequest request){

        if(studentRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email already registered");
        }

        if(request.getPassword().length() < 6){
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        String passwordHash = passwordEncoder.encode(request.getPassword());

        Student student = Student.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail())
                .name(request.getName())
                .passwordHash(passwordHash)
                .branch(request.getBranch())
                .batch(request.getBatch())
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        student = studentRepository.save(student);

        String token = jwtUtil.generateToken(student.getEmail(), student.getId().toString());

        return AuthResponse.builder()
                .token(token)
                .studentId(student.getId().toString())
                .email(student.getEmail())
                .name(student.getName())
                .branch(student.getBranch())
                .batch(student.getBatch())
                .expiresIn(86400L)
                .message("Registration successful")
                .build();

    }

    public AuthResponse login(LoginRequest request){
        Student student = studentRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), student.getPasswordHash())){
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(student.getEmail(),student.getId().toString());

        return AuthResponse.builder()
                .token(token)
                .studentId(student.getId().toString())
                .email(student.getEmail())
                .name(student.getName())
                .branch(student.getBranch())
                .batch(student.getBatch())
                .expiresIn(86400L)
                .message("Login successful")
                .build();
    }
}
