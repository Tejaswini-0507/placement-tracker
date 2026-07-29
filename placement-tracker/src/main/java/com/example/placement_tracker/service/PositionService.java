package com.example.placement_tracker.service;

import com.example.placement_tracker.entity.Company;
import com.example.placement_tracker.entity.Position;


public interface PositionService {
    Position getOrCreatePosition(Company company, String title, String location );
}
