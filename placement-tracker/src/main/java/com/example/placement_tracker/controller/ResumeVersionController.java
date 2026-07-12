package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.dto.ResumeResponse;
import com.example.placement_tracker.service.ResumeVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resume-version")
@CrossOrigin(origins = "*")
public class ResumeVersionController {

    @Autowired
    ResumeVersionService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("versionNumber") Integer versionNumber,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "usedForCompanies", required = false) String usedForCompanies) {
        try {
            ResumeResponse response = resumeService.uploadResume(
                    file, versionNumber, notes, usedForCompanies);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(
                    new ErrorResponse("VALIDATION_ERROR", e.getMessage(), System.currentTimeMillis())
            );
        } catch (IOException e) {
            return ResponseEntity.status(500).body(
                    new ErrorResponse("INTERNAL_ERROR", "Failed to upload resume: " + e.getMessage(), System.currentTimeMillis())
            );
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to upload resume", System.currentTimeMillis()));
        }
    }

    @GetMapping("/my-resumes")
    public ResponseEntity<?> getMyResumes(){
        try {
            List<ResumeResponse> resumes = resumeService.getMyResumes();
            return ResponseEntity.ok(resumes);
        }catch(Exception e)
        {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to fetch resumes", System.currentTimeMillis()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResumeById(@PathVariable UUID id){
        try{
            ResumeResponse response = resumeService.getResumeById(id);
            return ResponseEntity.ok(response);
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis()));
        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR","Failed to get fetch resume",System.currentTimeMillis()));
        }
    }

    @GetMapping("/version/{versionNumber}")
    public ResponseEntity<?> getResumeVersion(@PathVariable Integer versionNumber){
        try{
            ResumeResponse response = resumeService.getResumeVersion(versionNumber);
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis()));
        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR",e.getMessage(),System.currentTimeMillis()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable UUID id){
        try{
            resumeService.deleteResumeVersion(id);
            return ResponseEntity.ok().body(new ErrorResponse("SUCCESS","Resume deleted successfully",System.currentTimeMillis()));
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("VALIDATION_ERROR",e.getMessage(),System.currentTimeMillis()));
        }catch(IOException e){
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to delete resume file",System.currentTimeMillis()));
        }catch(Exception e){
             return ResponseEntity.status(500)
                .body(new ErrorResponse("INTERNAL_ERROR", "Failed to delete resume",System.currentTimeMillis()));
        }

    }
}
