package com.example.placement_tracker.service;

import com.example.placement_tracker.dto.AnalyticsDashboard;
import com.example.placement_tracker.dto.AnalyticsResponse;
import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.InterviewExperience;
import com.example.placement_tracker.entity.TopicFrequencyAnalytics;
import com.example.placement_tracker.enums.DifficultyLevel;
import com.example.placement_tracker.enums.InterviewResult;
import com.example.placement_tracker.repository.CompanyRepository;
import com.example.placement_tracker.repository.InterviewExperienceRepository;
import com.example.placement_tracker.repository.TopicFrequencyAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    TopicFrequencyAnalyticsRepository analyticsRepository;

    @Autowired
    InterviewExperienceRepository experienceRepository;

    @Autowired
    CompanyRepository companyRepository;

    // CALCULATE ANALYTICS FOR COMPANY
    public void calculateAnalyticsForCompany(UUID companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        logger.info("Calculating analytics for company: {}", company.getName());

        // Get all public experiences for this company
        List<InterviewExperience> experiences = experienceRepository.findByCompany_Id(companyId);

        if (experiences.isEmpty()) {
            logger.warn("No experiences found for company: {}", company.getName());
            return;
        }

        // Group by interview round
        Map<String, List<InterviewExperience>> byRound =
                experiences.stream()
                        .collect(Collectors.groupingBy(
                                exp -> exp.getInterviewRoundConfig().getRoundName()
                        ));

        // For each round, calculate topic frequencies
        for (Map.Entry<String, List<InterviewExperience>> entry : byRound.entrySet()) {
            String round = entry.getKey();
            List<InterviewExperience> roundExperiences = entry.getValue();

            logger.info("Processing round: {} with {} experiences", round, roundExperiences.size());

            // Parse topics and count frequency
            Map<String, Integer> topicCounts = new HashMap<>();
            Map<String, List<Integer>> topicDifficulties = new HashMap<>();

            for (InterviewExperience exp : roundExperiences) {
                if (exp.getTopics() != null) {
                    String topicsStr = exp.getTopics().toString();
                    // Handle JsonNode format: ["Arrays","Strings"] or ["Arrays", "Strings"]
                    String[] topics = topicsStr.replaceAll("[\\[\\]\"]", "").split(",");

                    for (String topic : topics) {
                        String cleanTopic = topic.trim();

                        if (!cleanTopic.isEmpty()) {
                            topicCounts.put(cleanTopic, topicCounts.getOrDefault(cleanTopic, 0) + 1);

                            // Convert DifficultyLevel enum to numeric value
                            Integer difficultyValue = convertDifficultyToNumeric(exp.getDifficultyRating());

                            topicDifficulties.computeIfAbsent(cleanTopic, k -> new ArrayList<>())
                                    .add(difficultyValue);
                        }
                    }
                }
            }

            // Save analytics
            int totalTopicMentions = topicCounts.values().stream().mapToInt(Integer::intValue).sum();

            if (totalTopicMentions == 0) {
                logger.warn("No topics found for round: {}", round);
                continue;
            }

            for (Map.Entry<String, Integer> topic : topicCounts.entrySet()) {
                String topicName = topic.getKey();
                int count = topic.getValue();

                // Calculate percentage as Double
                Double percentage = (double) count / totalTopicMentions * 100;

                // Calculate average difficulty as Double
                Double avgDifficulty = topicDifficulties.get(topicName).stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0);

                logger.debug("Topic: {}, Count: {}, Percentage: {}%, AvgDifficulty: {}",
                        topicName, count, String.format("%.2f", percentage), avgDifficulty);

                // Upsert analytics
                Optional<TopicFrequencyAnalytics> existing =
                        analyticsRepository.findByCompany_IdAndInterviewRound(companyId, round)
                                .stream()
                                .filter(a -> a.getTopic().equalsIgnoreCase(topicName))
                                .findFirst();

                TopicFrequencyAnalytics analytics = existing
                        .orElse(TopicFrequencyAnalytics.builder()
                                .company(company)
                                .interviewRound(round)
                                .topic(topicName)
                                .build());

                analytics.setFrequencyCount(count);
                analytics.setPercentage(percentage);
                analytics.setDifficultyAvg(avgDifficulty);

                analyticsRepository.save(analytics);
            }
        }

        logger.info("Analytics calculation completed for company: {}", company.getName());
    }

    // GET ANALYTICS BY COMPANY AND ROUND
    public List<AnalyticsResponse> getAnalyticsByCompanyRound(UUID companyId, String round) {

        if (!companyRepository.existsById(companyId)) {
            throw new IllegalArgumentException("Company not found");
        }

        List<TopicFrequencyAnalytics> analytics;

        if (round != null && !round.isEmpty()) {
            analytics = analyticsRepository.findByCompany_IdAndInterviewRound(companyId, round);
        } else {
            analytics = analyticsRepository.findByCompany_Id(companyId);
        }

        return analytics.stream()
                .sorted(Comparator.comparing(TopicFrequencyAnalytics::getFrequencyCount).reversed())
                .map(this::entityToResponse)
                .collect(Collectors.toList());
    }

    // GET ANALYTICS DASHBOARD
    public AnalyticsDashboard getDashboard(UUID companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        List<InterviewExperience> experiences = experienceRepository.findByCompany_Id(companyId);

        // Calculate stats
        int totalExperiences = experiences.size();

        // ✅ Convert DifficultyLevel enum to numeric for averaging
        double avgDifficulty = experiences.stream()
                .mapToInt(exp -> exp.getDifficultyRating() != null
                        ? convertDifficultyToNumeric(exp.getDifficultyRating())
                        : 5)  // Default to MEDIUM (5) if null
                .average()
                .orElse(0);

        long passedCount = experiences.stream()
                .filter(exp -> exp.getResult() == InterviewResult.PASSED)
                .count();
        int passRate = totalExperiences > 0 ? (int) (passedCount * 100 / totalExperiences) : 0;

        // Get analytics by round
        List<TopicFrequencyAnalytics> allAnalytics = analyticsRepository.findByCompany_Id(companyId);

        // Group by round and convert to response
        Map<String, List<AnalyticsResponse>> topicsByRound =
                allAnalytics.stream()
                        .collect(Collectors.groupingBy(
                                TopicFrequencyAnalytics::getInterviewRound,
                                Collectors.mapping(this::entityToResponse, Collectors.toList())
                        ));

        // Top topics overall
        List<AnalyticsResponse> topTopics =
                analyticsRepository.findByCompany_IdOrderByFrequencyCountDesc(companyId)
                        .stream()
                        .limit(10)
                        .map(this::entityToResponse)
                        .collect(Collectors.toList());

        // Difficulty by round
        Map<String, Double> difficultyByRound =
                allAnalytics.stream()
                        .collect(Collectors.groupingBy(
                                TopicFrequencyAnalytics::getInterviewRound,
                                Collectors.mapping(
                                        TopicFrequencyAnalytics::getDifficultyAvg,
                                        Collectors.averagingDouble(Double::doubleValue)
                                )
                        ));

        Long lastUpdated = allAnalytics.stream()
                .map(TopicFrequencyAnalytics::getLastUpdated)
                .max(Long::compare)
                .orElse(System.currentTimeMillis());

        return AnalyticsDashboard.builder()
                .companyId(companyId)
                .companyName(company.getName())
                .totalExperiences(totalExperiences)
                .averageDifficulty(Math.round(avgDifficulty * 100.0) / 100.0)  // Round to 2 decimals
                .passRate(passRate)
                .topicsByRound(topicsByRound)
                .topTopics(topTopics)
                .difficultyByRound(difficultyByRound)
                .lastUpdated(lastUpdated)
                .build();
    }

    // HELPER: Convert DifficultyLevel enum to numeric value for calculations
    private Integer convertDifficultyToNumeric(DifficultyLevel level) {
        if (level == null) {
            return 5;  // Default to MEDIUM
        }

        return switch(level) {
            case EASY -> 3;      // Easy = 3
            case MEDIUM -> 5;    // Medium = 5
            case HARD -> 8;      // Hard = 8
            case EXPERT -> 10;   // Expert = 10
        };
    }

    // HELPER: Convert numeric difficulty back to enum for display
    private DifficultyLevel convertNumericToDifficulty(Integer rating) {
        if (rating == null || rating <= 3) {
            return DifficultyLevel.EASY;
        } else if (rating <= 6) {
            return DifficultyLevel.MEDIUM;
        } else if (rating <= 8) {
            return DifficultyLevel.HARD;
        } else {
            return DifficultyLevel.EXPERT;
        }
    }

    // HELPER: Convert entity to DTO
    private AnalyticsResponse entityToResponse(TopicFrequencyAnalytics analytics) {
        return AnalyticsResponse.builder()
                .id(analytics.getId())
                .companyName(analytics.getCompany().getName())
                .interviewRound(analytics.getInterviewRound())
                .topic(analytics.getTopic())
                .frequencyCount(analytics.getFrequencyCount())
                .percentage(Math.round(analytics.getPercentage() * 100.0) / 100.0)  // Round to 2 decimals
                .difficultyAvg(Math.round(analytics.getDifficultyAvg() * 10.0) / 10.0)  // Round to 1 decimal
                .lastUpdated(analytics.getLastUpdated())
                .build();
    }

}
