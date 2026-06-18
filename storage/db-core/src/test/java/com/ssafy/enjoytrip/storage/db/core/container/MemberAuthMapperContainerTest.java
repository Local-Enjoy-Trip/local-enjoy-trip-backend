package com.ssafy.enjoytrip.storage.db.core.container;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.AuthLogRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AuthLogMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberAuthMapperContainerTest extends StorageContainerTestSupport {
    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private AuthLogMapper authLogMapper;

    @DisplayName("MemberMapper는 migration schema에서 회원 생성, 조회, 수정, 삭제를 수행한다")
    @Test
    void memberMapperPersistsAndMutatesMemberRecord() {
        String userId = uniqueId("member");
        MemberRecord record = new MemberRecord(
                userId,
                "회원",
                null,
                userId + "@example.com",
                "encoded-password",
                null,
                37.5665,
                126.9780,
                "서울 중구"
        );

        memberMapper.insert(record);
        MemberRecord saved = memberMapper.findByUserId(userId);
        saved.update(
                "회원수정",
                "nickname",
                userId + "-updated@example.com",
                "encoded-password-2",
                null,
                37.5700,
                126.9820,
                "서울 종로구"
        );
        memberMapper.update(saved);

        MemberRecord updated = memberMapper.findByEmail(userId + "-updated@example.com");

        assertThat(memberMapper.existsByUserId(userId)).isEqualTo(1);
        assertThat(memberMapper.existsByEmail(userId + "-updated@example.com")).isEqualTo(1);
        assertThat(memberMapper.findByUserIdAndEmail(userId, userId + "-updated@example.com")).isNotNull();
        assertThat(updated.getName()).isEqualTo("회원수정");
        assertThat(updated.getNickname()).isEqualTo("nickname");
        assertThat(memberMapper.findAllOrderByCreatedAtDesc())
                .extracting(MemberRecord::getUserId)
                .contains(userId);
        assertThat(memberMapper.deleteByUserId(userId)).isEqualTo(1);
        assertThat(memberMapper.findByUserId(userId)).isNull();
    }

    @DisplayName("AuthLogMapper는 auth_logs identity key와 logged_at을 실제 DB에서 채운다")
    @Test
    void authLogMapperPersistsLoginEvent() {
        String userId = uniqueId("auth");
        AuthLogRecord record = new AuthLogRecord(userId, "LOGIN");

        authLogMapper.insert(record);

        Integer count = jdbcTemplate.queryForObject(
                """
                        select count(*)
                        from auth_logs
                        where user_id = ?
                          and event_type = 'LOGIN'
                          and logged_at is not null
                        """,
                Integer.class,
                userId
        );
        assertThat(record.getId()).isNotNull();
        assertThat(count).isEqualTo(1);
    }
}
