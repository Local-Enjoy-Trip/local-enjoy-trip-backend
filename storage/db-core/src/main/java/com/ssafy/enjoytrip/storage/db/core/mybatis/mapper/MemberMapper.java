package com.ssafy.enjoytrip.storage.db.core.mybatis.mapper;

import com.ssafy.enjoytrip.storage.db.core.entity.MemberEntity;
import java.util.List;

public interface MemberMapper {
    List<MemberEntity> findAllOrderByCreatedAtDesc();

    MemberEntity findByUserId(String userId);

    MemberEntity findByEmail(String email);

    MemberEntity findByUserIdAndEmail(String userId, String email);

    int existsByUserId(String userId);

    int existsByEmail(String email);

    int insert(MemberEntity entity);

    int update(MemberEntity entity);

    int deleteByUserId(String userId);
}
