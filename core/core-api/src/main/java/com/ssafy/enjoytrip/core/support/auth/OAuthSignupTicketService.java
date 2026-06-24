package com.ssafy.enjoytrip.core.support.auth;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthSignupTicketService {
    private static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;
    private static final long EXPIRATION_SECONDS = 600;
    private static final String PURPOSE = "oauth-signup";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public OAuthSignupTicket issue(String provider, String providerSubject, String email, String suggestedName) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(EXPIRATION_SECONDS);
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("enjoytrip")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(providerSubject)
                .claim("purpose", PURPOSE)
                .claim("provider", provider)
                .claim("email", email)
                .claim("suggestedName", suggestedName)
                .build();
        String ticket = jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(JWT_ALGORITHM).build(), claims)
        ).getTokenValue();
        return new OAuthSignupTicket(ticket, email, suggestedName, EXPIRATION_SECONDS);
    }

    public PendingOAuthSignup verify(String ticket) {
        Jwt jwt = jwtDecoder.decode(ticket);
        if (!PURPOSE.equals(jwt.getClaimAsString("purpose"))) {
            throw new IllegalArgumentException("유효하지 않은 OAuth 가입 티켓입니다.");
        }
        return new PendingOAuthSignup(
                jwt.getClaimAsString("provider"),
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("suggestedName")
        );
    }

    public record PendingOAuthSignup(
            String provider,
            String providerSubject,
            String email,
            String suggestedName
    ) {
    }
}
