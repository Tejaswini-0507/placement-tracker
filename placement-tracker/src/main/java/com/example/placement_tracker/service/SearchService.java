package com.example.placement_tracker.service;

import com.example.placement_tracker.document.ExperienceDocument;
import com.example.placement_tracker.dto.InterviewExperienceResponse;
import com.example.placement_tracker.dto.SearchRequest;
import com.example.placement_tracker.dto.SearchResponse;
import com.example.placement_tracker.dto.SearchResultPage;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.enums.InterviewRound;
import com.example.placement_tracker.repository.ExperienceSearchRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static javax.management.Query.in;
import static javax.management.Query.or;
import static org.springframework.data.elasticsearch.core.query.Criteria.where;

@Service
public class SearchService {

    @Autowired
    ExperienceSearchRepository searchRepository;

    @Autowired
    ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public SearchResultPage searchExperience(SearchRequest request){
        Criteria criteria = where("isPublic").is(true);
        Query query = new CriteriaQuery(criteria);

        if (request.getQuery() != null && !request.getQuery().isBlank()) {

            org.springframework.data.elasticsearch.core.query.Criteria searchCriteria =
                    where("companyName").contains(request.getQuery())
                            .or(where("questionsAsked").contains(request.getQuery()))
                            .or(where("topics").contains(request.getQuery()))
                            .or(where("experienceSummary").contains(request.getQuery()));

            criteria = criteria.and(searchCriteria);
        }

        if(request.getCompanyId() != null){
            criteria = criteria.and(where("companyId").is(request.getCompanyId()));
        }

        if(request.getInterviewRound() != null && !request.getInterviewRound().isEmpty()){
            criteria = criteria.and(where("interviewRound").is(request.getInterviewRound()));
        }

        if(request.getDifficultyRating() != null){
            criteria = criteria.and(where("difficultyRating").is(request.getDifficultyRating()));
        }
        

        if (request.getTopics() != null && !request.getTopics().isEmpty()) {

            Criteria topicCriteria = null;

            for (String topic : request.getTopics()) {

                if (topicCriteria == null) {
                    topicCriteria = new Criteria("topics").is(topic.trim());
                } else {
                    topicCriteria = topicCriteria.or(new Criteria("topics").is(topic.trim()));
                }
            }

            criteria = criteria.and(topicCriteria);
        }

        if(request.getResult() != null && !request.getResult().isEmpty()){
            criteria = criteria.and(where("result").is(request.getResult()));
        }

        Sort.Direction direction = request.getSortOrder().
                equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(direction,request.getSortBy())
        );

        query.setPageable(pageable);

        SearchHits<ExperienceDocument> searchHits =
                elasticsearchTemplate.search(query,ExperienceDocument.class);

        List<SearchResponse> responses = searchHits.getSearchHits()
                .stream()
                .map(hit -> documentToResponse(hit.getContent(),hit.getScore()))
                .collect(Collectors.toList());
        long totalElements = searchHits.getTotalHits();
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        return SearchResultPage.builder()
                .content(responses)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(request.getPage() == 0)
                .last(request.getPage() >= totalPages - 1)
                .build();
    }

    private SearchResponse documentToResponse(ExperienceDocument doc, Float score) {

        return SearchResponse.builder()
                .id(doc.getId())
                .studentId(doc.getStudentId())
                .studentName(doc.getStudentName())
                .companyId(doc.getCompanyId())
                .companyName(doc.getCompanyName())
                .positionTitle(doc.getPositionTitle())
                .interviewRoundConfigId(doc.getInterviewRoundConfigId())
                .interviewRoundName(doc.getInterviewRoundName())
                .difficultyRating(String.valueOf(doc.getDifficultyRating()))
                .durationMinutes(doc.getDurationMinutes())
                .totalProblemsAsked(doc.getTotalProblemsAsked())
                .questionsAsked(doc.getQuestionsAsked())
                .topics(doc.getTopics() != null ? doc.getTopics().toString() :  null)
                .experienceSummary(doc.getExperienceSummary())
                .helpfulResources(doc.getHelpfulResources())
                .result(doc.getResult())
                .resultReceivedDate(doc.getResultReceivedDate())
                .isPublic(doc.getIsPublic())
                .upvotes(doc.getUpvotes())
                .downvotes(doc.getDownvotes())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .score(score)
                .build();
    }
}
