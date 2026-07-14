package com.example.placement_tracker.controller;

import com.example.placement_tracker.dto.DiscussionMessageRequest;
import com.example.placement_tracker.dto.DiscussionMessageResponse;
import com.example.placement_tracker.dto.ErrorResponse;
import com.example.placement_tracker.dto.PollResponse;
import com.example.placement_tracker.repository.DiscussionMessageRepository;
import com.example.placement_tracker.service.DiscussionMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/messages")
@CrossOrigin(origins = "*")
public class DiscussionMessageController {

    @Autowired
    DiscussionMessageService messageService;

    @Autowired
    DiscussionMessageRepository messageRepository;


    private DiscussionMessageRequest request;

    private static final ConcurrentHashMap<String, Long> LAST_POLL = new ConcurrentHashMap<>();
    // POST MESSAGE
    @PostMapping
    public ResponseEntity<?> postMessage(@RequestBody DiscussionMessageRequest request) {

        try {
            DiscussionMessageResponse response = messageService.postMessage(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(),System.currentTimeMillis()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to post message",System.currentTimeMillis()));
        }
    }
    // GET ALL MESSAGES BY THREAD
    @GetMapping("/thread/{threadId}")
    public ResponseEntity<?> getMessages(@PathVariable UUID threadId) {
        try {
            List<DiscussionMessageResponse> response = messageService.getMessagesByThread(threadId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("NOT_FOUND", e.getMessage(),System.currentTimeMillis()));
        }
    }

    // POLLING ENDPOINT - Get new messages since last poll (every 5 seconds)
    @GetMapping("/thread/{threadId}/poll")
    public ResponseEntity<?> pollMessages(@PathVariable UUID threadId) {
        try {
            String key = threadId.toString();
            long currentTime = System.currentTimeMillis();
            long lastPollTime = LAST_POLL.getOrDefault(key, currentTime - 5000);

            // Get all messages from thread
            List<DiscussionMessageResponse> allMessages = messageService.getMessagesByThread(threadId);

            // Filter only NEW messages since last poll
            List<DiscussionMessageResponse> newMessages = allMessages.stream()
                    .filter(msg -> msg.getCreatedAt() >= lastPollTime)
                    .toList();

            // Update last poll time for next call
            LAST_POLL.put(key, currentTime);

            // Create poll response
            PollResponse pollResponse = PollResponse.builder()
                    .threadId(threadId)
                    .newMessageCount(newMessages.size())
                    .newMessages(newMessages)
                    .pollTimeStamp(currentTime)
                    .hasUpdates(!newMessages.isEmpty())
                    .build();

            return ResponseEntity.ok(pollResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to poll messages",System.currentTimeMillis()));
        }
    }

    // UPDATE MESSAGE
    @PutMapping("/{messageId}")
    public ResponseEntity<?> updateMessage(
            @PathVariable UUID messageId,
            @RequestBody DiscussionMessageRequest request
    ) {
        try {
            DiscussionMessageResponse response = messageService.updateMessage(messageId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(),System.currentTimeMillis()));
        }
    }

    // DELETE MESSAGE
    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable UUID messageId) {
        try {
            messageService.deleteMessage(messageId);
            return ResponseEntity.ok(new ErrorResponse("SUCCESS", "Message deleted",System.currentTimeMillis()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage(),System.currentTimeMillis()));
        }
    }

    // LIKE MESSAGE
    @PostMapping("/{messageId}/like")
    public ResponseEntity<?> likeMessage(@PathVariable UUID messageId) {
        try {
            DiscussionMessageResponse response = messageService.likeMessage(messageId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to like message",System.currentTimeMillis()));
        }
    }

    // UNLIKE MESSAGE
    @PostMapping("/{messageId}/unlike")
    public ResponseEntity<?> unlikeMessage(@PathVariable UUID messageId) {
        try {
            DiscussionMessageResponse response = messageService.unlikeMessage(messageId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("INTERNAL_ERROR", "Failed to unlike message",System.currentTimeMillis()));
        }
    }



}
