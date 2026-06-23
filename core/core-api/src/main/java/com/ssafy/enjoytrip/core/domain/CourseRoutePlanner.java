package com.ssafy.enjoytrip.core.domain;

import java.util.List;

public interface CourseRoutePlanner {
    CourseRoute plan(List<CourseStopPoint> points);
}
