package com.ssafy.enjoytrip.core.domain.event;

public record CourseViewedEvent(String courseId, Long memberId) {}
