package com.example.placement_tracker.controller;


import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.dto.StudentApplicationRequest;
import com.example.placement_tracker.dto.StudentApplicationResponse;
import com.example.placement_tracker.dto.StudentApplicationUpdateRequest;
import com.example.placement_tracker.enums.ApplicationStatus;
import com.example.placement_tracker.service.StudentApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/student-application")
@CrossOrigin(origins = "*")
public class StudentApplicationController {

    @Autowired
    StudentApplicationService studentApplicationService;

    //CREATE APPLICATION
    @PostMapping
    public ResponseEntity<?> createApplication(@Valid @RequestBody StudentApplicationRequest request){
        try{
            StudentApplicationResponse response = studentApplicationService.createApplication(request);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(new ErrorResponse("VALIDATION_ERROR",e.getMessage(),System.currentTimeMillis()));
        }catch (Exception e){
            return ResponseEntity.status(500).body(new ErrorResponse("INTERNAL_ERROR","Failed to create application",System.currentTimeMillis()));
        }

    }

    //READ ONE
    @Transactional
    @GetMapping("/{id}")
    public ResponseEntity<?> getApplicationById(@PathVariable UUID id){
        try {
            StudentApplicationResponse studentApplicationResponse = studentApplicationService.getApplicationById(id);
            return ResponseEntity.ok(studentApplicationResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis()));
        }catch (Exception e){
            return ResponseEntity.status(500).body(new ErrorResponse("INTERNAL_ERROR",e.getMessage(),System.currentTimeMillis()));
        }
    }


    //READ ALL
    @Transactional
    @GetMapping("/my-applications")
    public ResponseEntity<?> getMyApplications(){
        try {
            List<StudentApplicationResponse> applicationResponses = studentApplicationService.getAllMyApplications();
            return ResponseEntity.ok(applicationResponses);
        }catch (IllegalArgumentException e){
            String message = e.getMessage();
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR",message,System.currentTimeMillis()));
        }
    }

    //READ COMPANY APPLICATIONS
    @GetMapping("/company-application/{companyId}")
    public ResponseEntity<?> getCompanyApplications(@PathVariable UUID companyId){
        try{
            List<StudentApplicationResponse> applicationResponses = studentApplicationService.getCompanyApplications(companyId);
            return ResponseEntity.ok(applicationResponses);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis()));

        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR","Failed to fetch applications",System.currentTimeMillis()));
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<?> updateApplication(
            @PathVariable UUID id,
            @Valid @RequestBody StudentApplicationUpdateRequest request
    ) {
        try {
            StudentApplicationResponse response = studentApplicationService.updateApplication(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to update application",System.currentTimeMillis()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable UUID id){
        try{
            studentApplicationService.deleteApplication(id);
            return ResponseEntity.ok().body(new ErrorResponse("SUCCESS","Application deleted successfully",System.currentTimeMillis()));
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND","Application does not exist",System.currentTimeMillis()));
        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR","Failed to delete application",System.currentTimeMillis()));
        }
    }


}
