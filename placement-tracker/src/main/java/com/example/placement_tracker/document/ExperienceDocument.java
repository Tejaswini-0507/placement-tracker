package com.example.placement_tracker.document;


import com.example.placement_tracker.enums.DifficultyLevel;

import com.example.placement_tracker.enums.InterviewRound;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "interview_experiences")
public class ExperienceDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private UUID id;

    @Field(type = FieldType.Keyword)
    private UUID studentId;

    @Field(type = FieldType.Keyword)
    private String studentName;

    @Field(type = FieldType.Keyword)
    private UUID companyId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String companyName;

    @Field(type = FieldType.Keyword)
    private InterviewRound interviewRound;

    @Field(type = FieldType.Keyword)
    private DifficultyLevel difficultyRating;

    @Field(type = FieldType.Integer)
    private Integer durationMinutes;

    @Field(type = FieldType.Integer)
    private Integer totalProblemsAsked;

    @Field(type = FieldType.Text,analyzer = "standard")
    private String questionsAsked;

    @Field(type = FieldType.Keyword)
    private List<String> topics;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String experienceSummary;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String helpfulResources;

    @Field(type = FieldType.Keyword)
    private String result;

    @Field(type = FieldType.Long)
    private Long resultReceivedDate;

    @Field(type = FieldType.Boolean)
    private Boolean isPublic;

    @Field(type = FieldType.Integer)
    private Integer upvotes;

    @Field(type = FieldType.Integer)
    private Integer downvotes;

    @Field(type = FieldType.Long)
    private Long createdAt;

    @Field(type = FieldType.Long)
    private Long updatedAt;
}
