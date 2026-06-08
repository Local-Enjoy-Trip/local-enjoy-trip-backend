package com.ssafy.enjoytrip.domain;

import com.ssafy.enjoytrip.security.PasswordCodec;

public record Member(
        String userId,
        String name,
        String email,
        String password,
        String createdAt
) {
    public Member withEncodedPassword(PasswordCodec passwordCodec) {
        return withPassword(passwordCodec.encode(password));
    }

    public Member withEncodedPasswordWhenPresent(PasswordCodec passwordCodec) {
        if (isBlank(password)) {
            return this;
        }
        return withEncodedPassword(passwordCodec);
    }

    public Member withPassword(String newPassword) {
        return new Member(userId, name, email, newPassword, createdAt);
    }

    public boolean canAuthenticate(String rawPassword, PasswordCodec passwordCodec) {
        if (isBlank(rawPassword) || isBlank(password)) {
            return false;
        }
        if (passwordCodec.isEncoded(password)) {
            return passwordCodec.matches(rawPassword, password);
        }
        return password.equals(rawPassword);
    }

    public boolean shouldUpgradePassword(PasswordCodec passwordCodec) {
        return !isBlank(password) && !passwordCodec.isEncoded(password);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
