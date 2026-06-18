package com.ssafy.enjoytrip.core.domain.auth;

public interface PasswordCodec {
    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);

    boolean isEncoded(String password);
}
