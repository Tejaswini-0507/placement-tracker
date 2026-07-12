package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.dto.InterviewExperienceRequest;
import com.example.placement_tracker.dto.InterviewExperienceResponse;
import com.example.placement_tracker.service.InterviewExperienceService;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.rmi.server.ExportException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/experience")
@CrossOrigin(origins = "*")
public class InterviewExperienceController {

    @Autowired
    InterviewExperienceService interviewExperienceService;

    @PostMapping
    public ResponseEntity<?> createExperience(@Valid @RequestBody InterviewExperienceRequest request){

        try{
            InterviewExperienceResponse response = interviewExperienceService.createInterviewExperience(request);
            return ResponseEntity.status(201).body(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(404).body(
                    new ErrorResponse("VALIDATION_ERROR",e.getMessage(),System.currentTimeMillis())
            );
        } catch (Exception e) {
            String message = e.getMessage();
            return ResponseEntity.status(500).body(
                    new ErrorResponse("INTERNAL_SERVER",message,System.currentTimeMillis())
            );
        }
    }

    @GetMapping("/my-experiences")
    public ResponseEntity<?> getMyExperiences(){
        try{
            List<InterviewExperienceResponse> responses = interviewExperienceService.getMyExperiences();
            return ResponseEntity.status(200).body(responses);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(500).body(
                    new ErrorResponse("INTERNAL_SERVER","Failed to fetch experiences",System.currentTimeMillis())
            );
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    public ResponseEntity<?> getExperienceById(@PathVariable UUID id){
        try{
            InterviewExperienceResponse response = interviewExperienceService.getExperienceById(id);
            return ResponseEntity.status(200).body(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(404).body(
                    new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis())
            );
        }catch (Exception e){

            return ResponseEntity.status(500).body(
                    new ErrorResponse("INTERNAL_SERVER","Failed to fetch application",System.currentTimeMillis())
            );
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/company/{companyId}")
    public ResponseEntity<?> getCompanyExperiences(@PathVariable UUID companyId){
        try{
            List<InterviewExperienceResponse> responses = interviewExperienceService.getCompanyExperiences(companyId);
            return ResponseEntity.status(200).body(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(
                    new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new ErrorResponse("INTERNAL_SERVER","Failed to fetch application",System.currentTimeMillis())
            );
        }
    }

    @Transactional(readOnly = true)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExperience(@PathVariable UUID id , @Valid @RequestBody InterviewExperienceRequest request){
        try{
            InterviewExperienceResponse response = interviewExperienceService.updateInterviewExperience(id, request);
            return ResponseEntity.status(201).body(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(400).body(
                    new ErrorResponse("VALIDATION_ERROR",e.getMessage(),System.currentTimeMillis())
            );
        }catch (Exception e){
            return ResponseEntity.status(500).body(
                    new ErrorResponse("INTERNAL_SERVER","Failed to update application",System.currentTimeMillis())
            );
        }
    }

    @PostMapping("/{id}/upvote")
    public ResponseEntity<?> upvoteExperience(@PathVariable UUID id) {
        try {
            InterviewExperienceResponse response = interviewExperienceService.upvoteExperience(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND", e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to upvote experience",System.currentTimeMillis()));
        }
    }

    @PostMapping("/{id}/downvote")
    public ResponseEntity<?> downvoteExperience(@PathVariable UUID id) {
        try {
            InterviewExperienceResponse response = interviewExperienceService.downvoteExperience(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND", e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to downvote experience",System.currentTimeMillis()));
        }
    }
}
