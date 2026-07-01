package com.example.placement_tracker.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_companies_industry", columnList = "industry"),
        @Index(name = "idx_companies_name",columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    private String website;

    private String description;

    private String headquarters;

    private String industry;

    @Column(nullable = false)
    private String hiringFor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode packagesOffered;

    @Column(nullable = false,name = "average_difficulty")
    private BigDecimal averageDifficulty;

    @Column(name = "total_applicants")
    private Integer totalApplicants;

    @Column(name = "total_selected")
    private Integer totalSelected;

    @Column(nullable = false, name = "created_at", updatable = false)
    private Long createdAt;

    @OneToMany(mappedBy = "company" , fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<StudentApplication> applications = new ArrayList<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<InterviewExperience> interviewExperiences = new ArrayList<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DiscussionThread> discussionThreads = new ArrayList<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PrepRoadmap> prepRoadmaps = new ArrayList<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TopicFrequencyAnalytics> topicFrequencyAnalytics = new ArrayList<>();

    @OneToMany(mappedBy = "company", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Position> positions = new ArrayList<>();

    @PrePersist
    public void onCreate(){
        createdAt = System.currentTimeMillis();
    }

}
