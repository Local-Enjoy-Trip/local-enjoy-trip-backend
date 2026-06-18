package com.ssafy.enjoytrip.core.api.security.password;

import com.ssafy.enjoytrip.core.domain.auth.PasswordCodec;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public record SpringSecurityPasswordCodec(PasswordEncoder passwordEncoder) implements PasswordCodec {
    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean isEncoded(String password) {
        return password != null && password.startsWith("$2");
    }
}
