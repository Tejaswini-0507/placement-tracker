package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.dto.UserProfileRequest;
import com.example.placement_tracker.dto.UserProfileResponse;
import com.example.placement_tracker.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "*")
public class UserProfileController {

    @Autowired
    UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(){
        System.out.println("============Profile endpoint reached=========");
        try{
            UserProfileResponse response = userProfileService.getMyProfile();
            return ResponseEntity.ok(response);
        }catch(IllegalArgumentException e){
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis()));
        }
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<?> getProfile(@PathVariable String studentId){
        try{
            UserProfileResponse response = userProfileService.getProfileById(studentId);
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody UserProfileRequest request) {
        try {
            UserProfileResponse response = userProfileService.updateMyProfile(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400)
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to update profile",System.currentTimeMillis()));
        }
    }
}
