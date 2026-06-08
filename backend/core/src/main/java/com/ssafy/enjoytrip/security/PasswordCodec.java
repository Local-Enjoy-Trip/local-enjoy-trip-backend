package com.ssafy.enjoytrip.security;

public interface PasswordCodec {
    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);

    boolean isEncoded(String password);
}
