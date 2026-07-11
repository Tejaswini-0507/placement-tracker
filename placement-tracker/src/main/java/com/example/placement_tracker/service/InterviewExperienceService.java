package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.InterviewExperienceRequest;
import com.example.placement_tracker.dto.InterviewExperienceResponse;
import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.InterviewExperience;
import com.example.placement_tracker.entity.Student;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.enums.InterviewRound;
import com.example.placement_tracker.repository.CompanyRepository;
import com.example.placement_tracker.repository.InterviewExperienceRepository;
import com.example.placement_tracker.repository.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class InterviewExperienceService {

    @Autowired
    InterviewExperienceRepository interviewExperienceRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    ObjectMapper objectMapper;

    //Create
    public InterviewExperienceResponse createInterviewExperience(InterviewExperienceRequest request){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

       Student student = studentRepository.findByEmail(email)
               .orElseThrow(() -> new IllegalArgumentException("Student not found"));

       Company company = companyRepository.findById(request.getCompanyId())
               .orElseThrow(() -> new IllegalArgumentException("Company not found"));

       JsonNode questionsJsonNode = null;
       if(request.getQuestionsJson() != null){
           try{
               questionsJsonNode = objectMapper.readTree(request.getQuestionsJson());
           }catch (Exception e){
               throw new IllegalArgumentException("Invalid JSON for questions: "+e.getMessage());
           }
       }

       JsonNode topicsJsonNode = null;
       if(request.getTopics() != null){
           try{
               String[] topicArray = request.getTopics().split(",");
               topicsJsonNode = objectMapper.valueToTree(topicArray);
           }catch (Exception e){
               throw new IllegalArgumentException("Invalid format for topics");
           }
       }

       InterviewExperience interviewExperience = InterviewExperience.builder()
               .student(student)
               .company(company)
               .interviewRound(InterviewRound.valueOf(request.getInterviewRound()))
               .dateExperienced(request.getDateExperienced())
               .difficultyRating(DifficultyLevel.valueOf(request.getDifficultyRating()))
               .totalProblemsAsked(request.getTotalProblemsAsked())
               .questionsAsked(request.getQuestionsAsked())
               .questionsJson(questionsJsonNode)
               .topics(topicsJsonNode)
               .experienceSummary(request.getExperienceSummary())
               .helpfulResources(request.getHelpfulResources())
               .interviewerFeedback(request.getInterviewerFeedback())
               .result(InterviewResult.valueOf(request.getResult()))
               .resultReceivedDate(request.getResultReceivedDate())
               .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
               .build();

            interviewExperience = interviewExperienceRepository.save(interviewExperience);
            return entityToResponse(interviewExperience);

    }

    //READ ONE
    public InterviewExperienceResponse getExperienceById(UUID id){
        InterviewExperience experience = interviewExperienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Interview Experience not found"));

        return entityToResponse(experience);
    }

    //READ ALL
    @Transactional(readOnly = true)
    public List<InterviewExperienceResponse> getMyExperiences(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        return interviewExperienceRepository.findByStudent_Id(student.getId())
                .stream().map(this :: entityToResponse)
                .collect(Collectors.toList());
    }

    //READ COMPANY EXPERIENCES
    public List<InterviewExperienceResponse> getCompanyExperiences(UUID companyId){
        if(!companyRepository.existsById(companyId)){
            throw new IllegalArgumentException("Company not found");
        }
        return interviewExperienceRepository.findByCompany_Id(companyId)
                .stream()
                .filter(exp -> exp.getIsPublic() != null &&
                        exp.getIsPublic())
                .map(this::entityToResponse)
                .collect(Collectors.toList());
    }


    //UPDATE
    public InterviewExperienceResponse updateInterviewExperience(UUID id , InterviewExperienceRequest request){
        //Check experience exists or not
        InterviewExperience interviewExperience = interviewExperienceRepository.findById(id)
                .orElseThrow(() ->  new IllegalArgumentException("Expereicne not found"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        //Check student exists or not
        Student currentStudent = studentRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        //Check whether logged in student and entered student id are same or not
        if(!interviewExperience.getStudent().getId().equals(currentStudent.getId())){
            throw new IllegalArgumentException("Unauthorized: Cannot update another student's experience");
        }
        JsonNode topicsJsonNode = null;
        if(request.getTopics() != null){
            try{
                String[] topicArray = request.getTopics().split(",");
                topicsJsonNode = objectMapper.valueToTree(topicArray);
            }catch (Exception e){
                throw new IllegalArgumentException("Invalid format for topics");
            }
        }

        JsonNode questionsJsonNode = null;
        if(request.getQuestionsJson() != null){
            try{
                questionsJsonNode = objectMapper.readTree(request.getQuestionsJson());
            }catch (Exception e){
                throw new IllegalArgumentException("Invalid JSON for questions: "+e.getMessage());
            }
        }

        interviewExperience.setInterviewRound(InterviewRound.valueOf(request.getInterviewRound()));
        interviewExperience.setDateExperienced(request.getDateExperienced());
        interviewExperience.setDifficultyRating(DifficultyLevel.valueOf(request.getDifficultyRating()));
        interviewExperience.setDurationMinutes(request.getDurationMinutes());
        interviewExperience.setTotalProblemsAsked(request.getTotalProblemsAsked());
        interviewExperience.setQuestionsAsked(request.getQuestionsAsked());
        interviewExperience.setQuestionsJson(questionsJsonNode);
        interviewExperience.setTopics(topicsJsonNode);
        interviewExperience.setExperienceSummary(request.getExperienceSummary());
        interviewExperience.setHelpfulResources(request.getHelpfulResources());
        interviewExperience.setInterviewerFeedback(request.getInterviewerFeedback());
        interviewExperience.setResult(InterviewResult.valueOf(request.getResult()));
        interviewExperience.setResultReceivedDate(request.getResultReceivedDate());
        interviewExperience.setIsPublic(request.getIsPublic());

        interviewExperience = interviewExperienceRepository.save(interviewExperience);
        return entityToResponse(interviewExperience);
    }

    //UPVOTE
    public InterviewExperienceResponse upvoteExperience(UUID id){
        InterviewExperience interviewExperience = interviewExperienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found"));
        interviewExperience.setUpvotes((interviewExperience.getUpvotes() != null ? interviewExperience.getUpvotes() : 0) + 1);
        interviewExperience =interviewExperienceRepository.save(interviewExperience);
        return entityToResponse(interviewExperience);
    }

    //UPVOTE
    public InterviewExperienceResponse downvoteExperience(UUID id){
        InterviewExperience interviewExperience = interviewExperienceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Experience not found"));
        interviewExperience.setDownvotes((interviewExperience.getDownvotes() != null ? interviewExperience.getUpvotes() : 0) + 1);
        interviewExperience =interviewExperienceRepository.save(interviewExperience);
        return entityToResponse(interviewExperience);
    }

    //HELPER
    private InterviewExperienceResponse entityToResponse(InterviewExperience interviewExperience){
        DifficultyLevel difficultyLevel = interviewExperience.getDifficultyRating();
        InterviewRound interviewRound = interviewExperience.getInterviewRound();
        InterviewResult interviewResult = interviewExperience.getResult();
        return InterviewExperienceResponse.builder()
                .id(interviewExperience.getId())
                .studentId(interviewExperience.getStudent().getId())
                .studentName(interviewExperience.getStudent().getName())
                .companyId(interviewExperience.getCompany().getId())
                .companyName(interviewExperience.getCompany().getName())
                .interviewRound(String.valueOf(interviewRound))
                .dateExperienced(interviewExperience.getDateExperienced())
                .difficultyRating(String.valueOf(difficultyLevel))
                .totalProblemsAsked(interviewExperience.getTotalProblemsAsked())
                .questionsAsked(interviewExperience.getQuestionsAsked())
                .questionsJson(interviewExperience.getQuestionsJson())
                .topics(interviewExperience.getTopics())
                .experienceSummary(interviewExperience.getExperienceSummary())
                .helpfulResources(interviewExperience.getHelpfulResources())
                .interviewerFeedback(interviewExperience.getInterviewerFeedback())
                .result(String.valueOf(interviewResult))
                .resultReceivedDate(interviewExperience.getResultReceivedDate())
                .isPublic(interviewExperience.getIsPublic())
                .upvotes(interviewExperience.getUpvotes())
                .downvotes(interviewExperience.getDownvotes())
                .createdAt(interviewExperience.getCreatedAt())
                .updatedAt(interviewExperience.getUpdatedAt())
                .build();
    }
}
