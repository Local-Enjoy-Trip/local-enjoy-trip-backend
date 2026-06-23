package com.ssafy.enjoytrip.core.support.auth;

import com.ssafy.enjoytrip.core.domain.Member;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

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
                .subject(String.valueOf(member.memberId()))
                .claim("name", member.name())
                .claim("email", member.email())
                .build();
        JwsHeader header = JwsHeader.with(JWT_ALGORITHM).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new IssuedToken(token, TOKEN_TYPE, properties.expirationSeconds());
    }
}
