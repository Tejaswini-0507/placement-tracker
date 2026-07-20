package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.dto.MyResumeResponse;
import com.example.placement_tracker.service.MyResumesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/my-resumes")
@CrossOrigin(origins = "*")
public class MyResumeController {

    @Autowired
    MyResumesService myResumesService;

    @GetMapping
    public ResponseEntity<?> getMyResumes(){
        try{
            MyResumeResponse response = myResumesService.getMyResumes();
            return ResponseEntity.ok(response);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis()));
        }catch (Exception e){
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_SERVER",e.getMessage(),System.currentTimeMillis()));
        }
    }
}
