package com.ssafy.enjoytrip.core.api.security;

import com.ssafy.enjoytrip.core.domain.MemberRole;
import com.ssafy.enjoytrip.storage.db.core.model.MemberRecord;
import com.ssafy.enjoytrip.storage.db.core.mybatis.mapper.MemberMapper;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@RequiredArgsConstructor
public class DbBackedJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final MemberMapper memberMapper;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, authorities(jwt.getSubject()));
    }

    private Collection<GrantedAuthority> authorities(String userId) {
        MemberRole role = memberRole(userId);
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    private MemberRole memberRole(String userId) {
        if (memberMapper == null || userId == null || userId.isBlank()) {
            return MemberRole.USER;
        }

        MemberRecord record = memberMapper.findByUserId(userId);
        if (record == null || record.getRole() == null || record.getRole().isBlank()) {
            return MemberRole.USER;
        }

        return MemberRole.valueOf(record.getRole());
    }
}
