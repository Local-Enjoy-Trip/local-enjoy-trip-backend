package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.CourseBriefingCandidateRecord;
import com.ssafy.enjoytrip.storage.db.core.model.LocalPlaceRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NeighborhoodBriefingMapper {
    List<CourseBriefingCandidateRecord> findPublicReadyCandidates(@Param("regionName") String regionName,
                                                               @Param("limit") int limit);

    List<LocalPlaceRecord> findLocalPlaces(@Param("regionName") String regionName,
                                           @Param("limit") int limit);
}
