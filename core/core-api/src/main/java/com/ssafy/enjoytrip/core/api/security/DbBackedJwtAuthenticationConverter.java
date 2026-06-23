package com.ssafy.enjoytrip.core.api.security;

import com.ssafy.enjoytrip.core.domain.MemberRole;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@RequiredArgsConstructor
public class DbBackedJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final String INVALID_SUBJECT_MESSAGE = "유효하지 않은 인증 주체입니다.";

    private final MemberMapper memberMapper;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Long memberId = parseMemberId(jwt.getSubject());
        return new JwtAuthenticationToken(jwt, authorities(memberId));
    }

    private Collection<GrantedAuthority> authorities(Long memberId) {
        MemberRole role = memberRole(memberId);
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    private MemberRole memberRole(Long memberId) {
        if (memberMapper == null) {
            return MemberRole.USER;
        }

        MemberRecord record = memberMapper.findById(memberId);
        if (record == null || record.getRole() == null || record.getRole().isBlank()) {
            return MemberRole.USER;
        }

        return MemberRole.valueOf(record.getRole());
    }

    private static Long parseMemberId(String subject) {
        if (subject == null || subject.isBlank()) {
            throw new BadCredentialsException(INVALID_SUBJECT_MESSAGE);
        }
        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException(INVALID_SUBJECT_MESSAGE, exception);
        }
    }
}
