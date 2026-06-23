package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MemberMapper {
    List<MemberRecord> findAllOrderByCreatedAtDesc();

    MemberRecord findById(Long memberId);

    MemberRecord findByEmail(String email);

    int existsByEmail(String email);

    int insert(MemberRecord record);

    int update(MemberRecord record);

    int updateProfileImage(
            @Param("memberId") Long memberId,
            @Param("profileImageObjectKey") String profileImageObjectKey,
            @Param("profileImageUrl") String profileImageUrl
    );

    int deleteById(Long memberId);
}
