package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.DiscussionMessageRequest;
import com.example.placement_tracker.dto.DiscussionMessageResponse;
import com.example.placement_tracker.dto.DiscussionThreadResponse;
import com.example.placement_tracker.entity.DiscussionMessage;
import com.example.placement_tracker.entity.DiscussionThread;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.repository.DiscussionMessageRepository;
import com.example.placement_tracker.repository.DiscussionThreadRepository;
import com.example.placement_tracker.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DiscussionMessageService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussionMessageService.class);

    @Autowired
    DiscussionMessageRepository messageRepository;

    @Autowired
    DiscussionThreadRepository threadRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    DiscussionThreadService threadService;

    //POST MESSAGE
    public DiscussionMessageResponse postMessage(DiscussionMessageRequest request){
        DiscussionThread thread = threadRepository.findById(request.getThreadId())
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        DiscussionMessage message = DiscussionMessage.builder()
                .thread(thread)
                .student(student)
                .message(request.getMessage())
                .build();

        message = messageRepository.save(message);

        threadService.updateActivity(request.getThreadId(),1);
        logger.info("Message posted: {} in thread {}",message.getId(),request.getThreadId());

        return entityToResponse(message);
    }

    //GET MESSAGES BY THREAD
    @Transactional
    public List<DiscussionMessageResponse> getMessagesByThread(UUID threadId){
        if(!threadRepository.existsById(threadId)){
            throw new IllegalArgumentException("Thread not found");
        }
        return messageRepository.findByThread_IdOrderByCreatedAtAsc(threadId)
                .stream()
                .map(this :: entityToResponse)
                .collect(Collectors.toList());
    }

    //UPDATE MESSAGE
    public DiscussionMessageResponse updateMessage(UUID messageId, DiscussionMessageRequest request){

        DiscussionMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Verify ownership
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (!message.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Only message author can update");
        }

        message.setMessage(request.getMessage());
        message.setEditedAt(System.currentTimeMillis());
        message = messageRepository.save(message);

        logger.info("Message updated: {}", messageId);

        return entityToResponse(message);
    }

    // DELETE MESSAGE
    public void deleteMessage(UUID messageId) {

        DiscussionMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Verify ownership
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (!message.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Only message author can delete");
        }

        UUID threadId = message.getThread().getId();
        messageRepository.delete(message);

        // Update thread activity
        threadService.updateActivity(threadId, -1);

        logger.info("Message deleted: {}", messageId);
    }

    // LIKE MESSAGE
    @Transactional
    public DiscussionMessageResponse likeMessage(UUID messageId) {

        DiscussionMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        message.setLikes(message.getLikes() + 1);
        message = messageRepository.save(message);

        return entityToResponse(message);
    }

    // UNLIKE MESSAGE
    @Transactional
    public DiscussionMessageResponse unlikeMessage(UUID messageId) {

        DiscussionMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        if (message.getLikes() > 0) {
            message.setLikes(message.getLikes() - 1);
            message = messageRepository.save(message);
        }

        return entityToResponse(message);
    }




    //HELPER
    private DiscussionMessageResponse entityToResponse(DiscussionMessage message){
        return DiscussionMessageResponse.builder()
                .id(message.getId())
                .threadId(message.getThread().getId())
                .studentId(message.getStudent().getId())
                .studentName(message.getStudent().getName())
                .message(message.getMessage())
                .likes(message.getLikes())
                .isEdited(message.getEditedAt() != null)
                .editedAt(message.getEditedAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
