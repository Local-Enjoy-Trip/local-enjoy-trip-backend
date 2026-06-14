package com.ssafy.enjoytrip.repository;

import com.ssafy.enjoytrip.domain.CourseBriefingCandidate;

import java.util.List;

public interface CourseRepository {
    List<CourseBriefingCandidate> findPublicReadyBriefingCandidates(String regionName, int limit);
}
