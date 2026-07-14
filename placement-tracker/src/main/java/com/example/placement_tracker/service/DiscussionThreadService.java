package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.DiscussionMessageResponse;
import com.example.placement_tracker.dto.DiscussionThreadRequest;
import com.example.placement_tracker.dto.DiscussionThreadResponse;
import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.DiscussionMessage;
import com.example.placement_tracker.entity.DiscussionThread;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.repository.CompanyRepository;
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
public class DiscussionThreadService {
    private static final Logger logger = LoggerFactory.getLogger(DiscussionThreadService.class);

    @Autowired
    DiscussionThreadRepository threadRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    StudentRepository studentRepository;

    //CREATE THREAD
    public DiscussionThreadResponse createThread(DiscussionThreadRequest request){
        //Get current student(who created the thread)
        String email  = SecurityContextHolder.getContext().getAuthentication().getName();

        //get Company
        Student createdByStudent = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        //Get Company
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        Student student = null;
        if(request.getStudentId() != null){
            student = studentRepository.findById(request.getStudentId()).orElseThrow(() -> new IllegalArgumentException("Student not found"));

        }

        DiscussionThread thread = DiscussionThread.builder()
                .student(student)
                .createdByStudent(createdByStudent)
                .company(company)
                .interviewRound(String.valueOf(request.getInterviewRound()))
                .topic(request.getTopic())
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        thread = threadRepository.save(thread);

        logger.info("Thread created: {} by {}", thread.getId(),createdByStudent.getEmail());

        return entityToResponse(thread);

    }

    //GET THREAD BY ID
    @Transactional
    public DiscussionThreadResponse getThreadById(UUID threadId){
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not exception found"));

        return entityToResponse(thread);
    }

    //GET THREAD BY COMPANY AND INTERVIEW ROUND
    @Transactional
    public List<DiscussionThreadResponse> getThreadsByCompanyAndRound(
            UUID companyId, String interviewRound
    ){
        return threadRepository.findByCompany_IdAndInterviewRound(companyId,interviewRound).stream()
                .map(this :: entityToResponse)
                .collect(Collectors.toList());
    }

    //GET PINNED THREADS
    @Transactional
    public List<DiscussionThreadResponse> getPinnedThreads(UUID companyId){
        return threadRepository.findByCompany_IdAndPinned(companyId,true)
                .stream()
                .map(this :: entityToResponse)
                .collect(Collectors.toList());
    }

    //GET MY THREADS
    @Transactional
    public List<DiscussionThreadResponse> getMyThreads(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        return threadRepository.findByCreatedByStudent_Id(student.getId())
                .stream()
                .map(this :: entityToResponse)
                .collect(Collectors.toList());

    }

    //UPDATE THREAD
    @Transactional
    public DiscussionThreadResponse updateThread(UUID threadId, DiscussionThreadRequest request){
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if(!thread.getCreatedByStudent().getId().equals(student.getId())){
            throw new IllegalArgumentException("Only thread creator can update");
        }

        if (request.getTitle() != null) thread.setTitle(request.getTitle());
        if (request.getDescription() != null) thread.setDescription(request.getDescription());
        if (request.getInterviewRound() != null) thread.setInterviewRound(String.valueOf(request.getInterviewRound()));
        if (request.getTopic() != null) thread.setTopic(request.getTopic());

        thread = threadRepository.save(thread);

        logger.info("Thread updated: {}", threadId);
        return entityToResponse(thread);
    }

    //DELETE THREAD
    public void deleteThread(UUID threadId){
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if(!thread.getCreatedByStudent().getId().equals(student.getId())){
            throw new IllegalArgumentException("Only thread creator can delete");
        }

        threadRepository.delete(thread);

        logger.info("Thread deleted: {}", threadId);
    }

    //PIN THREAD
    @Transactional
    public DiscussionThreadResponse pinThread(UUID threadId){
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        thread.setPinned(true);

        thread = threadRepository.save(thread);
        return entityToResponse(thread);
    }

    //UNPIN THREAD
    @Transactional
    public DiscussionThreadResponse unpinThread(UUID threadId){
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        thread.setPinned(false);

        thread = threadRepository.save(thread);
        return entityToResponse(thread);
    }

    //UPDATE MESSAGE COUNT AND LAST ACTIVITY
    public void updateActivity(UUID threadId, int messageIncrement){
        DiscussionThread thread = threadRepository.findById(threadId)
                .orElse(null);

        if(thread != null){
            thread.setMessageCount(thread.getMessageCount()+messageIncrement);
            thread.setLastActivity(System.currentTimeMillis());
            threadRepository.save(thread);
        }
    }




    //HELPER
    public DiscussionThreadResponse entityToResponse(DiscussionThread thread){

        return DiscussionThreadResponse.builder()
                .id(thread.getId())
                .companyId(thread.getCompany().getId())
                .companyName(thread.getCompany().getName())
                .studentId(thread.getStudent().getId())
                .studentName(thread.getStudent().getName())
                .createdByStudentId(thread.getCreatedByStudent().getId())
                .createdByStudentName(thread.getCreatedByStudent().getName())
                .interviewRound(thread.getInterviewRound())
                .topic(String.valueOf(thread.getInterviewRound()))
                .title(thread.getTitle())
                .description(thread.getDescription())
                .messageCount(thread.getMessageCount())
                .pinned(thread.getPinned())
                .lastActivity(thread.getLastActivity())
                .createdAt(thread.getLastActivity())
                .build();
    }

}
