package com.example.placement_tracker.service;

import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.InterviewRoundConfig;
import com.example.placement_tracker.entity.Position;

public interface InterviewRoundConfigService {

    InterviewRoundConfig getOrCreateInterviewRound(Company company, String roundName, Integer roundNumber);

}
