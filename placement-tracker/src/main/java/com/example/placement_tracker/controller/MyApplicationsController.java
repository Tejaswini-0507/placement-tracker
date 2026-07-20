package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.dto.MyApplicationResponse;
import com.example.placement_tracker.service.MyApplicationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/my-applications")
@CrossOrigin(origins = "*")
public class MyApplicationsController {

    @Autowired
    MyApplicationsService applicationsService;

    @GetMapping
    public ResponseEntity<?> getMyApplications() {
        try {
            MyApplicationResponse response = applicationsService.getMyApplications();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND", e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to fetch applications",System.currentTimeMillis()));
        }
    }
}
