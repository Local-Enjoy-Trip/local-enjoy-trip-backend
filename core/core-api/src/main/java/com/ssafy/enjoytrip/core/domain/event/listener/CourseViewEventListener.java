package com.ssafy.enjoytrip.core.domain.event.listener;

import com.ssafy.enjoytrip.core.domain.event.CourseViewedEvent;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.CourseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseViewEventListener {
    private final CourseMapper courseMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCourseViewed(CourseViewedEvent event) {
        try {
            courseMapper.insertView(event.courseId(), event.memberId());
        } catch (Exception ex) {
            log.warn(
                    "코스 조회 이력 저장 실패 - courseId: {}, memberId: {}",
                    event.courseId(), event.memberId(), ex
            );
        }
    }
}
