package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.mybatis.row.CourseBriefingCandidateRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface NeighborhoodBriefingMapper {
    List<CourseBriefingCandidateRow> findPublicReadyCandidates(@Param("regionName") String regionName,
                                                               @Param("limit") int limit);
}
