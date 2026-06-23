package com.ssafy.enjoytrip.core.api.web.dto.request;

import com.ssafy.enjoytrip.core.domain.CourseOrderOptimizationContext;
import com.ssafy.enjoytrip.core.support.error.CoreException;
import com.ssafy.enjoytrip.core.support.error.ErrorType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

public record CourseOrderRecommendationRequest(
        @DecimalMin("-90.0") @DecimalMax("90.0") Double currentLatitude,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double currentLongitude
) {
    public CourseOrderOptimizationContext toContext() {
        if (currentLatitude == null && currentLongitude == null) {
            return CourseOrderOptimizationContext.empty();
        }
        if (currentLatitude == null || currentLongitude == null) {
            throw new CoreException(ErrorType.COURSE_INVALID_ITEM);
        }
        return new CourseOrderOptimizationContext(currentLatitude, currentLongitude);
    }
}
