package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.MyApplicationResponse;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.entity.StudentApplication;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.repository.StudentApplicationRepository;
import com.example.placement_tracker.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MyApplicationsService {

    private static final Logger logger = LoggerFactory.getLogger(MyApplicationsService.class);

    @Autowired
    private StudentApplicationRepository applicationRepository;

    @Autowired
    private StudentRepository studentRepository;

    // GET MY APPLICATIONS
    @Transactional
    public MyApplicationResponse getMyApplications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<StudentApplication> applications = applicationRepository.findByStudent_Id(student.getId());

        // Group by status
        List<MyApplicationResponse.ApplicationDetailResponse> applied = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPLIED)
                .map(this::toDetailResponse)
                .collect(Collectors.toList());


        List<MyApplicationResponse.ApplicationDetailResponse> interviews = applications.stream()
                .filter(app -> switch (app.getStatus()) {
                    case INTERVIEW_SCHEDULED,
                         INTERVIEW_COMPLETED,
                         RESULT_WAITING,
                         SELECTED,
                         OFFER_RECEIVED,
                         OFFER_ACCEPTED,
                         OFFER_DECLINED,
                         JOINING_LETTER_RECEIVED -> true;
                    default -> false;
                })
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        List<MyApplicationResponse.ApplicationDetailResponse> offers = applications.stream()
                .filter(app ->
                        app.getStatus() == ApplicationStatus.OFFER_RECEIVED ||
                                app.getStatus() == ApplicationStatus.OFFER_ACCEPTED ||
                                app.getStatus() == ApplicationStatus.OFFER_DECLINED ||
                                app.getStatus() == ApplicationStatus.JOINING_LETTER_RECEIVED)
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        List<MyApplicationResponse.ApplicationDetailResponse> rejected = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.REJECTED)
                .map(this::toDetailResponse)
                .collect(Collectors.toList());

        // Count by status
        Map<String, Integer> statusCount = new HashMap<>();
        statusCount.put("APPLIED", applied.size());
        statusCount.put("INTERVIEW", interviews.size());
        statusCount.put("OFFER", offers.size());
        statusCount.put("REJECTED", rejected.size());

        int completedApplications = offers.size() + rejected.size();

        // Success rate
        double successRate = completedApplications == 0
                ? 0.0
                : (offers.size() * 100.0) / completedApplications;

        return MyApplicationResponse.builder()
                .totalApplications(applications.size())
                .appliedApplications(applied)
                .interviewApplications(interviews)
                .offerApplications(offers)
                .rejectedApplications(rejected)
                .statusCount(statusCount)
                .successRate(Math.round(successRate * 100.0) / 100.0)
                .build();
    }

    // Helper
    private MyApplicationResponse.ApplicationDetailResponse toDetailResponse(StudentApplication app) {
        return MyApplicationResponse.ApplicationDetailResponse.builder()
                .companyName(app.getCompany().getName())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .statusUpdatedAt(app.getStatusUpdatedAt())
                .notes(app.getNotes())
                .build();
    }
}
