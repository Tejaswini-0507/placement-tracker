package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.DashboardStats;
import com.example.placement_tracker.dto.RecentApplicationResponse;
import com.example.placement_tracker.dto.RecentExperienceResponse;
import com.example.placement_tracker.dto.ResumeVersionResponse;
import com.example.placement_tracker.entity.InterviewExperience;
import com.example.placement_tracker.entity.ResumeVersion;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.entity.StudentApplication;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.repository.InterviewExperienceRepository;
import com.example.placement_tracker.repository.ResumeVersionRepository;
import com.example.placement_tracker.repository.StudentApplicationRepository;
import com.example.placement_tracker.repository.StudentRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Locale.filter;
import static org.antlr.v4.runtime.misc.Utils.count;


@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    StudentApplicationRepository applicationRepository;

    @Autowired
    InterviewExperienceRepository experienceRepository;

    @Autowired
    ResumeVersionRepository resumeRepository;

    @Transactional
    public DashboardStats getDashBoard(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<StudentApplication> applications = applicationRepository.findByStudent_Id(student.getId());
        List<InterviewExperience> experiences = experienceRepository.findByStudent_Id(student.getId());
        List<ResumeVersion> resumes = resumeRepository.findByStudent_Id(student.getId());

        //Count by status
        long offersCount = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.OFFER_RECEIVED ||
                        app.getStatus() == ApplicationStatus.OFFER_ACCEPTED ||
                        app.getStatus() == ApplicationStatus.OFFER_DECLINED ||
                        app.getStatus() == ApplicationStatus.JOINING_LETTER_RECEIVED)
                .count();

        long interviewsCount = applications.stream()
                .filter(app -> switch(app.getStatus()){
                    case INTERVIEW_SCHEDULED ,
                         INTERVIEW_COMPLETED,
                         RESULT_WAITING,
                         SELECTED,
                         OFFER_RECEIVED,
                         OFFER_ACCEPTED,
                         OFFER_DECLINED,
                         JOINING_LETTER_RECEIVED -> true;
                    default -> false;
                })
                .count();

        long rejectedCount = applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.REJECTED)
                .count();

        long appliedCount = applications.size();

        long completedApplications = offersCount + rejectedCount;

        double successRate = completedApplications == 0
                ? 0.0
                : (offersCount * 100.0) / completedApplications;

        // Recent applications (last 5)
        List<RecentApplicationResponse> recentApps = applications.stream()
                .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
                .limit(5)
                .map(this::applicationToRecent)
                .collect(Collectors.toList());

        // Recent experiences (last 5)
        List<RecentExperienceResponse> recentExp = experiences.stream()
                .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
                .limit(5)
                .map(this::experienceToRecent)
                .collect(Collectors.toList());

        // Resume versions
        List<ResumeVersionResponse> resumeList = resumes.stream()
                .sorted((a, b) -> Integer.compare(b.getVersionNumber(), a.getVersionNumber()))
                .map(this::resumeToResponse)
                .collect(Collectors.toList());

        return DashboardStats.builder()
                .totalApplications(applications.size())
                .offersCount((int) offersCount)
                .interviewsCount((int) interviewsCount)
                .rejectedCount((int) rejectedCount)
                .appliedCount((int) appliedCount)
                .totalExperiences(experiences.size())
                .totalResumeVersions(resumes.size())
                .recentApplications(recentApps)
                .recentExperiences(recentExp)
                .resumeVersions(resumeList)
                .applicationSuccessRate(Math.round(successRate * 100.0) / 100.0)
                .lastUpdated(System.currentTimeMillis())
                .build();
    }

    // Helper methods
    private RecentApplicationResponse applicationToRecent(StudentApplication app) {
        return RecentApplicationResponse.builder()
                .id(app.getId())
                .companyId(app.getCompany().getId())
                .companyName(app.getCompany().getName())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .statusUpdatedAt(app.getStatusUpdatedAt())
                .notes(app.getNotes())
                .build();
    }

    private RecentExperienceResponse experienceToRecent(InterviewExperience exp) {
        return RecentExperienceResponse.builder()
                .id(exp.getId())
                .companyId(exp.getCompany().getId())
                .companyName(exp.getCompany().getName())
                .interviewRound(exp.getInterviewRoundConfig().getRoundName())
                .difficultyLevel(exp.getDifficultyRating())
                .result(exp.getResult())
                .topics(exp.getTopics().toString())
                .upvotes(exp.getUpvotes())
                .downvotes(exp.getDownvotes())
                .createdAt(exp.getCreatedAt())
                .build();
    }

    private ResumeVersionResponse resumeToResponse(ResumeVersion resume) {
        return ResumeVersionResponse.builder()
                .id(resume.getId())
                .versionNumber(resume.getVersionNumber())
                .fileUrl(resume.getFileUrl())
                .fileSizeBytes(resume.getFileSizeBytes())
                .notes(resume.getNotes())
                .createdAt(resume.getCreatedAt())
                .isCurrent(true)
                .build();
    }



}
