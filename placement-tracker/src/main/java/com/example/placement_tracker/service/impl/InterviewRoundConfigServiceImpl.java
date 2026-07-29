package com.example.placement_tracker.service.impl;

import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.InterviewRoundConfig;
import com.example.placement_tracker.entity.Position;
import com.example.placement_tracker.repository.InterviewRoundConfigRepository;
import com.example.placement_tracker.service.InterviewRoundConfigService;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewRoundConfigServiceImpl implements InterviewRoundConfigService {

    private final InterviewRoundConfigRepository configRepository;

    @Override
    public InterviewRoundConfig getOrCreateInterviewRound(Company company, String roundName, Integer roundNumber) {



        return configRepository.findByCompanyAndRoundNameIgnoreCase(company,roundName)
                .orElseGet(() -> {
                    InterviewRoundConfig roundConfig = InterviewRoundConfig.builder()
                            .company(company)
                            .roundName(roundName)
                            .roundNumber(roundNumber)
                            .build();

                    return configRepository.save(roundConfig);
                });
    }

}
