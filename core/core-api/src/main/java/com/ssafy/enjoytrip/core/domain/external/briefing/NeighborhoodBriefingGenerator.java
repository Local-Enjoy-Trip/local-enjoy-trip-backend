package com.ssafy.enjoytrip.core.domain.external.briefing;

import com.ssafy.enjoytrip.core.domain.NeighborhoodBriefingPrompt;

public interface NeighborhoodBriefingGenerator {
    String generate(NeighborhoodBriefingPrompt prompt);
}
