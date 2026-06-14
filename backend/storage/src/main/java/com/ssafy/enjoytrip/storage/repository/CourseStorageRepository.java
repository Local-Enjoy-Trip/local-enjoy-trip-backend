package com.ssafy.enjoytrip.storage.repository;

import static com.ssafy.enjoytrip.storage.jooq.tables.Courses.COURSES;
import static org.jooq.impl.DSL.inline;
import static org.jooq.impl.DSL.when;

import com.ssafy.enjoytrip.domain.CourseBriefingCandidate;
import com.ssafy.enjoytrip.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CourseStorageRepository implements CourseRepository {
    private static final String PUBLIC = "PUBLIC";
    private static final String READY = "READY";

    private final DSLContext dslContext;

    @Override
    public List<CourseBriefingCandidate> findPublicReadyBriefingCandidates(String regionName, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        return dslContext.select(COURSES.ID, COURSES.TITLE, COURSES.REGION_NAME)
                .from(COURSES)
                .where(publicReadyCandidateCondition())
                .orderBy(regionMatchFirst(regionName), COURSES.CREATED_AT.desc(), COURSES.ID.asc())
                .limit(limit)
                .fetch(record -> new CourseBriefingCandidate(
                        record.get(COURSES.ID),
                        record.get(COURSES.TITLE),
                        record.get(COURSES.REGION_NAME)
                ));
    }

    static Condition publicReadyCandidateCondition() {
        return COURSES.VISIBILITY.eq(PUBLIC)
                .and(COURSES.STATUS.eq(READY))
                .and(COURSES.DELETED_AT.isNull());
    }

    static SortField<Integer> regionMatchFirst(String regionName) {
        if (regionName == null || regionName.isBlank()) {
            return inline(0).asc();
        }

        return when(COURSES.REGION_NAME.eq(regionName), 0)
                .otherwise(1)
                .asc();
    }
}
