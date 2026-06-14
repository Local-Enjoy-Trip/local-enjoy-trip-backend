package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.NeighborhoodBriefingPrompt;

public interface NeighborhoodBriefingGenerator {
    String generate(NeighborhoodBriefingPrompt prompt);
}
