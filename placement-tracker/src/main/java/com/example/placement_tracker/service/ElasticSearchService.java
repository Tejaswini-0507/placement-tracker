package com.example.placement_tracker.service;

import com.example.placement_tracker.document.ExperienceDocument;
import com.example.placement_tracker.entity.InterviewExperience;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.enums.InterviewRound;
import com.example.placement_tracker.repository.ExperienceSearchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ElasticSearchService {
    @Autowired
    ExperienceSearchRepository experienceSearchRepository;

    @Autowired
    ObjectMapper objectMapper;

    public void indexExperience(InterviewExperience experience){
        InterviewResult result = experience.getResult();

        ExperienceDocument doc = ExperienceDocument.builder()
                .id(experience.getId())
                .studentId(experience.getStudent().getId())
                .studentName(experience.getStudent().getName())
                .companyId(experience.getCompany().getId())
                .companyName(experience.getCompany().getName())
                .interviewRound(experience.getInterviewRound())
                .difficultyRating(experience.getDifficultyRating())
                .durationMinutes(experience.getDurationMinutes())
                .totalProblemsAsked(experience.getTotalProblemsAsked())
                .questionsAsked(experience.getQuestionsAsked())
                .topics(convertTopics(experience.getTopics()))
                .experienceSummary(experience.getExperienceSummary())
                .helpfulResources(experience.getHelpfulResources())
                .result(String.valueOf(result))
                .resultReceivedDate(experience.getResultReceivedDate())
                .isPublic(experience.getIsPublic())
                .upvotes(experience.getUpvotes())
                .downvotes(experience.getDownvotes())
                .createdAt(experience.getCreatedAt())
                .updatedAt(experience.getUpdatedAt())
                .build();

        experienceSearchRepository.save(doc);
    }

    public void updateExperience(InterviewExperience experience){
        indexExperience(experience);
    }

    public void deleteExperience(UUID id){
        experienceSearchRepository.deleteById(id);
    }


    private List<String> convertTopics(JsonNode topicsNode) {

        List<String> topics = new ArrayList<>();

        if (topicsNode != null && topicsNode.isArray()) {
            for (JsonNode node : topicsNode) {
                topics.add(node.asText().trim());
            }
        }

        return topics;
    }
}
