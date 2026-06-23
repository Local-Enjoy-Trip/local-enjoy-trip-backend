package com.ssafy.enjoytrip.core.domain.service;

import com.ssafy.enjoytrip.core.domain.CourseRoute;
import com.ssafy.enjoytrip.core.domain.CourseStopPoint;
import java.util.List;

public interface CourseRoutePlanner {
    CourseRoute plan(List<CourseStopPoint> points);
}
