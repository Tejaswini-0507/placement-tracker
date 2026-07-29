package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.*;
import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.Position;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.entity.StudentApplication;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.repository.CompanyRepository;
import com.example.placement_tracker.repository.PositionRepository;
import com.example.placement_tracker.repository.StudentApplicationRepository;
import com.example.placement_tracker.repository.StudentRepository;
import org.springframework.security.core.Authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentApplicationService {

    @Autowired
    StudentApplicationRepository studentApplicationRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    PositionService positionService;

    @Autowired
    PositionRepository positionRepository;

    public StudentApplicationResponse createApplication(StudentApplicationRequest request){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        //Check if student exists
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(()-> new IllegalArgumentException("Student not found"));

        //Check if company exists
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(()-> new IllegalArgumentException("Company not found"));



        Position position = positionService.getOrCreatePosition(
                company,
                request.getPositionTitle(),
                request.getLocation()
        );


        //Check if student already applied
        if(studentApplicationRepository
            .findByStudent_IdAndCompany_IdAndPosition_Id(student.getId(),company.getId(),position.getId())
            .isPresent()){
            throw new IllegalArgumentException("You have already applied for this company at the selected company");
        }

        StudentApplication application = StudentApplication.builder()
                .student(student)
                .company(company)
                .position(position)
                .status(ApplicationStatus.valueOf(request.getStatus()))
                .statusUpdatedAt(request.getStatusUpdatedAt())
                .oaScheduledDate(request.getOaScheduledDate())
                .oaCompletedDate(request.getOaCompletedDate())
                .interviewScheduledDate(request.getInterviewScheduledDate())
                .interviewCompletedDate(request.getInterviewCompletedDate())
                .resultReceivedDate(request.getResultReceivedDate())
                .offerAccepted(request.getOfferAccepted())
                .notes(request.getNotes())
                .build();

        studentApplicationRepository.save(application);
        return entityToResponse(application);
    }

    //GET APPLICATIONS BY ID
    public StudentApplicationResponse getApplicationById(UUID id){
        StudentApplication studentApplication = studentApplicationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Company not found: "+ id));
        return entityToResponse(studentApplication);
    }

    //GET ALL MY APPLICATIONS
    public List<StudentApplicationResponse> getAllMyApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        return studentApplicationRepository.findByStudent_Id(student.getId())
                .stream()
                .map(this::entityToResponse)
                .collect(Collectors.toList());
    }

    //GET COMPANY APPLICATIONS

    public List<StudentApplicationResponse> getCompanyApplications(UUID companyId){
        if(!companyRepository.existsById(companyId)){
            throw new IllegalArgumentException(
                    "Company not found"
            );
        }

        return studentApplicationRepository.findByCompany_Id(companyId)
                .stream()
                .map(this :: entityToResponse)
                .collect(Collectors.toList());
    }

    //GET POSITION APPLICATIONS
    public List<StudentApplicationResponse> getPositionApplications(String positionTitle){
        positionRepository.findByTitle(positionTitle)
                .orElseThrow(()-> new IllegalArgumentException("Position not found"));

        return studentApplicationRepository.findByPosition_Title(positionTitle)
                .stream().map(this::entityToResponse).collect(Collectors.toList());

    }

    // UPDATE
    @Transactional
    public StudentApplicationResponse updateApplication(UUID applicationId, StudentApplicationUpdateRequest request) {

        StudentApplication application = studentApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found"));

        // Verify student owns this application
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Student currentStudent = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (!application.getStudent().getId().equals(currentStudent.getId())) {
            throw new IllegalArgumentException("Unauthorized: Cannot update another student's application");
        }

        // Update fields
        application.setStatus(ApplicationStatus.valueOf(request.getStatus()));
        application.setOaScheduledDate(request.getOaScheduledDate());
        application.setOaCompletedDate(request.getOaCompletedDate());
        application.setInterviewScheduledDate(request.getInterviewScheduledDate());
        application.setInterviewCompletedDate(request.getInterviewCompletedDate());
        application.setResultReceivedDate(request.getResultReceivedDate());
        application.setOfferAccepted(request.getOfferAccepted());
        application.setNotes(request.getNotes());

        // Save and return
        application = studentApplicationRepository.save(application);
        return entityToResponse(application);
    }


    //HELPER
    @Transactional
    public StudentApplicationResponse entityToResponse(StudentApplication application){
        ApplicationStatus status = application.getStatus();
        return StudentApplicationResponse.builder()
                .id(application.getId())
                .studentId(application.getStudent().getId())
                .studentName(application.getStudent().getName())
                .companyId(application.getCompany().getId())
                .companyName(application.getCompany().getName())
                .positionTitle(application.getPosition().getTitle())
                .status(String.valueOf(status))
                .statusUpdatedAt(application.getStatusUpdatedAt())
                .oaScheduledDate(application.getOaScheduledDate())
                .oaCompletedDate(application.getOaCompletedDate())
                .interviewScheduledDate(application.getInterviewScheduledDate())
                .interviewCompletedDate(application.getInterviewCompletedDate())
                .resultReceivedDate(application.getResultReceivedDate())
                .offerAccepted(application.getOfferAccepted())
                .notes(application.getNotes())
                 .createdAt(application.getCreatedAt())
                 .updatedAt(application.getUpdatedAt())
                .build();

    }
}
