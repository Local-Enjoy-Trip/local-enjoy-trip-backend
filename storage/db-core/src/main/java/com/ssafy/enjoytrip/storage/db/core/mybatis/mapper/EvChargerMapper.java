package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.mybatis.row.ChargerItemRow;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EvChargerMapper {
    List<ChargerItemRow> findChargers(@Param("region") String region,
                                       @Param("keyword") String keyword,
                                       @Param("limit") int limit,
                                       @Param("offset") int offset);
}
