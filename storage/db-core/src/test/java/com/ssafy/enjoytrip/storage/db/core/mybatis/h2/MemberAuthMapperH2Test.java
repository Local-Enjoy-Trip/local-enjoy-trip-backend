package com.ssafy.enjoytrip.storage.db.core.mybatis.h2;

import static org.assertj.core.api.Assertions.assertThat;

import com.ssafy.enjoytrip.storage.db.core.model.AuthLogRecord;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.AuthLogMapper;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MemberAuthMapperH2Test extends H2MapperTestSupport {
    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private AuthLogMapper authLogMapper;

    @DisplayName("MemberMapper는 H2 인메모리 DB에서 회원 생성, 조회, 수정, 삭제 SQL을 실행한다")
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
                "nickname",
                null,
                37.5700,
                126.9820,
                "서울 종로구"
        );
        memberMapper.update(saved);

        MemberRecord updated = memberMapper.findByEmail(userId + "@example.com");

        assertThat(memberMapper.existsByUserId(userId)).isEqualTo(1);
        assertThat(memberMapper.existsByUserIdOrEmail(userId, "new@example.com")).isEqualTo(1);
        assertThat(memberMapper.existsByUserIdOrEmail("new-user", userId + "@example.com")).isEqualTo(1);
        assertThat(updated.getName()).isEqualTo("회원");
        assertThat(updated.getNickname()).isEqualTo("nickname");
        assertThat(updated.getPassword()).isEqualTo("encoded-password");
        assertThat(updated.getRepresentativeLatitude()).isEqualTo(37.5700);
        assertThat(updated.getRepresentativeLongitude()).isEqualTo(126.9820);
        assertThat(updated.getRepresentativeRegionName()).isEqualTo("서울 종로구");

        updated.update(null, null, null, null, null);
        memberMapper.update(updated);
        MemberRecord cleared = memberMapper.findByUserId(userId);

        assertThat(cleared.getName()).isEqualTo("회원");
        assertThat(cleared.getEmail()).isEqualTo(userId + "@example.com");
        assertThat(cleared.getPassword()).isEqualTo("encoded-password");
        assertThat(cleared.getNickname()).isNull();
        assertThat(cleared.getProfileImageUrl()).isNull();
        assertThat(cleared.getRepresentativeLatitude()).isNull();
        assertThat(cleared.getRepresentativeLongitude()).isNull();
        assertThat(cleared.getRepresentativeRegionName()).isNull();
        assertThat(memberMapper.findAllOrderByCreatedAtDesc())
                .extracting(MemberRecord::getUserId)
                .contains(userId);
        assertThat(memberMapper.deleteByUserId(userId)).isEqualTo(1);
        assertThat(memberMapper.findByUserId(userId)).isNull();
    }

    @DisplayName("AuthLogMapper는 H2 인메모리 DB에서 identity key와 logged_at을 채운다")
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
