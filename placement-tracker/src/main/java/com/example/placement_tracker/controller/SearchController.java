package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.dto.SearchRequest;
import com.example.placement_tracker.dto.SearchResultPage;
import com.example.placement_tracker.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    SearchService searchService;

    @PostMapping("/experiences")
    public ResponseEntity<?> searchExperience(@Valid @RequestBody SearchRequest request){
        System.out.println("Inside Controller");
        try{
            SearchResultPage resultPage = searchService.searchExperience(request);
            return ResponseEntity.ok(resultPage);
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(400).body(new ErrorResponse("VALIDATION_ERROR",e.getMessage(),System.currentTimeMillis()));
        }catch (Exception e){
            return ResponseEntity.status(500).body(new ErrorResponse("INTERNAL_ERROR","Search failed: "+ e.getMessage(),System.currentTimeMillis()));

        }
    }
}
