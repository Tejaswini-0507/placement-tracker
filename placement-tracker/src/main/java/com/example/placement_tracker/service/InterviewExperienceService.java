package com.example.placement_tracker.service;

import com.example.placement_tracker.document.ExperienceDocument;
import com.example.placement_tracker.dto.InterviewExperienceRequest;
import com.example.placement_tracker.dto.InterviewExperienceResponse;
import com.example.placement_tracker.entity.*;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.enums.InterviewRound;
import com.example.placement_tracker.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
    StudentApplicationRepository applicationRepository;

    @Autowired
    InterviewRoundConfigRepository interviewRoundConfigRepository;

    @Autowired
    ExperienceSearchRepository experienceSearchRepository;

    @Autowired
    ObjectMapper objectMapper;

    //Create
    @Transactional
    public InterviewExperienceResponse createInterviewExperience(InterviewExperienceRequest request){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

       Student student = studentRepository.findByEmail(email)
               .orElseThrow(() -> new IllegalArgumentException("Student not found"));

       Company company = companyRepository.findById(request.getCompanyId())
               .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        StudentApplication application = applicationRepository.findByStudent_IdAndCompany_IdAndPosition_Id(student.getId(),company.getId(),request.getPositionId())
                .orElseThrow(() -> new IllegalArgumentException("Application Not found"));

       InterviewRoundConfig roundConfig = interviewRoundConfigRepository.findById(request.getInterviewRoundConfigId())
               .orElseThrow(() -> new IllegalArgumentException("Round not found"));

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
               e.printStackTrace();
               throw new IllegalArgumentException("Invalid format for topics",e);
           }
       }



       InterviewExperience interviewExperience = InterviewExperience.builder()
               .student(student)
               .company(company)
               .studentApplication(application)
               .interviewRoundConfig(roundConfig)
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

            ExperienceDocument document = convertToDocument(interviewExperience);
            experienceSearchRepository.save(document);

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


        InterviewRoundConfig round = interviewRoundConfigRepository.findById(request.getInterviewRoundConfigId())
                        .orElseThrow(()->  new IllegalArgumentException("Interview Round not found"));

        interviewExperience.setInterviewRoundConfig(round);
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
        interviewExperience.setDownvotes((interviewExperience.getDownvotes() != null ? interviewExperience.getDownvotes() : 0) + 1);
        interviewExperience =interviewExperienceRepository.save(interviewExperience);
        return entityToResponse(interviewExperience);
    }

    //HELPER
    private InterviewExperienceResponse entityToResponse(InterviewExperience interviewExperience){
        DifficultyLevel difficultyLevel = interviewExperience.getDifficultyRating();
        InterviewResult interviewResult = interviewExperience.getResult();
        return InterviewExperienceResponse.builder()
                .id(interviewExperience.getId())
                .studentId(interviewExperience.getStudent().getId())
                .studentName(interviewExperience.getStudent().getName())
                .companyId(interviewExperience.getCompany().getId())
                .companyName(interviewExperience.getCompany().getName())
                .positionId(interviewExperience.getStudentApplication().getPosition().getId())
                .positionName(interviewExperience.getStudentApplication().getPosition().getTitle())
                .interviewRoundConfigId(interviewExperience.getInterviewRoundConfig().getId())
                .interviewRoundName(interviewExperience.getInterviewRoundConfig().getRoundName())
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

    private ExperienceDocument convertToDocument(InterviewExperience experience) {



        return ExperienceDocument.builder()
                .id(experience.getId())
                .studentId(experience.getStudent().getId())
                .studentName(experience.getStudent().getName()) // Change if your field name is different
                .companyId(experience.getCompany().getId())
                .companyName(experience.getCompany().getName())
                .positionTitle(experience.getStudentApplication().getPosition().getTitle())// Change if your field name is different
                .interviewRoundConfigId(experience.getInterviewRoundConfig().getId())
                .interviewRoundName(experience.getInterviewRoundConfig().getRoundName())
                .difficultyRating(experience.getDifficultyRating())
                .durationMinutes(experience.getDurationMinutes())
                .totalProblemsAsked(experience.getTotalProblemsAsked())
                .questionsAsked(experience.getQuestionsAsked())

                // Convert JsonNode to String
                .topics(convertTopics(experience.getTopics()))

                .experienceSummary(experience.getExperienceSummary())
                .helpfulResources(experience.getHelpfulResources())
                .result(experience.getResult().name())
                .resultReceivedDate(experience.getResultReceivedDate())
                .isPublic(experience.getIsPublic())
                .upvotes(experience.getUpvotes())
                .downvotes(experience.getDownvotes())
                .createdAt(experience.getCreatedAt())
                .updatedAt(experience.getUpdatedAt())
                .build();
    }

    private List<String> convertTopics(JsonNode topicsNode) {

        if (topicsNode == null || !topicsNode.isArray()) {
            return Collections.emptyList();
        }

        List<String> topics = new ArrayList<>();

        for (JsonNode node : topicsNode) {
            topics.add(node.asText().trim());
        }

        return topics;
    }
}
