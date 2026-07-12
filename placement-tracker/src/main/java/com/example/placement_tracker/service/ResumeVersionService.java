package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.ResumeResponse;
import com.example.placement_tracker.entity.ResumeVersion;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.repository.ResumeVersionRepository;
import com.example.placement_tracker.repository.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResumeVersionService {
    @Autowired
    ResumeVersionRepository resumeVersionRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${app.upload.resume-dir:uploads/resumes}")
    private String resumeDir;

    public ResumeResponse uploadResume(MultipartFile file, Integer versionNumber, String notes, String usedForCompanies) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFileName = file.getOriginalFilename();

        System.out.println("Original File Name: " + originalFileName);
        System.out.println("Content Type: " + file.getContentType());

        if(originalFileName == null){
            throw new IllegalArgumentException("Invalid File");
        }

        String lowerCaseName = originalFileName.toLowerCase();

        if (!(lowerCaseName.endsWith(".pdf") || lowerCaseName.endsWith(".docx"))) {
            throw new IllegalArgumentException("Only .pdf and .docx files are allowed");
        }

        if(file.getSize() > 5 * 1024 * 1024){
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        if (versionNumber == null || versionNumber <= 0) {
            throw new IllegalArgumentException("Version number must be positive");
        }

        List<ResumeVersion> existingVersions = resumeVersionRepository.findByStudent_Id(student.getId());
        if (existingVersions.stream()
                .anyMatch(v -> v.getVersionNumber().equals(versionNumber))) {
            throw new IllegalArgumentException(
                    "Resume version " + versionNumber + " already exists for this student"
            );
        }

        File dir = new File(resumeDir + "/" + student.getId());
        if(!dir.exists()){
            dir.mkdirs();
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String filePath = resumeDir + "/" + student.getId() + "/" + fileName;
        String fileUrl = filePath;
        Files.write(Paths.get(filePath),file.getBytes());

        JsonNode usedForCompaniesNode = null;
        if (usedForCompanies != null && !usedForCompanies.isEmpty()) {
            try {
                usedForCompaniesNode = objectMapper.readTree(usedForCompanies);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid JSON for usedForCompanies: " + e.getMessage());
            }
        }

        ResumeVersion resume = ResumeVersion.builder()
                .id(UUID.randomUUID())
                .student(student)
                .versionNumber(versionNumber)
                .fileUrl(filePath)
                .fileSizeBytes(file.getSize())
                .notes(notes)
                .usedForCompanies(usedForCompaniesNode)
                .build();

        resume = resumeVersionRepository.save(resume);
        return entityToResponse(resume);
    }

    public List<ResumeResponse> getMyResumes(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return resumeVersionRepository.findByStudent_Id(student.getId())
                .stream()
                .map(this :: entityToResponse)
                .collect(Collectors.toList());
    }

    public ResumeResponse getResumeById(UUID id){
        ResumeVersion resume = resumeVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        return entityToResponse(resume);
    }

    public ResumeResponse getResumeVersion(Integer versionNumber){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        ResumeVersion resumeVersion = resumeVersionRepository.findByStudent_IdAndVersionNumber(
                student.getId(),versionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Resume version" + versionNumber + "not found"));

        return entityToResponse(resumeVersion);
    }

    public void deleteResumeVersion(UUID id) throws IOException{
        ResumeVersion resume = resumeVersionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Student currentStudent = studentRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("Student not found"));

        if(!resume.getStudent().getId().equals(currentStudent.getId())){
            throw new IllegalArgumentException("Unautorized: Cannot delete another student's resume");
        }
        Files.deleteIfExists(Paths.get(resume.getFileUrl()));
        resumeVersionRepository.deleteById(id);
    }

    private ResumeResponse entityToResponse(ResumeVersion resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .studentId(resume.getStudent().getId())
                .versionNumber(resume.getVersionNumber())
                .fileUrl(resume.getFileUrl())
                .fileSizeBytes(resume.getFileSizeBytes())
                .notes(resume.getNotes())
                .usedForCompanies(resume.getUsedForCompanies() != null ?
                        resume.getUsedForCompanies().toString() : null)
                .build();
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}
