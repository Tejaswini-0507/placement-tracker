package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.StudentApplicationRequest;
import com.example.placement_tracker.dto.StudentApplicationResponse;
import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.Position;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.entity.StudentApplication;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.repository.CompanyRepository;

import static org.mockito.ArgumentMatchers.any;
import com.example.placement_tracker.repository.PositionRepository;
import com.example.placement_tracker.repository.StudentApplicationRepository;
import com.example.placement_tracker.repository.StudentRepository;
import com.example.placement_tracker.service.impl.PositionServiceImpl;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@DisplayName("StudentApplicationService.class")
public class StudentApplicationServiceTest {

    @Mock
    StudentApplicationRepository applicationRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    CompanyRepository companyRepository;

    @Mock
    PositionRepository positionRepository;

    @Mock
    PositionService positionService;

    @InjectMocks
    StudentApplicationService applicationService;


    private Student testStudent;
    private Company testCompany;
    private Position testPosition;
    private StudentApplication testApplication;
    private StudentApplicationRequest request;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
        testStudent = Student.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .name("Test Student")
                .batch(2025)
                .branch("CSE")
                .build();

        testCompany = Company.builder()
                .id(UUID.randomUUID())
                .name("Google")
                .build();

        testPosition = Position.builder()
                .id(UUID.randomUUID())
                .company(testCompany)
                .title("Software Developer")
                .build();

        testApplication = StudentApplication.builder()
                .id(UUID.randomUUID())
                .student(testStudent)
                .company(testCompany)
                .position(testPosition)
                .status(ApplicationStatus.APPLIED)
                .statusUpdatedAt(System.currentTimeMillis())
                .build();

        request = StudentApplicationRequest.builder()
                .companyId(testCompany.getId())
                .status("APPLIED")
                .statusUpdatedAt(System.currentTimeMillis())
                .build();
    }

    @Test
    @DisplayName("Should create application successfully")
    void testCreateApplication(){
        System.out.println("Creating application");
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        SecurityContextHolder.setContext(securityContext);

        when(studentRepository.findByEmail(testStudent.getEmail()))
                .thenReturn(Optional.of(testStudent));

        when(companyRepository.findById(testCompany.getId()))
                .thenReturn(Optional.of(testCompany));

        when(applicationRepository.findByStudent_IdAndCompany_IdAndPosition_Id(
                testStudent.getId(),
                testCompany.getId(),
                testPosition.getId()))
                .thenReturn(Optional.empty());

        when(applicationRepository.save(any(StudentApplication.class))).thenReturn(testApplication);

        System.out.println("PositionService = " + positionService);

        when(positionService.getOrCreatePosition(
                any(Company.class),any(String.class),any(String.class)
        )).thenReturn(testPosition);

        System.out.println("Position = " + testPosition);

        StudentApplicationResponse response = applicationService.createApplication(request);

        assertNotNull(response);
        assertEquals(ApplicationStatus.APPLIED.toString(),String.valueOf(response.getStatus()));

        verify(applicationRepository,times(1)).save(any(StudentApplication.class));
        System.out.println("Application created successfully");
    }

    @Test
    @DisplayName("Should get application by ID")
    void testApplicationById(){
        System.out.println("Fetching application by id");
        when(applicationRepository.findById(testApplication.getId())).thenReturn(Optional.of(testApplication));

        StudentApplicationResponse response = applicationService.getApplicationById(testApplication.getId());

        assertNotNull(response);
        assertEquals(testApplication.getId(),response.getId());
        System.out.println("Fetched successfully");
    }

    @Test
    @DisplayName("Should throw application when not found")
    void testGetApplicationNotFound(){
        System.out.println("Throwing application if it is not found");
        when(applicationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                applicationService.getApplicationById(UUID.randomUUID())
        );
        System.out.println("Thrown successfully");
    }

    @Test
    @DisplayName("Should get application by ID")
    void testGetApplicationById(){
        System.out.println("Fetching application by it's ID");
        when(applicationRepository.findById(testApplication.getId())).thenReturn(Optional.of(testApplication));

        StudentApplicationResponse response = applicationService.getApplicationById(testApplication.getId());

        assertNotNull(response);
        assertEquals(testApplication.getId(), response.getId());
        System.out.println("Fetched application successfully");
    }

    @Test
    @DisplayName("Should get all applications by student")
    void testGetAllApplications(){
        System.out.println("Fetching all the applications");

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        SecurityContextHolder.setContext(securityContext);

        when(studentRepository.findByEmail(testStudent.getEmail()))
                .thenReturn(Optional.of(testStudent));


        List<StudentApplication> applications = Arrays.asList(testApplication);
        when(applicationRepository.findByStudent_Id(testStudent.getId())).thenReturn(applications);

        List<StudentApplicationResponse> responses = applicationService.getAllMyApplications();
        assertNotNull(responses);
        assertEquals(1,responses.size());
        assertEquals(testApplication.getId(),responses.get(0).getId());
        System.out.println("Fetched all applications");
    }

    @Test
    @DisplayName("Should update application")
    void testUpdateApplication(){
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        SecurityContextHolder.setContext(securityContext);

        when(studentRepository.findByEmail(testStudent.getEmail()))
                .thenReturn(Optional.of(testStudent));

        when(companyRepository.findById(testCompany.getId()))
                .thenReturn(Optional.of(testCompany));

        when(applicationRepository.findByStudent_IdAndCompany_IdAndPosition_Id(
                testStudent.getId(),
                testCompany.getId(),
                testPosition.getId()))
                .thenReturn(Optional.empty());

        when(positionService.getOrCreatePosition(
                any(Company.class),any(String.class),any(String.class)
        )).thenReturn(testPosition);

        when(applicationRepository.save(any(StudentApplication.class))).thenReturn(testApplication);

        request.setStatus("INTERVIEW_COMPLETED");
        StudentApplicationResponse response = applicationService.createApplication(request);

        assertNotNull(response);
        assertNotEquals(ApplicationStatus.APPLIED.toString(),String.valueOf(response.getStatus()));

        verify(applicationRepository,times(1)).save(any(StudentApplication.class));
        System.out.println("Application updated successfully");
    }

    @Test
    @DisplayName("Should get application by Position")
    void getApplicationsByPosition(){

        when(positionRepository.findByTitle(testPosition.getTitle()))
                .thenReturn(Optional.of(testPosition));

        when(applicationRepository.findByPosition_Title(testPosition.getTitle()))
                .thenReturn(List.of(testApplication));

        List<StudentApplicationResponse> responses = applicationService.getPositionApplications(testPosition.getTitle());
        assertNotNull(responses);
        assertEquals(1,responses.size());
        assertEquals(testApplication.getId(),responses.get(0).getId());
        System.out.println("Fetched all applications");

    }

}
