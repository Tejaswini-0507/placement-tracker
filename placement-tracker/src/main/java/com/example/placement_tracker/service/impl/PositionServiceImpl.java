package com.example.placement_tracker.service.impl;

import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.Position;
import com.example.placement_tracker.repository.PositionRepository;
import com.example.placement_tracker.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {
    private final PositionRepository positionRepository;

    @Override
    public Position getOrCreatePosition(Company company,
                                        String title,
                                        String location){
        return positionRepository.findByCompanyAndTitleIgnoreCase(company, title)
                .orElseGet(()-> {
                    Position position = Position.builder()
                            .company(company)
                            .title(title)
                            .location(location)
                            .build();
                    return positionRepository.save(position);
                });
    }
}
