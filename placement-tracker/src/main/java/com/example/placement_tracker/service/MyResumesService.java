package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.MyResumeResponse;
import com.example.placement_tracker.entity.ResumeVersion;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.repository.ResumeVersionRepository;
import com.example.placement_tracker.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MyResumesService {
    private static final Logger logger = LoggerFactory.getLogger(MyResumesService.class);

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    ResumeVersionRepository resumeVersionRepository;

    @Transactional
    public MyResumeResponse getMyResumes(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findByEmail(email)
                        .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<ResumeVersion> resumes = resumeVersionRepository.findByStudent_Id(student.getId());

        List<MyResumeResponse.ResumeDetailResponse> resumeList = resumes.stream()
                .sorted((a,b) -> Integer.compare(b.getVersionNumber(),a.getVersionNumber()))
                .map(this :: toDetailResponse)
                .collect(Collectors.toList());

        return MyResumeResponse.builder()
                .totalVersions(resumes.size())
                .resumes(resumeList)
                .build();
    }

    private MyResumeResponse.ResumeDetailResponse toDetailResponse(ResumeVersion resume) {
        return MyResumeResponse.ResumeDetailResponse.builder()
                .versionNumber(resume.getVersionNumber())
                .fileUrl(resume.getFileUrl())
                .fileSizeBytes(resume.getFileSizeBytes())
                .notes(resume.getNotes())
                .createdAt(resume.getCreatedAt())
                .isCurrent(true)
                .build();
    }
}
