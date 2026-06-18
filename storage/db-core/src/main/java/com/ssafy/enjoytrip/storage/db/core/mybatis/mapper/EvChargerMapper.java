package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.ChargerItemRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EvChargerMapper {
    List<ChargerItemRecord> findChargers(@Param("region") String region,
                                       @Param("keyword") String keyword,
                                       @Param("limit") int limit,
                                       @Param("offset") int offset);
}
