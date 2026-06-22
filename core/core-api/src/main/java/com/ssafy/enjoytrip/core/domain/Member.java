package com.ssafy.enjoytrip.core.domain;

public record Member(
        String userId,
        String name,
        String nickname,
        String email,
        String password,
        String profileImageUrl
) {
    public Member(String userId, String name, String email, String password) {
        this(userId, name, name, email, password, null);
    }

    public Member withPassword(String newPassword) {
        return new Member(
                userId,
                name,
                nickname,
                email,
                newPassword,
                profileImageUrl
        );
    }

    public String displayName() {
        if (!isBlank(nickname)) {
            return nickname;
        }
        if (!isBlank(name)) {
            return name;
        }
        return userId;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
