package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.HotplaceRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface HotplaceMapper {
    List<HotplaceRecord> findAllOrderByCreatedAtDesc();

    List<HotplaceRecord> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    int existsById(String id);

    int insert(HotplaceRecord record);

    int deleteByIdAndMemberId(@Param("id") String id, @Param("memberId") Long memberId);
}
