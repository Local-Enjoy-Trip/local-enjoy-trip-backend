package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import java.util.List;

public interface MemberMapper {
    List<MemberRecord> findAllOrderByCreatedAtDesc();

    MemberRecord findByUserId(String userId);

    MemberRecord findByEmail(String email);

    MemberRecord findByUserIdAndEmail(String userId, String email);

    int existsByUserId(String userId);

    int existsByEmail(String email);

    int insert(MemberRecord record);

    int update(MemberRecord record);

    int deleteByUserId(String userId);
}
