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

    @DisplayName("MemberMapper는 H2 인메모리 DB에서 email/memberId 기반 회원 SQL을 실행한다")
    @Test
    void memberMapperPersistsAndMutatesMemberRecord() {
        String email = uniqueId("member") + "@example.com";
        MemberRecord record = new MemberRecord(
                "회원",
                null,
                email,
                "encoded-password",
                null
        );

        memberMapper.insert(record);
        Long memberId = record.getId();
        MemberRecord saved = memberMapper.findById(memberId);
        saved.updateNickname("nickname");
        memberMapper.update(saved);

        MemberRecord updated = memberMapper.findByEmail(email);

        assertThat(memberMapper.existsByEmail(email)).isEqualTo(1);
        assertThat(memberMapper.existsByEmail("new@example.com")).isZero();
        assertThat(updated.getName()).isEqualTo("회원");
        assertThat(updated.getNickname()).isEqualTo("nickname");
        assertThat(updated.getPassword()).isEqualTo("encoded-password");
        memberMapper.updateProfileImage(
                memberId,
                "profiles/" + memberId + "/sample.jpg",
                "http://localhost:9000/dongnepin-notes/profiles/" + memberId + "/sample.jpg"
        );
        MemberRecord profileUpdated = memberMapper.findById(memberId);

        assertThat(profileUpdated.getName()).isEqualTo("회원");
        assertThat(profileUpdated.getEmail()).isEqualTo(email);
        assertThat(profileUpdated.getPassword()).isEqualTo("encoded-password");
        assertThat(profileUpdated.getNickname()).isEqualTo("nickname");
        assertThat(profileUpdated.getProfileImageObjectKey()).isEqualTo(
                "profiles/" + memberId + "/sample.jpg"
        );
        assertThat(profileUpdated.getProfileImageUrl()).isEqualTo(
                "http://localhost:9000/dongnepin-notes/profiles/" + memberId + "/sample.jpg"
        );

        profileUpdated.updateNickname(null);
        memberMapper.update(profileUpdated);
        MemberRecord cleared = memberMapper.findById(memberId);

        assertThat(cleared.getName()).isEqualTo("회원");
        assertThat(cleared.getEmail()).isEqualTo(email);
        assertThat(cleared.getPassword()).isEqualTo("encoded-password");
        assertThat(cleared.getNickname()).isNull();
        assertThat(cleared.getProfileImageUrl()).isEqualTo(
                "http://localhost:9000/dongnepin-notes/profiles/" + memberId + "/sample.jpg"
        );
        assertThat(cleared.getProfileImageObjectKey()).isEqualTo(
                "profiles/" + memberId + "/sample.jpg"
        );
        assertThat(memberMapper.findAllOrderByCreatedAtDesc())
                .extracting(MemberRecord::getEmail)
                .contains(email);
        assertThat(memberMapper.deleteById(memberId)).isEqualTo(1);

        assertThat(memberMapper.findById(memberId)).isNull();
    }

    @DisplayName("MemberMapper는 이메일 prefix LIKE 키워드로 회원을 검색한다")
    @Test
    void findByEmailKeywordReturnsPrefixMatchedMembers() {
        String prefix = uniqueId("search");
        String emailA = prefix + "_alice@example.com";
        String emailB = prefix + "_bob@example.com";
        String emailOther = uniqueId("other") + "@example.com";
        seedMember("Alice", emailA);
        seedMember("Bob", emailB);
        seedMember("Other", emailOther);

        var results = memberMapper.findByEmailKeyword(prefix);

        assertThat(results)
                .extracting(MemberRecord::getEmail)
                .containsExactlyInAnyOrder(emailA, emailB)
                .doesNotContain(emailOther);
    }

    @DisplayName("AuthLogMapper는 H2 인메모리 DB에서 memberId와 logged_at을 채운다")
    @Test
    void authLogMapperPersistsLoginEvent() {
        Long memberId = seedMember("auth", uniqueId("auth") + "@example.com");
        AuthLogRecord record = new AuthLogRecord(memberId, "LOGIN");

        authLogMapper.insert(record);

        Integer count = jdbcTemplate.queryForObject(
                """
                select count(*)
                from auth_logs
                where member_id = ?
                  and event_type = 'LOGIN'
                  and logged_at is not null
                """,
                Integer.class,
                memberId
        );
        assertThat(record.getId()).isNotNull();
        assertThat(count).isEqualTo(1);
    }
}
