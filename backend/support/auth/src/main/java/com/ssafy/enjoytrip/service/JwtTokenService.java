package com.ssafy.enjoytrip.service;

import com.ssafy.enjoytrip.config.JwtProperties;
import com.ssafy.enjoytrip.domain.Member;
import com.ssafy.enjoytrip.web.dto.response.IssuedToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtTokenService {
    private static final String TOKEN_TYPE = "Bearer";
    private static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public IssuedToken issue(Member member) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(properties.expirationSeconds());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("enjoytrip")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(member.userId())
                .claim("name", member.name())
                .claim("email", member.email())
                .build();
        JwsHeader header = JwsHeader.with(JWT_ALGORITHM).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, TOKEN_TYPE, properties.expirationSeconds());
    }
}
