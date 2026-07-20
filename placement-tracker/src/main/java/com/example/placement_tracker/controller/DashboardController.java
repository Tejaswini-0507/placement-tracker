package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.DashboardStats;
import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        try {
            DashboardStats stats = dashboardService.getDashBoard();
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(404)
                    .body(new ErrorResponse("NOT_FOUND", e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("INTERNAL_ERROR", e.getMessage(),System.currentTimeMillis()));
        }
    }
}
