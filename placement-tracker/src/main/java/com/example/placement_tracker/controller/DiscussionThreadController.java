package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.DiscussionThreadRequest;
import com.example.placement_tracker.dto.DiscussionThreadResponse;
import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.service.DiscussionThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/discussions")
@CrossOrigin(origins = "*")
public class DiscussionThreadController {

    @Autowired
    DiscussionThreadService threadService;

    // CREATE THREAD
    @PostMapping("/threads")
    public ResponseEntity<?> createThread(@RequestBody DiscussionThreadRequest request) {
        try {
            DiscussionThreadResponse response = threadService.createThread(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            String message = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", message,System.currentTimeMillis()));
        }
    }

    // GET THREAD BY ID
    @GetMapping("/threads/{threadId}")
    public ResponseEntity<?> getThread(@PathVariable UUID threadId) {
        try {
            DiscussionThreadResponse response = threadService.getThreadById(threadId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("NOT_FOUND", e.getMessage(),System.currentTimeMillis()));
        }
    }

    // GET ALL THREADS BY COMPANY AND INTERVIEW ROUND
    @GetMapping("/company/{companyId}/round/{interviewRound}")
    public ResponseEntity<?> getThreadsByRound(
            @PathVariable UUID companyId,
            @PathVariable String interviewRound
    ) {
        try {
            List<DiscussionThreadResponse> response =
                    threadService.getThreadsByCompanyAndRound(companyId, interviewRound);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to fetch threads",System.currentTimeMillis()));
        }
    }

    // GET PINNED THREADS
    @GetMapping("/company/{companyId}/pinned")
    public ResponseEntity<?> getPinnedThreads(@PathVariable UUID companyId) {
        try {
            List<DiscussionThreadResponse> response = threadService.getPinnedThreads(companyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to fetch threads",System.currentTimeMillis()));
        }
    }

    // GET MY THREADS
    @GetMapping("/my-threads")
    public ResponseEntity<?> getMyThreads() {
        try {
            List<DiscussionThreadResponse> response = threadService.getMyThreads();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("NOT_FOUND", e.getMessage(),System.currentTimeMillis()));
        }
    }

    // UPDATE THREAD
    @PutMapping("/threads/{threadId}")
    public ResponseEntity<?> updateThread(
            @PathVariable UUID threadId,
            @RequestBody DiscussionThreadRequest request
    ) {
        try {
            DiscussionThreadResponse response = threadService.updateThread(threadId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(),System.currentTimeMillis()));
        }
    }

    // DELETE THREAD
    @DeleteMapping("/threads/{threadId}")
    public ResponseEntity<?> deleteThread(@PathVariable UUID threadId) {
        try {
            threadService.deleteThread(threadId);
            return ResponseEntity.ok(new ErrorResponse("SUCCESS", "Thread deleted",System.currentTimeMillis()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(),System.currentTimeMillis()));
        }
    }

    // PIN THREAD
    @PostMapping("/threads/{threadId}/pin")
    public ResponseEntity<?> pinThread(@PathVariable UUID threadId) {
        try {
            DiscussionThreadResponse response = threadService.pinThread(threadId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to pin thread",System.currentTimeMillis()));
        }
    }

    // UNPIN THREAD
    @PostMapping("/threads/{threadId}/unpin")
    public ResponseEntity<?> unpinThread(@PathVariable UUID threadId) {
        try {
            DiscussionThreadResponse response = threadService.unpinThread(threadId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to unpin thread",System.currentTimeMillis()));
        }
    }









}
