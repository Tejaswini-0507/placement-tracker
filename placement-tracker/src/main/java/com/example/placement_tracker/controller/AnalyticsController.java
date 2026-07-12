package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.AnalyticsDashboard;
import com.example.placement_tracker.dto.AnalyticsResponse;
import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    AnalyticsService analyticsService;

    //CALCULATE ANALYTICS FOR COMPANY
    @PostMapping("/calculate/{companyId}")
    public ResponseEntity<?> calculateAnalytics(@PathVariable UUID companyId){
        try{
            analyticsService.calculateAnalyticsForCompany(companyId);
            return ResponseEntity.ok(new ErrorResponse("SUCCESS","Analytics calculated successfuly",System.currentTimeMillis()));
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(400).body(
                    new ErrorResponse("VALIDATION_ERROR",e.getMessage(),System.currentTimeMillis())
            );
        }catch (Exception e){
            return ResponseEntity.status(500).body(
                    new ErrorResponse("INTERNAL_ERROR","Failed to calculate analytics",System.currentTimeMillis())
            );
        }
    }

    //GET ANALYTICS BY COMPANY AND ROUND
    @GetMapping("/topics/{companyId}")
    public ResponseEntity<?> getTopicAnalytics(@PathVariable UUID companyId,
                                               @RequestParam(required = false) String round){
        try{
            if(round != null && !round.isEmpty()){
                List<AnalyticsResponse> analytics = analyticsService.getAnalyticsByCompanyRound(companyId,round);
                return ResponseEntity.ok(analytics);
            }else{
                List<AnalyticsResponse> analytics = analyticsService.getAnalyticsByCompanyRound(companyId,"");
                return ResponseEntity.ok(analytics);
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(
                    new ErrorResponse("VALIDATION_ERROR",e.getMessage(),System.currentTimeMillis())
            );
        }catch (Exception e){
            return ResponseEntity.status(500).body(
                    new ErrorResponse("INTERNAL_ERROR","Failed to fetch analytics",System.currentTimeMillis())
            );
        }

    }

    @GetMapping("/dashboard/{companyId}")
    public ResponseEntity<?> getDashboard(@PathVariable UUID companyId){
        try{
            AnalyticsDashboard dashboard = analyticsService.getDashboard(companyId);
            return ResponseEntity.ok(dashboard);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(404).body(new ErrorResponse("NOT_FOUND",e.getMessage(),System.currentTimeMillis()));
        }catch (Exception e){
            return ResponseEntity.status(500).body(
                    new ErrorResponse("INTERNAL_ERROR","Failed to fetch dashboard",System.currentTimeMillis())
            );
        }
    }
}
