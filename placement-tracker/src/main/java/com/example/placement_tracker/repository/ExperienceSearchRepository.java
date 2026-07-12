package com.example.placement_tracker.repository;

import com.example.placement_tracker.document.ExperienceDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExperienceSearchRepository  extends ElasticsearchRepository<ExperienceDocument , UUID> {

}
