package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public interface CourseRoutePlanner {
    List<CourseStop> plan(List<CourseStopPoint> points);
}
